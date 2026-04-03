package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import net.minecraft.client.renderer.MultiBufferSource;

public class BufferSourceExtension {

	public static IAcceleratableBufferSource getAcceleratable(MultiBufferSource in) {
		return (IAcceleratableBufferSource) in;
	}
}
