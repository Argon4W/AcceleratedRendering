package com.github.argon4w.acceleratedrendering.core.backends.buffers;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46.*;

public class ImmutableBuffer implements IServerBuffer {

	protected final int bufferHandle;

	public ImmutableBuffer(long size, int bits) {
		this.bufferHandle = glCreateBuffers();

		glNamedBufferStorage(
				this.bufferHandle,
				size,
				bits
		);
	}

	public void copyTo(IServerBuffer buffer, long size) {
		glCopyNamedBufferSubData(
				bufferHandle,
				buffer.getBufferHandle(),
				0,
				0,
				size
		);
	}

	public long map(long length, int bits) {
		return nglMapNamedBufferRange(
				bufferHandle,
				0L,
				length,
				bits
		);
	}

	public void unmap() {
		glUnmapNamedBuffer(bufferHandle);
	}

	@Override
	public int getBufferHandle() {
		return bufferHandle;
	}

	@Override
	public void delete() {
		glDeleteBuffers(bufferHandle);
	}

	@Override
	public void bind(int target) {
		glBindBuffer(target, bufferHandle);
	}

	@Override
	public void bindBase(int target, int index) {
		glBindBufferBase(
				target,
				index,
				bufferHandle
		);
	}

	@Override
	public void bindRange(
			int		target,
			int		index,
			long	offset,
			long	size
	) {
		glBindBufferRange(
				target,
				index,
				bufferHandle,
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
		nglNamedBufferSubData(
				bufferHandle,
				offset,
				size,
				address
		);
	}
}
