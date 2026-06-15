package com.github.argon4w.acceleratedrendering.core.mixins.buffers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratableBufferSource;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(MultiBufferSource.class)
public interface MultiBufferSourceMixin extends IAcceleratableBufferSource {

	@Override
	default Supplier<IAcceleratedBufferSource> getBoundAcceleratedBufferSource() {
		return () -> null;
	}

	@Override
	default boolean isBufferSourceAcceleratable() {
		return false;
	}

	@Override
	default void bindAcceleratedBufferSource(Supplier<IAcceleratedBufferSource> bufferSource) {

	}
}
