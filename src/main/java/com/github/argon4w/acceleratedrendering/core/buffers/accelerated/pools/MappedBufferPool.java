package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;

public class MappedBufferPool extends SimpleResetPool<MappedBufferPool.Pooled, Void> {

	public MappedBufferPool() {
		super(16, null);
	}

	@Override
	protected Pooled create(Void context, int i) {
		return new Pooled();
	}

	@Override
	protected void reset(Pooled pooled) {
		pooled.poolReset();
	}

	@Override
	protected void delete(Pooled pooled) {
		pooled.poolDelete();
	}

	@Override
	protected Pooled fail(boolean force) {
		expand();
		return get();
	}

	public static class Pooled extends MappedBuffer {

		public Pooled() {
			super(64L);
		}

		@Override
		public void delete() {
			throw new IllegalStateException("Pooled buffers cannot be deleted directly.");
		}

		@Override
		public void reset() {
			throw new IllegalStateException("Pooled buffers cannot be reset directly.");
		}

		private void poolDelete() {
			super.delete();
		}

		private void poolReset() {
			super.reset();
		}
	}
}
