package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class NullMemoryInterface implements IMemoryInterface {

	public static final NullMemoryInterface INSTANCE = new NullMemoryInterface();

	@Override
	public void putByte(long address, byte value) {

	}

	@Override
	public void putShort(long address, short value) {

	}

	@Override
	public void putInt(long address, int value) {

	}

	@Override
	public void putInt(long address, long value) {

	}

	@Override
	public void putFloat(long address, float value) {

	}

	@Override
	public void putNormal(long address, float value) {

	}

	@Override
	public void putMatrix4f(long address, Matrix4f value) {

	}

	@Override
	public void putMatrix3f(long address, Matrix3f value) {

	}

	@Override
	public void putByteAt(long address, int at, byte value) {

	}

	@Override
	public void putShortAt(long address, int at, short value) {

	}

	@Override
	public void putIntAt(long address, int at, int value) {

	}

	@Override
	public void putFloatAt(long address, int at, float value) {

	}

	@Override
	public void putNormalAt(long address, int at, float value) {

	}

	@Override
	public void putMatrix4fAt(long address, int at, Matrix4f value) {

	}

	@Override
	public void putMatrix3fAt(long address, int at, Matrix3f value) {

	}
}
