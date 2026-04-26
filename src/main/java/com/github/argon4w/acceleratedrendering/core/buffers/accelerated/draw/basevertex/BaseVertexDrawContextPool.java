package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.basevertex;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool.IElementSegment;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderSystem.AutoStorageIndexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;

import static org.lwjgl.opengl.GL46.*;

public class BaseVertexDrawContextPool extends SimpleResetPool<BaseVertexDrawContextPool.DrawContext, Void> implements IDrawContextPool {

	public BaseVertexDrawContextPool(int size) {
		super(size, null);
	}

	@Override
	public void setup() {

	}

	@Override
	protected DrawContext create(Void buffer, int i) {
		return new DrawContext();
	}

	@Override
	protected void reset(DrawContext drawContext) {

	}

	@Override
	protected void delete(DrawContext drawContext) {

	}

	@Override
	public void delete() {

	}

	@Override
	public DrawContext fail() {
		expand();
		return get();
	}

	public static class DrawContext implements IDrawContext {

		private RenderType				renderType;
		private AutoStorageIndexBuffer	elementBuffer;
		private int						baseVertex;
		private int						count;

		public DrawContext() {
			this.baseVertex	= -1;
			this.count		= -1;
		}

		@Override
		public void setupContext(
				AcceleratedBufferBuilder	builder,
				IElementSegment				elementSegment,
				IServerBuffer				elementBuffer,
				RenderType					renderType
		) {
			this.renderType		= renderType;
			this.elementBuffer	= RenderSystem			.getSequentialBuffer	(renderType.mode);
			this.baseVertex		= (int) builder			.getVertexCountOffset	();
			this.count			= (int) elementSegment	.getCount				();
		}

		@Override
		public void drawElements(Mode mode) {
			elementBuffer.bind(count);

			glDrawElementsBaseVertex(
					mode.asGLMode,
					count,
					elementBuffer.type().asGLType,
					0L,
					baseVertex
			);
		}

		@Override
		public int compareTo(IDrawContext that) {
			return Boolean.compare(
					this.getRenderType().sortOnUpload,
					that.getRenderType().sortOnUpload
			);
		}

		@Override
		public RenderType getRenderType() {
			return renderType;
		}
	}
}
