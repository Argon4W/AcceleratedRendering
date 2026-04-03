package com.github.argon4w.acceleratedrendering.core.mixins.buffers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratableBufferSource;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.Supplier;

@ExtensionMethod(BufferSourceExtension.class)
@Mixin			(OutlineBufferSource	.class)
public class OutlineBufferSourceMixin implements IAcceleratableBufferSource {

	@Shadow @Final private MultiBufferSource.BufferSource outlineBufferSource;

	@Unique
	@Override
	public void bindAcceleratedBufferSource(Supplier<IAcceleratedBufferSource> supplier) {
		outlineBufferSource
				.getAcceleratable			()
				.bindAcceleratedBufferSource(supplier);
	}

	@Unique
	@Override
	public IAcceleratedBufferSource getBoundAcceleratedBufferSource() {
		return outlineBufferSource
				.getAcceleratable				()
				.getBoundAcceleratedBufferSource();
	}
}
