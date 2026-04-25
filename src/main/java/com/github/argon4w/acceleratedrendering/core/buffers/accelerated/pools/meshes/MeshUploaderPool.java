package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleDynamicMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IUploadingShaderProgramOverride;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import lombok.Setter;

import java.util.function.LongSupplier;

public class MeshUploaderPool extends SimpleResetPool<MeshUploaderPool.MeshUploader, Void> {

	public MeshUploaderPool() {
		super(128, null);
	}

	@Override
	protected MeshUploader create(Void context, int i) {
		return new MeshUploader();
	}

	@Override
	protected void reset(MeshUploader meshUploader) {
		meshUploader.reset();
	}

	@Override
	protected void delete(MeshUploader meshUploader) {
		meshUploader.delete();
	}

	@Override
	public MeshUploader fail() {
		expand();
		return get();
	}

	public static class MeshUploader implements LongSupplier {

		public			final	IMemoryInterface				meshInfoVertexOffset;
		public			final	IMemoryInterface				meshInfoVaryingOffset;
		public			final	IMemoryInterface				meshInfoSharing;
		public			final	IMemoryInterface				meshInfoNoCull;
		public			final	IMemoryInterface				meshInfoColor;
		public			final	IMemoryInterface				meshInfoOverlay;
		public			final	IMemoryInterface				meshInfoLight;
		@Getter private	final	IMeshInfoCache					meshInfos;

		@Getter	@Setter private	ServerMesh						serverMesh;
		@Getter @Setter private AcceleratedBufferBuilder		bufferBuilder;
		@Getter @Setter private IUploadingShaderProgramOverride	uploadingOverride;

		public MeshUploader() {
			this.meshInfoVertexOffset	= new SimpleDynamicMemoryInterface	(0L * 4L, this);
			this.meshInfoVaryingOffset	= new SimpleDynamicMemoryInterface	(1L * 4L, this);
			this.meshInfoSharing		= new SimpleDynamicMemoryInterface	(2L * 4L, this);
			this.meshInfoNoCull			= new SimpleDynamicMemoryInterface	(3L * 4L, this);
			this.meshInfoColor			= new SimpleDynamicMemoryInterface	(4L * 4L, this);
			this.meshInfoOverlay		= new SimpleDynamicMemoryInterface	(5L * 4L, this);
			this.meshInfoLight			= new SimpleDynamicMemoryInterface	(6L * 4L, this);
			this.meshInfos				= CoreFeature.createMeshInfoCache	();

			this.serverMesh			= null;
			this.bufferBuilder		= null;
			this.uploadingOverride	= null;
		}

		public void addUpload(
				int color,
				int light,
				int overlay,
				int sharing,
				int shouldCull
		) {
			meshInfos.setup(
					color,
					light,
					overlay,
					sharing,
					shouldCull
			);
		}

		public void upload(
				long	meshInfoAddress,
				int		vertexOffset,
				int		varyingOffset
		) {
			var meshCount = meshInfos.getMeshCount();

			for (var i = 0; i < meshCount; i ++) {
				meshInfoVertexOffset	.at(i)	.putInt			(meshInfoAddress, vertexOffset	+ i * serverMesh.size());
				meshInfoVaryingOffset	.at(i)	.putInt			(meshInfoAddress, varyingOffset	+ i * serverMesh.size());
				meshInfoSharing			.at(i)	.putInt			(meshInfoAddress, meshInfos.getSharing		(i));
				meshInfoNoCull			.at(i)	.putInt			(meshInfoAddress, meshInfos.getShouldCull	(i));
				meshInfoColor			.at(i)	.putInt			(meshInfoAddress, meshInfos.getColor		(i));
				meshInfoOverlay			.at(i)	.putInt			(meshInfoAddress, meshInfos.getOverlay		(i));
				meshInfoLight			.at(i)	.putInt			(meshInfoAddress, meshInfos.getLight		(i));
				uploadingOverride				.uploadMeshInfo	(meshInfoAddress, i);
			}
		}

		public void reset() {
			meshInfos.reset();
		}

		public void delete() {
			meshInfos.delete();
		}

		public int getMeshCount() {
			return meshInfos.getMeshCount();
		}

		public long getMeshInfoSize() {
			return getMeshCount() * getAsLong();
		}

		public int getVertexCount() {
			return getMeshCount() * serverMesh.size();
		}

		@Override
		public long getAsLong() {
			return uploadingOverride.getMeshInfoSize();
		}
	}
}
