package com.github.argon4w.acceleratedrendering.core.backends.states.buffers;

import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import com.github.argon4w.acceleratedrendering.core.backends.states.buffers.cache.BlockBufferBindingCacheType;
import com.github.argon4w.acceleratedrendering.core.backends.states.buffers.cache.IBlockBufferBindingCache;
import net.minecraft.client.gui.GuiGraphics;

import static org.lwjgl.opengl.GL46.*;

public class SimpleBlockBufferBindingState implements IBindingState {

	private final IBlockBufferBindingCache	bindingCache;
	private final int						bindingRange;
	private final int						bindingBlock;
	private final int						bufferParam;
	private final int						offsetParam;
	private final int						sizeParam;

	public SimpleBlockBufferBindingState(
			BlockBufferBindingCacheType	type,
			int							bindingRange,
			int							bindingBlock,
			int							bufferParam,
			int							offsetParam,
			int							sizeParam
	) {
		this.bindingRange	= bindingRange;
		this.bindingCache	= type.create(this.bindingRange);

		this.bindingBlock	= bindingBlock;
		this.bufferParam	= bufferParam;
		this.offsetParam	= offsetParam;
		this.sizeParam		= sizeParam;
	}

	@Override
	public void capture(GuiGraphics graphics) {
		for (var bindingPoint = 0; bindingPoint < bindingRange; bindingPoint++) {
			bindingCache.setup(
					bindingPoint,
					glGetIntegeri(bufferParam,	bindingPoint),
					glGetIntegeri(offsetParam,	bindingPoint),
					glGetIntegeri(sizeParam,	bindingPoint)
			);
		}
	}

	@Override
	public void restore() {
		for (var bindingPoint = 0; bindingPoint < bindingRange; bindingPoint ++) {
			var buffer	= bindingCache.getBuffer(bindingPoint);
			var offset	= bindingCache.getOffset(bindingPoint);
			var size	= bindingCache.getSize	(bindingPoint);

			if (		offset	== 0
					&&	size	== 0
			) {
				glBindBufferBase(
						bindingBlock,
						bindingPoint,
						buffer
				);
			} else {
				glBindBufferRange(
						bindingBlock,
						bindingPoint,
						buffer,
						offset,
						size
				);
			}
		}
	}

	@Override
	public void delete() {
		bindingCache.delete();
	}
}
