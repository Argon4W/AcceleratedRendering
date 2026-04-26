package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools;

import com.github.argon4w.acceleratedrendering.core.backends.GLConstants;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableLong;

import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

public class StagingBufferPool extends SimpleResetPool<StagingBufferPool.StagingBuffer, Void> {

	@Getter private	final MutableBuffer buffer;
	private			final MutableLong	bufferSegments;
	private			final MutableLong	bufferOutSize;
	private			final MutableLong	bufferOutUsedSize;

	public StagingBufferPool(int size) {
		super(size, null);

		this.buffer				= new MutableBuffer	(64L * size, GL_DYNAMIC_STORAGE_BIT);
		this.bufferSegments		= new MutableLong	(0L);
		this.bufferOutSize		= new MutableLong	(64L * size);
		this.bufferOutUsedSize	= new MutableLong	(0L);
	}

	public void prepare() {
		buffer.resizeTo(bufferOutSize.getValue());
	}

	@Override
	public void delete() {
		buffer	.delete();
		super	.delete();
	}

	@Override
	public void reset() {
		bufferOutUsedSize	.setValue	(0L);
		bufferSegments		.setValue	(0L);
		super				.reset		();
	}

	@Override
	protected StagingBuffer create(Void context, int i) {
		return new StagingBuffer();
	}

	@Override
	protected void reset(StagingBuffer stagingBuffer) {
		stagingBuffer.poolReset();
	}

	@Override
	protected void delete(StagingBuffer stagingBuffer) {
		stagingBuffer.poolDelete();
	}

	@Override
	public boolean test(StagingBuffer stagingBuffer) {
		return bufferOutUsedSize.addAndGet(stagingBuffer.getSize()) <= GLConstants.MAX_SHADER_STORAGE_BLOCK_SIZE;
	}

	@Getter
	public class StagingBuffer extends MappedBuffer {

		private long offset;

		public StagingBuffer() {
			super(64L);
			this.offset = -1;
		}

		@Override
		public void onExpand(long bytes) {
			bufferOutSize		.add(bytes);
			bufferOutUsedSize	.add(bytes);
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

		public void allocateOffset() {
			offset = bufferSegments.getAndAdd(position);
		}
	}
}
