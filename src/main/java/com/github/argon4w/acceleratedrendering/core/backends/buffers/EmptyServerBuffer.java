package com.github.argon4w.acceleratedrendering.core.backends.buffers;

public class EmptyServerBuffer implements IServerBuffer {

	public static final EmptyServerBuffer INSTANCE = new EmptyServerBuffer();

	@Override
	public int getBufferHandle() {
		return 0;
	}

	@Override
	public void delete() {

	}

	@Override
	public void bind(int target) {

	}

	@Override
	public void bindBase(int target, int index) {

	}

	@Override
	public void bindRange(
			int		target,
			int		index,
			long	offset,
			long	size
	) {

	}

	@Override
	public void data(
			long offset,
			long size,
			long address
	) {

	}
}
