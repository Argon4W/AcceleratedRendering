package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleDynamicMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ProgramOverride;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import lombok.Setter;

import java.util.function.LongSupplier;

public class MeshUploaderPool extends SimpleResetPool<MeshUploaderPool.MeshUploader, Buffers> {

	public MeshUploaderPool(Buffers buffers) {
		super(128, buffers);
	}

	@Override
	protected MeshUploader create(Buffers context, int i) {
		return new MeshUploader(context);
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

		private			final	IMemoryInterface	meshInfoVertexOffset;
		private			final	IMemoryInterface	meshInfoVaryingOffset;
		private			final	IMemoryInterface	meshInfoSharing;
		private			final	IMemoryInterface	meshInfoShouldCull;
		private			final	IMemoryInterface	meshInfoColor;
		private			final	IMemoryInterface	meshInfoOverlay;
		private			final	IMemoryInterface	meshInfoLight;
		@Getter private	final	IMeshInfoCache		meshInfos;
		@Getter private	final	Buffers				buffers;

		@Getter	@Setter private	ServerMesh			serverMesh;
		@Getter @Setter private ProgramOverride		override;
		@Getter @Setter private int					index;

		public MeshUploader(Buffers buffers) {
			this.meshInfoVertexOffset	= new SimpleDynamicMemoryInterface	(0L * 4L, this);
			this.meshInfoVaryingOffset	= new SimpleDynamicMemoryInterface	(1L * 4L, this);
			this.meshInfoSharing		= new SimpleDynamicMemoryInterface	(2L * 4L, this);
			this.meshInfoShouldCull		= new SimpleDynamicMemoryInterface	(3L * 4L, this);
			this.meshInfoColor			= new SimpleDynamicMemoryInterface	(4L * 4L, this);
			this.meshInfoOverlay		= new SimpleDynamicMemoryInterface	(5L * 4L, this);
			this.meshInfoLight			= new SimpleDynamicMemoryInterface	(6L * 4L, this);
			this.meshInfos				= CoreFeature.createMeshInfoCache	();
			this.buffers				= buffers;

			this.index		= -1;
			this.override	= null;
			this.serverMesh	= null;
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
				meshInfoVertexOffset	.putIntAt			(meshInfoAddress, i, vertexOffset	+ i * serverMesh.size());
				meshInfoVaryingOffset	.putIntAt			(meshInfoAddress, i, varyingOffset	+ i * serverMesh.size());
				meshInfoSharing			.putIntAt			(meshInfoAddress, i, meshInfos.getSharing	(i));
				meshInfoShouldCull		.putIntAt			(meshInfoAddress, i, meshInfos.getShouldCull(i));
				meshInfoColor			.putIntAt			(meshInfoAddress, i, meshInfos.getColor		(i));
				meshInfoOverlay			.putIntAt			(meshInfoAddress, i, meshInfos.getOverlay	(i));
				meshInfoLight			.putIntAt			(meshInfoAddress, i, meshInfos.getLight		(i));
				override				.uploadMeshInfo		(meshInfoAddress, i);
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
			return override.getMeshInfoSize();
		}
	}
}
