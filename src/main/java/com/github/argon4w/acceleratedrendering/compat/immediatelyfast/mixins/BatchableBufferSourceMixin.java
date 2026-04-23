package com.github.argon4w.acceleratedrendering.compat.immediatelyfast.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratableBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.RenderType;
import net.raphimc.immediatelyfast.feature.core.BatchableBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(BatchableBufferSource	.class)
public abstract class BatchableBufferSourceMixin implements IAcceleratableBufferSource {

	@ModifyReturnValue(
			method	= "getBuffer",
			at		= @At("RETURN")
	)
	public VertexConsumer initAcceleration(VertexConsumer original, RenderType renderType) {
		return original
				.getHolder			()
				.initAcceleration	(renderType, getBoundAcceleratedBufferSource());
	}
}
