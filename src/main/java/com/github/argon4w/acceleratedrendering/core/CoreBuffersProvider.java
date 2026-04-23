package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.core.buffers.EmptyAcceleratedBufferSources;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.RenderBuffers;

import java.util.function.Supplier;

@ExtensionMethod(BufferSourceExtension.class)
public class CoreBuffersProvider {

	public static final Supplier<IAcceleratedBufferSource> EMPTY	= () -> EmptyAcceleratedBufferSources	.INSTANCE;
	public static final Supplier<IAcceleratedBufferSource> CORE		= () -> CoreBuffers						.CORE;
	public static final Supplier<IAcceleratedBufferSource> OUTLINE	= () -> CoreBuffers						.OUTLINE;

	public static void bindAcceleratedBufferSources(RenderBuffers renderBuffers) {
		renderBuffers.bufferSource			().getAcceleratable().bindAcceleratedBufferSource(CoreBuffersProvider.CORE);
		renderBuffers.crumblingBufferSource	().getAcceleratable().bindAcceleratedBufferSource(CoreBuffersProvider.CORE);
		renderBuffers.outlineBufferSource	().getAcceleratable().bindAcceleratedBufferSource(CoreBuffersProvider.OUTLINE);
	}
}
