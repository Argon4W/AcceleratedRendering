package com.github.argon4w.acceleratedrendering.core.buffers.memory;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public interface IMemoryInterface {

	void putByte		(long address, byte		value);
	void putShort		(long address, short	value);
	void putInt			(long address, int		value);
	void putInt			(long address, long		value);
	void putFloat		(long address, float	value);
	void putNormal		(long address, float	value);
	void putMatrix4f	(long address, Matrix4f	value);
	void putMatrix3f	(long address, Matrix3f	value);
	void putByteAt		(long address, int		at, byte		value);
	void putShortAt		(long address, int		at, short		value);
	void putIntAt		(long address, int		at, int			value);
	void putFloatAt		(long address, int		at, float		value);
	void putNormalAt	(long address, int		at, float		value);
	void putMatrix4fAt	(long address, int		at, Matrix4f	value);
	void putMatrix3fAt	(long address, int		at, Matrix3f	value);
}
