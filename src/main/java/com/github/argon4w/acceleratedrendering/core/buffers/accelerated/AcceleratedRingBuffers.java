package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.Sync;
import com.github.argon4w.acceleratedrendering.core.backends.VertexArray;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool.IDrawContext;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool.IElementSegment;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerKey;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.functions.ILayerFunction;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage.ILayerStorage;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.MappedBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool.StagingBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.utils.LoopResetPool;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Map;

import static org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER;

public class AcceleratedRingBuffers extends LoopResetPool<AcceleratedRingBuffers.Buffers, IBufferEnvironment> {

	public AcceleratedRingBuffers(IBufferEnvironment bufferEnvironment) {
		super(CoreFeature.getPooledRingBufferSize(), bufferEnvironment);
	}

	@Override
	protected Buffers create(IBufferEnvironment context, int i) {
		return new Buffers(context);
	}

	@Override
	protected void reset(Buffers buffers) {

	}

	@Override
	protected void delete(Buffers buffers) {
		buffers.delete();
	}

	@Override
	protected boolean test(Buffers buffers) {
		return buffers.isFree();
	}

	@Override
	public void init(Buffers buffers) {
		buffers.setUsed();
	}

	@Override
	protected Buffers fail(boolean force) {
		var index = 0;

		if (force) {
			index = size;
			expand();
		}

		var buffer = at(index);

		buffer.waitSync	();
		buffer.setUsed	();

		return buffer;
	}

	public static class Buffers {

		public static	final	int														VERTEX_BUFFER_OUT_INDEX		= 1;
		public static	final	int														SHARING_BUFFER_INDEX		= 2;
		public static	final	int														VARYING_BUFFER_OUT_INDEX	= 4;

		private			final	MappedBufferPool										meshInfoBufferPool;
		private			final	MeshUploaderPool										meshUploaderPool;
		private			final	IDrawContextPool										drawContextPool;
		private			final	IElementPool											elementPool;
		private			final 	MappedBuffer											sharingBuffer;
		private			final 	StagingBufferPool										varyingBuffer;
		private			final	StagingBufferPool										vertexBuffer;
		private			final 	VertexArray												vertexArray;
		private			final 	Sync													sync;
		private			final 	MutableInt												sharing;
		@Getter	private	final	Map				<LayerKey, AcceleratedBufferBuilder>	builders;
		@Getter	private final	Int2ReferenceMap<ILayerStorage>							layers;
		@Getter private final	Int2ReferenceMap<ILayerFunction>						functions;
		@Getter private	final	IBufferEnvironment										environment;
		@Getter private	final	int														size;

		private 				boolean													used;
		private					VertexLayout											layout;

		public Buffers(IBufferEnvironment environment) {
			this.size				= CoreFeature.getPooledBatchingSize				();
			this.meshInfoBufferPool	= new MappedBufferPool							();
			this.meshUploaderPool	= new MeshUploaderPool							();
			this.drawContextPool	= environment.getDrawMethod().getDrawContextPool(size);
			this.elementPool		= environment.getDrawMethod().getElementPool	(size);
			this.sharingBuffer		= new MappedBuffer								(64L);
			this.varyingBuffer		= new StagingBufferPool							(size);
			this.vertexBuffer		= new StagingBufferPool							(size);
			this.vertexArray		= new VertexArray								();
			this.sync				= new Sync										();
			this.sharing			= new MutableInt								(0);
			this.builders			= new Object2ObjectLinkedOpenHashMap<>			();
			this.layers				= new Int2ReferenceOpenHashMap		<>			();
			this.functions			= new Int2ReferenceOpenHashMap		<>			();
			this.environment		= environment;

			this.used				= false;
		}

		public void reset() {
			meshInfoBufferPool	.reset		();
			meshUploaderPool	.reset		();
			drawContextPool		.reset		();
			elementPool			.reset		();
			varyingBuffer		.reset		();
			sharingBuffer		.reset		();
			vertexBuffer		.reset		();
			sharing				.setValue	(0);
			builders			.clear		();

			for (int i : layers.keySet()) {
				layers		.get(i).reset();
				functions	.get(i).reset();
			}
		}

		public void bindTransformBuffers() {
			vertexBuffer	.getBuffer().bindBase(GL_SHADER_STORAGE_BUFFER, VERTEX_BUFFER_OUT_INDEX);
			varyingBuffer	.getBuffer().bindBase(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_OUT_INDEX);
			sharingBuffer				.bindBase(GL_SHADER_STORAGE_BUFFER, SHARING_BUFFER_INDEX);
		}

		public void bindDrawBuffers() {
			vertexArray		.bind	();
			drawContextPool	.setup	();

			if (		!	environment	.getLayout()	.equals		(layout)
					||		vertexBuffer.getBuffer()	.isResized	()
					||		elementPool					.isResized	()
			) {
				layout = environment.getLayout();

				vertexBuffer.getBuffer().bind				(GL_ARRAY_BUFFER);
				vertexBuffer.getBuffer().mark				();
				elementPool				.bindBuffer			();
				environment				.setupBufferState	();
			}
		}

		public void prepare() {
			vertexBuffer	.prepare();
			varyingBuffer	.prepare();
			elementPool		.prepare();
		}

		public void unbindVertexArray() {
			vertexArray.unbind();
		}

		public MappedBuffer getMeshInfoBuffer() {
			return meshInfoBufferPool.get();
		}

		public MeshUploader getMeshUploader() {
			return meshUploaderPool.get();
		}

		public StagingBuffer getVertexBuffer() {
			return vertexBuffer.get();
		}

		public StagingBuffer getVaryingBuffer() {
			return varyingBuffer.get();
		}

		public IElementSegment getElementSegment() {
			return elementPool.get();
		}

		public IDrawContext getDrawContext() {
			return drawContextPool.get();
		}

		public IServerBuffer getElementBuffer() {
			return elementPool.getBuffer();
		}

		public long getVertexSize() {
			return environment.getVertexSize();
		}

		public int getSharing() {
			return sharing.getAndIncrement();
		}

		public long reserveSharing() {
			return sharingBuffer.reserve(4L * 4L * 4L + 4L * 4L * 3L);
		}

		public void setUsed() {
			used = true;
		}

		public void setInFlight() {
			used = false;
			sync.setSync();
		}

		protected void waitSync() {
			if (!sync.isSyncSet()) {
				return;
			}

			if (!sync.isSyncSignaled()) {
				sync.waitSync();
			}

			sync.deleteSync	();
			sync.resetSync	();
		}

		public boolean isFree() {
			if (used) {
				return false;
			}

			if (!sync.isSyncSet()) {
				return true;
			}

			if (!sync.isSyncSignaled()) {
				return false;
			}

			sync.deleteSync	();
			sync.resetSync	();

			return true;
		}

		public void delete() {
			meshUploaderPool	.delete	();
			drawContextPool		.delete	();
			elementPool			.delete	();
			sharingBuffer		.delete	();
			varyingBuffer		.delete	();
			vertexBuffer		.delete	();
			vertexArray			.delete	();
			waitSync					();
		}
	}
}
