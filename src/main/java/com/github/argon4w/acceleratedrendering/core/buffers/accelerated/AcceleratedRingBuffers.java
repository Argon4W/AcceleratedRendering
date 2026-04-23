package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.Sync;
import com.github.argon4w.acceleratedrendering.core.backends.VertexArray;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerKey;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.functions.ILayerFunction;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage.ILayerStorage;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.DrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.ElementBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.utils.LoopResetPool;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Map;

import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL40.GL_DRAW_INDIRECT_BUFFER;
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

		var buffer = at	(index);
		buffer.waitSync	();
		buffer.setUsed	();

		return buffer;
	}

	public static class Buffers {

		public static	final	int														VERTEX_BUFFER_OUT_INDEX		= 1;
		public static	final	int														SHARING_BUFFER_INDEX		= 2;
		public static	final	int														VARYING_BUFFER_OUT_INDEX	= 4;
		public static	final	int														ELEMENT_BUFFER_INDEX		= 6;

		private			final	MeshUploaderPool										meshUploaderPool;
		private			final 	DrawContextPool											drawContextPool;
		private			final 	ElementBufferPool										elementBufferPool;
		private			final 	MappedBuffer											sharingBuffer;
		private			final 	StagingBufferPool										varyingBuffer;
		private			final	StagingBufferPool										vertexBuffer;
		private			final 	VertexArray												vertexArray;
		private			final 	Sync													sync;
		private			final 	MutableInt												sharing;
		@Getter	private	final	Map				<LayerKey, AcceleratedBufferBuilder>	builders;
		@Getter	private final	Int2ReferenceMap<ILayerStorage>							layers;
		@Getter private final	Int2ReferenceMap<ILayerFunction>						functions;
		@Getter private	final	IBufferEnvironment										bufferEnvironment;
		@Getter private	final	int														size;

		private 				boolean													used;
		private					VertexLayout											layout;

		public Buffers(IBufferEnvironment bufferEnvironment) {
			this.size				= CoreFeature.getPooledBatchingSize		();
			this.meshUploaderPool	= new MeshUploaderPool					();
			this.drawContextPool	= new DrawContextPool					(size);
			this.elementBufferPool	= new ElementBufferPool					(size);
			this.sharingBuffer		= new MappedBuffer						(64L);
			this.varyingBuffer		= new StagingBufferPool					(size);
			this.vertexBuffer		= new StagingBufferPool					(size);
			this.vertexArray		= new VertexArray						();
			this.sync				= new Sync								();
			this.sharing			= new MutableInt						(0);
			this.builders			= new Object2ObjectLinkedOpenHashMap<>	();
			this.layers				= new Int2ReferenceOpenHashMap		<>	();
			this.functions			= new Int2ReferenceOpenHashMap		<>	();
			this.bufferEnvironment	= bufferEnvironment;

			this.used				= false;
		}

		public void reset() {
			meshUploaderPool	.reset		();
			drawContextPool		.reset		();
			elementBufferPool	.reset		();
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
			vertexBuffer	.getBufferOut()	.bindBase(GL_SHADER_STORAGE_BUFFER, VERTEX_BUFFER_OUT_INDEX);
			varyingBuffer	.getBufferOut()	.bindBase(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_OUT_INDEX);
			sharingBuffer					.bindBase(GL_SHADER_STORAGE_BUFFER, SHARING_BUFFER_INDEX);
		}

		public void bindElementBuffer(ElementBufferPool.ElementSegment elementSegment) {
			elementBufferPool
					.getElementBufferOut()
					.bindRange			(
							GL_SHADER_STORAGE_BUFFER,
							ELEMENT_BUFFER_INDEX,
							elementSegment.getOffset(),
							elementSegment.getSize	()
					);
		}

		public void bindDrawBuffers() {
			vertexArray					.bind();
			drawContextPool.getContext().bind(GL_DRAW_INDIRECT_BUFFER);

			if (		!	bufferEnvironment	.getLayout			().equals	(layout)
					||		vertexBuffer		.getBufferOut		().isResized()
					||		elementBufferPool	.getElementBufferOut().isResized()
			) {
				layout = bufferEnvironment					.getLayout			();
				elementBufferPool	.getElementBufferOut()	.bind				(GL_ELEMENT_ARRAY_BUFFER);
				elementBufferPool	.getElementBufferOut()	.resetResized		();
				vertexBuffer		.getBufferOut()			.bind				(GL_ARRAY_BUFFER);
				vertexBuffer		.getBufferOut()			.resetResized		();
				bufferEnvironment							.setupBufferState	();
			}
		}

		public void prepare() {
			vertexBuffer		.prepare();
			varyingBuffer		.prepare();
			elementBufferPool	.prepare();
		}

		public void unbindVertexArray() {
			vertexArray.unbind();
		}

		public MeshUploaderPool.MeshUploader getMeshUploader() {
			return meshUploaderPool.get();
		}

		public StagingBufferPool.StagingBuffer getVertexBuffer() {
			return vertexBuffer.get();
		}

		public StagingBufferPool.StagingBuffer getVaryingBuffer() {
			return varyingBuffer.get();
		}

		public ElementBufferPool.ElementSegment getElementSegment() {
			return elementBufferPool.get();
		}

		public DrawContextPool.DrawContext getDrawContext() {
			return drawContextPool.get();
		}

		public long getVertexSize() {
			return bufferEnvironment.getVertexSize();
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
			elementBufferPool	.delete	();
			sharingBuffer		.delete	();
			varyingBuffer		.delete	();
			vertexBuffer		.delete	();
			vertexArray			.delete	();
			waitSync					();
		}
	}
}
