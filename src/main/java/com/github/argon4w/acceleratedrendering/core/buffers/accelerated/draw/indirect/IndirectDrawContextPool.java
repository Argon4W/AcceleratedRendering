package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.indirect;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool.IElementSegment;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.indirect.IndirectElementBufferPool.ElementSegment;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;

import static org.lwjgl.opengl.GL46.*;

public class IndirectDrawContextPool extends SimpleResetPool<IndirectDrawContextPool.DrawContext, MappedBuffer> implements IDrawContextPool {

	public IndirectDrawContextPool(int size) {
		super(size, new MappedBuffer(20L * size));
	}

	@Override
	public void setup() {
		context.bind(GL_DRAW_INDIRECT_BUFFER);
	}

	@Override
	protected DrawContext create(MappedBuffer buffer, int i) {
		return new DrawContext(i);
	}

	@Override
	protected void reset(DrawContext drawContext) {

	}

	@Override
	protected void delete(DrawContext drawContext) {

	}

	@Override
	public void delete() {
		getContext().delete();
	}

	@Override
	public DrawContext fail() {
		expand();
		return get();
	}

	public class DrawContext implements IDrawContext {

		public static	final	int					ELEMENT_COUNT_INDEX		= 0;
		public static	final	int					ELEMENT_BUFFER_INDEX	= 6;
		public static	final	IMemoryInterface	INDIRECT_INDEX_COUNT	= new SimpleMemoryInterface(0L * 4L, 4);
		public static	final	IMemoryInterface	INDIRECT_INSTANCE_COUNT	= new SimpleMemoryInterface(1L * 4L, 4);
		public static	final	IMemoryInterface	INDIRECT_FIRST_INDEX	= new SimpleMemoryInterface(2L * 4L, 4);
		public static	final	IMemoryInterface	INDIRECT_BASE_INDEX		= new SimpleMemoryInterface(3L * 4L, 4);
		public static	final	IMemoryInterface	INDIRECT_BASE_INSTANCE	= new SimpleMemoryInterface(4L * 4L, 4);

		private			final	long				commandOffset;
		private					RenderType			renderType;

		public DrawContext(int index) {
			this.commandOffset	= index * 20L;
			this.renderType		= null;

			var address = context.reserve(20L);

			INDIRECT_INDEX_COUNT	.putInt	(address, 0);
			INDIRECT_INSTANCE_COUNT	.putInt	(address, 1);
			INDIRECT_FIRST_INDEX	.putInt	(address, 0);
			INDIRECT_BASE_INDEX		.putInt	(address, 0);
			INDIRECT_BASE_INSTANCE	.putInt	(address, 0);
		}

		@Override
		public void setupContext(
				AcceleratedBufferBuilder	builder,
				IElementSegment				elementSegment,
				IServerBuffer				elementBuffer,
				RenderType					renderType
		) {
			if (!(elementSegment instanceof ElementSegment indirect)) {
				throw new IllegalStateException("Incorrect draw method.");
			}

			this.renderType = renderType;

			var commandAddress	= context	.addressAt	(commandOffset);
			var elementOffset	= indirect	.getOffset	();
			var elementSize		= indirect	.getSize	();

			INDIRECT_INDEX_COUNT.putInt(commandAddress, 0);
			INDIRECT_FIRST_INDEX.putInt(commandAddress, elementOffset / 4);

			elementBuffer	.bindRange(GL_SHADER_STORAGE_BUFFER, ELEMENT_BUFFER_INDEX,	elementOffset,	elementSize);
			context			.bindRange(GL_ATOMIC_COUNTER_BUFFER, ELEMENT_COUNT_INDEX,	commandOffset,	4);
		}

		@Override
		public void drawElements(Mode mode) {
			glDrawElementsIndirect(
					mode.asGLMode,
					GL_UNSIGNED_INT,
					commandOffset
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
