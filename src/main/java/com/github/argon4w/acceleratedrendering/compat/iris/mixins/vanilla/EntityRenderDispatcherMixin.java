package com.github.argon4w.acceleratedrendering.compat.iris.mixins.vanilla;

import com.github.argon4w.acceleratedrendering.compat.iris.buffers.IrisEntityAcceleratableBufferSource;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderDispatcher.class, priority = 2000)
public class EntityRenderDispatcherMixin {

	@Inject(
			method	= "render",
			at		= @At("HEAD")
	)
	public void storeOriginalBufferSource(
			Entity											entity,
			double											posX,
			double											posY,
			double											posZ,
			float											rotationYaw,
			float											partialTicks,
			PoseStack										poseStack,
			MultiBufferSource								buffer,
			int												packedLight,
			CallbackInfo									ci,
			@Share("original") LocalRef<MultiBufferSource>	original
	) {
		original.set(buffer);
	}

	@ModifyVariable(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
					shift	= At.Shift.AFTER
			),
			allow		= 1,
			require		= 1,
			argsOnly	= true
	)
	public MultiBufferSource wrapIrisBufferSource(MultiBufferSource bufferSource, @Share("original") LocalRef<MultiBufferSource> original) {
		return new IrisEntityAcceleratableBufferSource(bufferSource, original.get());
	}
}
