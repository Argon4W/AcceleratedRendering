package com.github.argon4w.acceleratedrendering.core.mixins.compatibility;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {

	@Inject(
			method	= "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
			at		= @At("HEAD"),
			remap	= false
	)
	public void disableParticleAcceleration(
			PoseStack						poseStack,
			MultiBufferSource.BufferSource	buffer,
			LightTexture					lightTexture,
			Camera							activeRenderInfo,
			float							partialTicks,
			Frustum							clippingHelper,
			CallbackInfo					ci
	) {
		if (!CoreFeature.isLoaded()) {
			return;
		}

		AcceleratedEntityRenderingFeature	.useVanillaPipeline();
		AcceleratedItemRenderingFeature		.useVanillaPipeline();
		AcceleratedTextRenderingFeature		.useVanillaPipeline();
	}

	@Inject(
			method	= "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V",
			at		= @At("RETURN"),
			remap	= false
	)
	public void resetParticleAcceleration(
			PoseStack						poseStack,
			MultiBufferSource.BufferSource	buffer,
			LightTexture					lightTexture,
			Camera							activeRenderInfo,
			float							partialTicks,
			Frustum							clippingHelper,
			CallbackInfo					ci
	) {
		if (!CoreFeature.isLoaded()) {
			return;
		}

		AcceleratedEntityRenderingFeature	.resetPipeline();
		AcceleratedItemRenderingFeature		.resetPipeline();
		AcceleratedTextRenderingFeature		.resetPipeline();
	}
}
