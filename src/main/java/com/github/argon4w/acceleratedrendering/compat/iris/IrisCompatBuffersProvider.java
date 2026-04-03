package com.github.argon4w.acceleratedrendering.compat.iris;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;

import java.util.function.Supplier;

public class IrisCompatBuffersProvider {

	public static final Supplier<IAcceleratedBufferSource> SHADOW	= () -> IrisCompatBuffers.SHADOW;
	public static final Supplier<IAcceleratedBufferSource> HAND		= () -> IrisCompatBuffers.HAND;
}
