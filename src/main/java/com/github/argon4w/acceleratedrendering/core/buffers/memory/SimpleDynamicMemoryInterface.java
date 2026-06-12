package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import com.github.argon4w.acceleratedrendering.core.utils.MemUtils;
import lombok.AllArgsConstructor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.util.function.LongSupplier;

@AllArgsConstructor
public class SimpleDynamicMemoryInterface implements IMemoryInterface {

	private final long			offset;
	private final LongSupplier	size;

	@Override
	public void putByte(long address, byte value) {
		MemoryUtil.memPutByte(address + offset, value);
	}

	@Override
	public void putShort(long address, short value) {
		MemoryUtil.memPutShort(address + offset, value);
	}

	@Override
	public void putInt(long address, int value) {
		MemoryUtil.memPutInt(address + offset, value);
	}

	@Override
	public void putInt(long address, long value) {
		MemoryUtil.memPutInt(address + offset, (int) value);
	}

	@Override
	public void putFloat(long address, float value) {
		MemoryUtil.memPutFloat(address + offset, value);
	}

	@Override
	public void putNormal(long address, float value) {
		MemUtils.putNormal(address + offset, value);
	}

	@Override
	public void putMatrix4f(long address, Matrix4f value) {
		MemUtils.putMatrix4f(address + offset, value);
	}

	@Override
	public void putMatrix3f(long address, Matrix3f value) {
		MemUtils.putMatrix3f(address + offset, value);
	}

	@Override
	public void putByteAt(long address, int at, byte value) {
		MemoryUtil.memPutByte(address + offset + at * size.getAsLong(), value);
	}

	@Override
	public void putShortAt(long address, int at, short value) {
		MemoryUtil.memPutShort(address + offset + at * size.getAsLong(), value);
	}

	@Override
	public void putIntAt(long address, int at, int value) {
		MemoryUtil.memPutInt(address + offset + at * size.getAsLong(), value);
	}

	@Override
	public void putFloatAt(long address, int at, float value) {
		MemoryUtil.memPutFloat(address + offset + at * size.getAsLong(), value);
	}

	@Override
	public void putNormalAt(long address, int at, float value) {
		MemUtils.putNormal(address + offset + at * size.getAsLong(), value);
	}

	@Override
	public void putMatrix4fAt(long address, int at, Matrix4f value) {
		MemUtils.putMatrix4f(address + offset + at * size.getAsLong(), value);
	}

	@Override
	public void putMatrix3fAt(long address, int at, Matrix3f value) {
		MemUtils.putMatrix3f(address + offset + at * size.getAsLong(), value);
	}
}
