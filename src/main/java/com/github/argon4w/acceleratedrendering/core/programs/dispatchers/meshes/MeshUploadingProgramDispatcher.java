package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IUploadingOverride;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Map;

import static org.lwjgl.opengl.GL46.*;

public class MeshUploadingProgramDispatcher {

	private	static	final	int							GROUP_SIZE					= 128;
	private	static	final	int							DISPATCH_COUNT_Y_Z			= 1;
	public	static	final	int							SPARSE_MESH_BUFFER_INDEX	= 5;
	public	static	final	int							MESH_BUFFER_INDEX			= 7;
	public	static	final	int							MESH_INFO_BUFFER_INDEX		= 8;

	private			final	MeshOffsets					offsets;
	private			final	Map<IServerBuffer,	Buffer>	buffers;

	private					int							lastBarriers;

	public MeshUploadingProgramDispatcher() {
		this.offsets = new MeshOffsets							(CoreFeature.getPooledBatchingSize());
		this.buffers = new Reference2ObjectLinkedOpenHashMap<>	();

		this.lastBarriers = GL_SHADER_STORAGE_BARRIER_BIT;
	}

	public void dispatch(Collection<AcceleratedBufferBuilder> builders, Buffers ringBuffer) {
		glMemoryBarrier(lastBarriers);

		var transform = ringBuffer
				.getEnvironment						()
				.selectTransformProgramDispatcher	();

		for (var builder : builders) {
			var vertexBuffer	= builder.getVertexBuffer	();
			var varyingBuffer	= builder.getVaryingBuffer	();
			var meshUploaders	= builder.getMeshUploaders	();
			var meshVertexCount = builder.getMeshVertexCount();

			vertexBuffer	.reserve		(meshVertexCount * builder.getVertexSize	());
			varyingBuffer	.reserve		(meshVertexCount * builder.getVaryingSize	());
			vertexBuffer	.allocateOffset	();
			varyingBuffer	.allocateOffset	();
			offsets			.setupOffsets	(builder);

			for	(var meshUploader : meshUploaders.values()) {
				var serverMesh	= meshUploader	.getServerMesh	();
				var meshBuffer	= serverMesh	.meshBuffer		();
				var buffer		= buffers		.get			(meshBuffer);

				if (buffer == null) {
					buffer = new Buffer(meshBuffer, ringBuffer);
				}

				buffer.add(meshUploader);
			}
		}

		for (var buffer : buffers.values()) {
			var sparse	= buffer.getSparseUploads	();
			var dense	= buffer.getDenseUploads	();
			var all		= buffer.getMeshUploads		();

			for (var uploads : all.getUploads().values()) {
				var mesh		= uploads.getMesh		();
				var uploaders	= uploads.getMeshUploads();
				var uploadCount	= uploads.getMeshCounter();

				var upload = mesh.isDense(uploadCount)
						? dense
						: sparse;

				for (var uploader : uploaders) {
					upload.add(uploader);
				}
			}
		}

		ringBuffer.prepare				();
		ringBuffer.bindTransformBuffers	();

		for (var builder : builders) {
			var offset		= 0;
			var sparseStart	= 0;

			var vertexBuffer	= builder.getVertexBuffer	();
			var varyingBuffer	= builder.getVaryingBuffer	();
			var vertexCount		= builder.getVertexCount	();

			var vertexAddress	= vertexBuffer	.getCurrent				();
			var varyingAddress	= varyingBuffer	.getCurrent				();
			var vertexOffset	= builder		.getVertexCountOffset	();
			var varyingOffset	= builder		.getVaryingCountOffset	();

			for (var buffer : buffers.values()) {
				var sparse		= buffer.getSparseUploads	();
				var meshBuffer	= buffer.getMeshBuffer		();
				var upload		= sparse.getUploads			()[builder.getIndex()];

				if (upload == null) {
					continue;
				}

				for	(var uploader : upload.getUploaders()) {
					var mesh		= uploader	.getServerMesh	();
					var meshInfos	= uploader	.getMeshInfos	();
					var meshCount	= meshInfos	.getMeshCount	();
					var meshSize	= mesh		.size			();

					for (var i = 0; i < meshCount; i ++) {
						builder.getColorOffset		().at(offset)	.putInt			(vertexAddress, meshInfos	.getColor		(i));
						builder.getUv1Offset		().at(offset)	.putInt			(vertexAddress, meshInfos	.getOverlay		(i));
						builder.getUv2Offset		().at(offset)	.putInt			(vertexAddress, meshInfos	.getLight		(i));

						builder.getVaryingSharing	().at(offset)	.putInt			(varyingAddress, meshInfos	.getSharing		(i));
						builder.getVaryingMesh		().at(offset)	.putInt			(varyingAddress, mesh		.offset			());
						builder.getVaryingShouldCull().at(offset)	.putInt			(varyingAddress, meshInfos	.getShouldCull	(i));
						builder.getProgramOverride	()				.uploadVarying	(varyingAddress, offset);

						for (var offsetValue = 0; offsetValue < meshSize; offsetValue ++) {
							builder
									.getVaryingOffset	()
									.at					(offset)
									.at					(offsetValue)
									.putInt				(varyingAddress, offsetValue);
						}

						offset += meshSize;
					}
				}

				var count = offset - sparseStart;

				if (count != 0) {
					meshBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, SPARSE_MESH_BUFFER_INDEX);

					lastBarriers |=	transform.dispatch(
							builder,
							vertexBuffer,
							varyingBuffer,
							count,
							sparseStart + vertexCount + vertexOffset,
							sparseStart + vertexCount + varyingOffset
					);
				}

				sparseStart = offset;
			}
		}

		for (var buffer : buffers.values()) {
			var meshBuffer = buffer.getMeshBuffer();

			for (var group : buffer.getDenseUploads().getGroups().values()) {
				var mesh = group.getMesh();

				for (var upload : group.getUploads()) {
					if (upload == null) {
						continue;
					}

					var override			= upload	.getOverride		();
					var overrideUploaders	= upload	.getMeshUploads		();
					var overrideCounts		= upload	.getMeshCounter		();
					var uploading			= override	.uploading			();
					var infoBuffer			= ringBuffer.getMeshInfoBuffer	();

					if (overrideCounts == 0) {
						continue;
					}

					for (var uploader : overrideUploaders) {
						var meshOffsets		= offsets		.reserve		(uploader);
						var vertexOffset	= meshOffsets	.vertexOffset	();
						var varyingOffset	= meshOffsets	.varyingOffset	();
						var address			= infoBuffer	.reserve		(uploader.getMeshInfoSize());

						uploader.upload(
								address,
								(int) vertexOffset,
								(int) varyingOffset
						);
					}

					transform.resetOverride	();
					uploading.useProgram	();
					uploading.setupProgram	();

					meshBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, MESH_BUFFER_INDEX);
					infoBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, MESH_INFO_BUFFER_INDEX);

					lastBarriers |= uploading.dispatchUploading(
							overrideCounts,
							mesh.size	(),
							mesh.offset	()
					);
				}
			}
		}

		for (var buffer : buffers.values()) {
			buffer.clear();
		}

		offsets.clear();
	}

	public void clear() {
		for (var buffer : buffers.values()) {
			buffer.endUpload();
		}
	}

	public static class Default implements IUploadingOverride {

		private final long				meshInfoSize;
		private final ComputeProgram	program;
		private final Uniform			meshCountUniform;
		private final Uniform			meshSizeUniform;
		private final Uniform			meshOffsetUniform;

		public Default(ResourceLocation key, long meshInfoSize) {
			this.meshInfoSize			= meshInfoSize;
			this.program				= ComputeShaderProgramLoader.getProgram(key);
			this.meshCountUniform		= program					.getUniform("meshCount");
			this.meshSizeUniform		= program					.getUniform("meshSize");
			this.meshOffsetUniform		= program					.getUniform("meshOffset");
		}

		@Override
		public long getMeshInfoSize() {
			return meshInfoSize;
		}

		@Override
		public void useProgram() {
			program.useProgram();
		}

		@Override
		public void setupProgram() {
			program.setup();
		}

		@Override
		public void uploadMeshInfo(long meshInfoAddress, int meshInfoIndex) {

		}

		@Override
		public int dispatchUploading(
				int		meshCount,
				int		meshSize,
				long	meshOffset
		) {
			meshCountUniform	.uploadUnsignedInt(meshCount);
			meshSizeUniform		.uploadUnsignedInt(meshSize);
			meshOffsetUniform	.uploadUnsignedInt((int) meshOffset);

			program.dispatch(
					((meshCount * meshSize) + GROUP_SIZE - 1) / GROUP_SIZE,
					DISPATCH_COUNT_Y_Z,
					DISPATCH_COUNT_Y_Z
			);

			return program.getBarrierFlags();
		}
	}

	@Getter
	public class Buffer {

		private final IServerBuffer	meshBuffer;
		private final SparseUploads	sparseUploads;
		private final DenseUploads	denseUploads;
		private final MeshUploads	meshUploads;

		public Buffer(IServerBuffer meshBuffer, Buffers buffer) {
			this.meshBuffer		= meshBuffer;
			this.sparseUploads	= new SparseUploads	(buffer, offsets);
			this.denseUploads	= new DenseUploads	(buffer);
			this.meshUploads	= new MeshUploads	();

			buffers.put(meshBuffer, this);
		}

		public void add(MeshUploader uploader) {
			meshUploads.add(uploader);
		}

		public void clear() {
			sparseUploads	.clear();
			denseUploads	.clear();
			meshUploads		.clear();
		}

		public void endUpload() {
			sparseUploads	.endUpload();
			denseUploads	.endUpload();
			meshUploads		.endUpload();
		}
	}
}
