package com.github.argon4w.acceleratedrendering.compat.vanilla.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

	@Inject(
			method	= "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at		= @At("HEAD")
	)
	public void initLayer(
			LivingEntity				entity,
			float						entityYaw,
			float						partialTicks,
			PoseStack					poseStack,
			MultiBufferSource			buffer,
			int							packedLight,
			CallbackInfo				ci,
			@Share("layer") LocalIntRef	layer
	) {
		layer.set(CoreFeature.getDefaultLayer() + 1);
	}

	@SuppressWarnings	("rawtypes")
	@WrapOperation		(
			method	= "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"
			)
	)
	public void wrapRenderLayer(
			RenderLayer					instance,
			PoseStack					poseStack,
			MultiBufferSource			bufferSource,
			int							packedLight,
			Entity						entity,
			float						limbSwing,
			float						limbSwingAmount,
			float						partialTick,
			float						ageInTicks,
			float						netHeadYaw,
			float						headPitch,
			Operation<Void>				original,
			@Share("layer") LocalIntRef	layer
	) {
		if (		!CoreFeature.isLoaded			()
				||	!ModsFeature.isEnabled			()
				||	!ModsFeature.shouldFixVanilla	()
		) {
			original.call(
					instance,
					poseStack,
					bufferSource,
					packedLight,
					entity,
					limbSwing,
					limbSwingAmount,
					partialTick,
					ageInTicks,
					netHeadYaw,
					headPitch
			);
			return;
		}

		CoreFeature.forceSetDefaultLayer(layer.get());

		original.call(
				instance,
				poseStack,
				bufferSource,
				packedLight,
				entity,
				limbSwing,
				limbSwingAmount,
				partialTick,
				ageInTicks,
				netHeadYaw,
				headPitch
		);

		CoreFeature.resetDefaultLayer();

		layer.set(layer.get() + 1);
	}
}
