package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IUploadingShaderProgramOverride;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL46.*;
import static com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;

public class MeshUploadingProgramDispatcher {

	private static	final	int									GROUP_SIZE					= 128;
	private static	final	int									DISPATCH_COUNT_Y_Z			= 1;
	public static	final	int									SPARSE_MESH_BUFFER_INDEX	= 5;
	public static	final	int									MESH_BUFFER_INDEX			= 7;
	public static	final	int									MESH_INFO_BUFFER_INDEX		= 8;

	private			final	Map<IServerBuffer, DenseStorage>	denseStorages;
	private			final	Map<IServerBuffer, SparseStorage>	sparseStorages;
	private			final	Map<IServerBuffer, Collector>		uploaderCollectors;

	private					int									lastBarriers;

	public MeshUploadingProgramDispatcher() {
		this.denseStorages		= new Reference2ObjectLinkedOpenHashMap<>();
		this.sparseStorages		= new Reference2ObjectLinkedOpenHashMap<>();
		this.uploaderCollectors	= new Reference2ObjectLinkedOpenHashMap<>();

		this.lastBarriers		= GL_SHADER_STORAGE_BARRIER_BIT;
	}

	public static class Collector {

		private final Reference2ObjectMap<ServerMesh, List<MeshUploader>>	meshUploaders;
		private final Reference2ObjectMap<ServerMesh, MutableInt>			meshUploadCounts;

		public Collector() {
			this.meshUploaders		= new Reference2ObjectOpenHashMap<>();
			this.meshUploadCounts	= new Reference2ObjectOpenHashMap<>();
		}

		public void add(MeshUploader uploader) {
			var serverMesh	= uploader			.getServerMesh	();
			var meshCount	= uploader			.getMeshCount	();
			var uploaders	= meshUploaders		.get			(serverMesh);
			var uploadCount	= meshUploadCounts	.get			(serverMesh);

			if (uploaders == null) {
				uploaders	= new ReferenceArrayList<>	();
				uploadCount	= new MutableInt			();

				meshUploaders	.put(serverMesh, uploaders);
				meshUploadCounts.put(serverMesh, uploadCount);
			}

			uploaders	.add(uploader);
			uploadCount	.add(meshCount);
		}

		public void clear() {
			for (var serverMesh : meshUploaders.keySet()) {
				meshUploaders	.get(serverMesh).clear		();
				meshUploadCounts.get(serverMesh).setValue	(0);
			}
		}
	}

	public interface IMeshStorage {

		void add(MeshUploader uploader);
	}

	public static class SparseStorage implements IMeshStorage {

		private final Reference2ObjectMap<AcceleratedBufferBuilder, List<MeshUploader>>	sparseUploads;
		private final Reference2ObjectMap<AcceleratedBufferBuilder, Offsets>			sparseOffsets;

		public SparseStorage() {
			this.sparseUploads = new Reference2ObjectOpenHashMap<>();
			this.sparseOffsets = new Reference2ObjectOpenHashMap<>();

			this.sparseUploads.defaultReturnValue(ReferenceLists.emptyList());
			this.sparseOffsets.defaultReturnValue(null);
		}

		@Override
		public void add(MeshUploader uploader) {
			var builder = uploader		.getBufferBuilder	();
			var uploads = sparseUploads	.getOrDefault		(builder, null);
			var offsets = sparseOffsets	.getOrDefault		(builder, null);

			if (uploads == null) {
				uploads = new ReferenceArrayList<>	();
				offsets = new Offsets				(builder);

				sparseUploads.put(builder, uploads);
				sparseOffsets.put(builder, offsets);
			}

			uploads.add(uploader);
			offsets.add(uploader);
		}

		public Offsets getOffsets(MeshUploader uploader) {
			var builder = uploader		.getBufferBuilder	();
			var uploads = sparseUploads	.getOrDefault		(builder, null);
			var offsets = sparseOffsets	.getOrDefault		(builder, null);

			if (offsets == null) {
				offsets = new Offsets				(builder);
				uploads = new ReferenceArrayList<>	();

				sparseUploads.put(builder, uploads);
				sparseOffsets.put(builder, offsets);
			}

			return offsets;
		}

		public void clear() {
			sparseUploads.clear();
			sparseOffsets.clear();
		}

		public static class Offsets {

			private final MutableLong vertexOffset;
			private final MutableLong varyingOffset;

			public Offsets(AcceleratedBufferBuilder builder) {
				this.vertexOffset	= new MutableLong(builder.getVertexCountOffset	());
				this.varyingOffset	= new MutableLong(builder.getVaryingCountOffset	());
			}

			public void add(MeshUploader uploader) {
				var vertexCount = uploader.getVertexCount();

				vertexOffset	.add(vertexCount);
				varyingOffset	.add(vertexCount);
			}

			public long getVertexOffset(long vertexCount) {
				return vertexOffset.getAndAdd(vertexCount);
			}

			public long getVaryingOffset(long vertexCount) {
				return varyingOffset.getAndAdd(vertexCount);
			}
		}
	}

	public static class DenseStorage implements IMeshStorage {

		private final Reference2ObjectMap<ServerMesh, Map<IUploadingShaderProgramOverride, List<MeshUploader>>>	denseUploaders;
		private final Reference2ObjectMap<ServerMesh, Map<IUploadingShaderProgramOverride, MutableInt>>			denseCounts;

		public DenseStorage() {
			this.denseUploaders	= new Reference2ObjectOpenHashMap<>();
			this.denseCounts	= new Reference2ObjectOpenHashMap<>();
		}

		@Override
		public void add(MeshUploader uploader) {
			var serverMesh		= uploader		.getServerMesh			();
			var override		= uploader		.getUploadingOverride	();
			var uploaders		= denseUploaders.get					(serverMesh);
			var uploadCounts	= denseCounts	.get					(serverMesh);

			if (uploaders == null) {
				uploaders		= new Reference2ObjectOpenHashMap<>();
				uploadCounts	= new Reference2ObjectOpenHashMap<>();

				denseUploaders	.put(serverMesh, uploaders);
				denseCounts		.put(serverMesh, uploadCounts);
			}

			var overrideUploaders	= uploaders		.get(override);
			var overrideCounts		= uploadCounts	.get(override);

			if (overrideUploaders == null) {
				overrideUploaders	= new ReferenceArrayList<>	();
				overrideCounts		= new MutableInt			(0);

				uploaders	.put(override, overrideUploaders);
				uploadCounts.put(override, overrideCounts);
			}

			overrideUploaders	.add(uploader);
			overrideCounts		.add(uploader.getMeshCount());
		}

		public void clear() {
			for		(var serverMesh	: denseUploaders.keySet()) {
				for	(var list		: denseUploaders.get(serverMesh).values()) list	.clear();
				for	(var count		: denseCounts	.get(serverMesh).values()) count.setValue(0L);
			}
		}
	}

	public void dispatch(Collection<AcceleratedBufferBuilder> builders, AcceleratedRingBuffers.Buffers buffer) {
		glMemoryBarrier(lastBarriers);

		var transform = buffer
				.getEnvironment()
				.selectTransformProgramDispatcher	();

		for (var builder : builders) {
			var vertexBuffer	= builder.getVertexBuffer	();
			var varyingBuffer	= builder.getVaryingBuffer	();
			var meshVertexCount = builder.getMeshVertexCount();

			vertexBuffer	.reserve		(meshVertexCount * builder.getVertexSize	());
			varyingBuffer	.reserve		(meshVertexCount * builder.getVaryingSize	());
			vertexBuffer	.allocateOffset	();
			varyingBuffer	.allocateOffset	();
		}

		for		(var builder	: builders) {
			for	(var uploader	: builder.getMeshUploaders().values()) {
				var meshBuffer	= uploader.getServerMesh()	.meshBuffer	();
				var dense		= denseStorages				.get		(meshBuffer);
				var sparse		= sparseStorages			.get		(meshBuffer);
				var collector	= uploaderCollectors		.get		(meshBuffer);

				if (		dense		== null
						||	sparse		== null
						||	collector	== null
				) {
					dense		= new DenseStorage	();
					sparse		= new SparseStorage	();
					collector	= new Collector		();

					denseStorages		.put(meshBuffer, dense);
					sparseStorages		.put(meshBuffer, sparse);
					uploaderCollectors	.put(meshBuffer, collector);
				}

				collector.add(uploader);
			}
		}

		for (var meshBuffer : uploaderCollectors.keySet()) {
			var collector = uploaderCollectors.get(meshBuffer);

			for (var serverMesh : collector.meshUploaders.keySet()) {
				var uploaders	= collector.meshUploaders	.get(serverMesh);
				var uploadCount	= collector.meshUploadCounts.get(serverMesh).getValue();

				var storage = serverMesh.isDense(uploadCount)
						? denseStorages	.get(meshBuffer)
						: sparseStorages.get(meshBuffer);

				for (var uploader : uploaders) {
					storage.add(uploader);
				}
			}
		}

		buffer.prepare				();
		buffer.bindTransformBuffers	();

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

			for (var meshBuffer : sparseStorages.keySet()) {
				var storage = sparseStorages.get(meshBuffer);

				if (storage == null) {
					continue;
				}

				for	(var uploader : storage.sparseUploads.get(builder)) {
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
						builder.getTransformOverride()				.uploadVarying	(varyingAddress, offset);

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

		for (var meshBuffer : denseStorages.keySet()) {
			var dense	= denseStorages	.get(meshBuffer);
			var sparse 	= sparseStorages.get(meshBuffer);

			for (var serverMesh : dense.denseUploaders.keySet()) {
				var uploaders	= dense.denseUploaders	.get(serverMesh);
				var counts		= dense.denseCounts		.get(serverMesh);

				for (var override : uploaders.keySet()) {
					var overrideUploaders	= uploaders	.get				(override);
					var overrideCounts		= counts	.get				(override).getValue();
					var infoBuffer			= buffer	.getMeshInfoBuffer	();

					if (overrideCounts == 0) {
						continue;
					}

					for (var uploader : overrideUploaders) {
						var vertexCount		= uploader	.getVertexCount		();
						var offsets			= sparse	.getOffsets			(uploader);
						var vertexOffset	= offsets	.getVertexOffset	(vertexCount);
						var varyingOffset	= offsets	.getVaryingOffset	(vertexCount);
						var address			= infoBuffer.reserve			(uploader.getMeshInfoSize());

						uploader.upload(
								address,
								(int) vertexOffset,
								(int) varyingOffset
						);
					}

					transform	.resetOverride	();
					override	.useProgram		();
					override	.setupProgram	();

					meshBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, MESH_BUFFER_INDEX);
					infoBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, MESH_INFO_BUFFER_INDEX);

					lastBarriers |= override.dispatchUploading(
							overrideCounts,
							serverMesh.size		(),
							serverMesh.offset	()
					);
				}
			}
		}

		for (var meshBuffer : uploaderCollectors.keySet()) {
			denseStorages		.get(meshBuffer).clear();
			sparseStorages		.get(meshBuffer).clear();
			uploaderCollectors	.get(meshBuffer).clear();
		}
	}

	public static class Default implements IUploadingShaderProgramOverride {

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
}
