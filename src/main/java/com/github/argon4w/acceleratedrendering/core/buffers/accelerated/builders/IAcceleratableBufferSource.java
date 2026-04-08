package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;

import java.util.function.Supplier;

public interface IAcceleratableBufferSource {

	Supplier<IAcceleratedBufferSource>	getBoundAcceleratedBufferSource	();
	boolean								isBufferSourceAcceleratable		();
	void								bindAcceleratedBufferSource		(Supplier<IAcceleratedBufferSource> bufferSource);
}
