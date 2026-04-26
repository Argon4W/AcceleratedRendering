package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.indirect;

import com.github.argon4w.acceleratedrendering.core.backends.GLConstants;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MutableBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.utils.MutableSize;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleResetPool;
import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableLong;

import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL44.GL_DYNAMIC_STORAGE_BIT;

public class IndirectElementBufferPool extends SimpleResetPool<IndirectElementBufferPool.ElementSegment, Void> implements IElementPool {

	private final MutableBuffer	elementBufferOut;
	private final MutableLong	elementBufferSegments;
	private final MutableLong	elementBufferOutSize;
	private final MutableLong	elementBufferOutUsedSize;

	public IndirectElementBufferPool(int size) {
		super(size, null);

		this.elementBufferOut			= new MutableBuffer	(64L * size, GL_DYNAMIC_STORAGE_BIT);
		this.elementBufferSegments		= new MutableLong	(0L);
		this.elementBufferOutSize		= new MutableLong	(64L * size);
		this.elementBufferOutUsedSize	= new MutableLong	(64L * size);
	}

	@Override
	public void bindBuffer() {
		elementBufferOut.bind(GL_ELEMENT_ARRAY_BUFFER);
		elementBufferOut.mark();
	}

	@Override
	public void reset() {
		elementBufferOutUsedSize.setValue	(0L);
		elementBufferSegments	.setValue	(0L);

		super.reset();
	}

	@Override
	public void prepare() {
		elementBufferOut.resizeTo(elementBufferOutSize.getValue());
	}

	@Override
	public void delete() {
		elementBufferOut.delete();
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
	public boolean test(ElementSegment elementSegment) {
		return elementBufferOutUsedSize.addAndGet(elementSegment.getSize()) <= GLConstants.MAX_SHADER_STORAGE_BLOCK_SIZE;
	}

	@Override
	public IServerBuffer getBuffer() {
		return elementBufferOut;
	}

	@Override
	public boolean isResized() {
		return elementBufferOut.isResized();
	}

	@Getter
	public class ElementSegment extends MutableSize implements IElementSegment {

		private long count;
		private long bytes;
		private long offset;

		public ElementSegment() {
			super(64L);
			this.count	= 0L;
			this.bytes	= 0L;
			this.offset	= -1L;
		}

		private void reset() {
			count = 0L;
			bytes = 0L;
		}

		@Override
		public void onExpand(long bytes) {
			elementBufferOutSize	.add(bytes);
			elementBufferOutUsedSize.add(bytes);
		}

		@Override
		public void setup() {
			offset = elementBufferSegments.getAndAdd(size);
		}

		@Override
		public long getCount() {
			return count;
		}

		@Override
		public void count(int count) {
			this.count += count;
			this.bytes += count * 4L;

			if (bytes > size) {
				resize(bytes);
			}
		}
	}
}
