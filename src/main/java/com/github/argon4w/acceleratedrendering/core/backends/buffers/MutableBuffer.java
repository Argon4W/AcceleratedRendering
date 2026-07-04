package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import com.github.argon4w.acceleratedrendering.core.utils.MutableSize;

public class MutableBuffer extends MutableSize implements IServerBuffer {

	private final	int				bits;

	protected		ImmutableBuffer glBuffer;

	public MutableBuffer(long initialSize, int bits) {
		super(initialSize);

		this.bits		= bits;
		this.glBuffer	= new ImmutableBuffer(this.size, bits);
	}

	@Override
	public void doExpand(long size, long bytes) {
		var newSize		= size + bytes;
		var newBuffer	= new ImmutableBuffer(newSize, bits);

		glBuffer.copyTo(newBuffer, size);
		glBuffer.delete();
		glBuffer = newBuffer;
	}

	public long map(int flags) {
		return glBuffer.map(size, flags);
	}

	public void unmap() {
		glBuffer.unmap();
	}

	@Override
	public int getBufferHandle() {
		return glBuffer.getBufferHandle();
	}

	@Override
	public void delete() {
		glBuffer.delete();
	}

	@Override
	public void bind(int target) {
		glBuffer.bind(target);
	}

	@Override
	public void bindBase(int target, int index) {
		glBuffer.bindBase(target, index);
	}

	@Override
	public void bindRange(
			int		target,
			int		index,
			long	offset,
			long	size
	) {
		glBuffer.bindRange(
				target,
				index,
				offset,
				size
		);
	}

	@Override
	public void data(
			long offset,
			long size,
			long address
	) {
		glBuffer.data(
				offset,
				size,
				address
		);
	}
}
