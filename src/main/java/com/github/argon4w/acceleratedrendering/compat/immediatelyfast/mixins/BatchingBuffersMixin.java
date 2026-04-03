package com.github.argon4w.acceleratedrendering.compat.immediatelyfast.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreBuffersProvider;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.raphimc.immediatelyfast.feature.batching.BatchingBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@ExtensionMethod(BufferSourceExtension	.class)
@Mixin			(BatchingBuffers		.class)
public class BatchingBuffersMixin {

	@ModifyReturnValue(
			method	= "getHudBatchingVertexConsumers",
			at		= @At("RETURN")
	)
	private static MultiBufferSource.BufferSource bindAcceleratableBufferSourceCore(MultiBufferSource.BufferSource original) {
		original
				.getAcceleratable			()
				.bindAcceleratedBufferSource(CoreBuffersProvider.CORE);

		return original;
	}
}
