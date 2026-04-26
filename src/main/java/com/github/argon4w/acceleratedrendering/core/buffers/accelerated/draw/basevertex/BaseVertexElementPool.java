package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.basevertex;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.EmptyServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;

public class BaseVertexElementPool extends SimpleResetPool<BaseVertexElementPool.ElementSegment, Void> implements IElementPool {

	public BaseVertexElementPool(int size) {
		super(size, null);
	}

	@Override
	public void bindBuffer() {

	}

	@Override
	public void prepare() {

	}

	@Override
	public void delete() {

	}

	@Override
	protected ElementSegment create(Void value, int i) {
		return new ElementSegment();
	}

	@Override
	protected void reset(ElementSegment elementSegment) {
		elementSegment.reset();
	}

	@Override
	protected void delete(ElementSegment elementSegment) {

	}

	@Override
	public IServerBuffer getBuffer() {
		return EmptyServerBuffer.INSTANCE;
	}

	@Override
	public boolean isResized() {
		return false;
	}

	public static class ElementSegment implements IElementSegment {

		private long count;

		public ElementSegment() {
			this.count = 0L;
		}

		private void reset() {
			count = 0L;
		}

		@Override
		public void setup() {

		}

		@Override
		public long getCount() {
			return count;
		}

		@Override
		public void count(int count) {
			this.count += count;
		}
	}
}
