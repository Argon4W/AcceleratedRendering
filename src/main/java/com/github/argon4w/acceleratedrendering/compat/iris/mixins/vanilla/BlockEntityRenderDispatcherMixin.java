package com.github.argon4w.acceleratedrendering.compat.iris.mixins.vanilla;

import com.github.argon4w.acceleratedrendering.compat.iris.buffers.IrisEntityAcceleratableBufferSource;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntityRenderDispatcher.class, priority = 2000)
public class BlockEntityRenderDispatcherMixin {

	@Inject(
			method	= "render",
			at		= @At("HEAD")
	)
	public void storeOriginalBufferSource(
			BlockEntity										blockEntity,
			float											partialTick,
			PoseStack										poseStack,
			MultiBufferSource								bufferSource,
			CallbackInfo									ci,
			@Share("original") LocalRef<MultiBufferSource>	original
	) {
		original.set(bufferSource);
	}

	@ModifyVariable(
			method		= "render",
			at			= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/world/level/block/entity/BlockEntityType;isValid(Lnet/minecraft/world/level/block/state/BlockState;)Z"
			),
			allow		= 1,
			require		= 1,
			argsOnly	= true
	)
	public MultiBufferSource wrapIrisBufferSource(MultiBufferSource bufferSource, @Share("original") LocalRef<MultiBufferSource> original) {
		return new IrisEntityAcceleratableBufferSource(bufferSource, original.get());
	}
}
