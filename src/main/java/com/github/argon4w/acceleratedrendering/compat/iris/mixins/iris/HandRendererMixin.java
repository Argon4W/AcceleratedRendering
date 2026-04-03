package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffers;
import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffersProvider;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.CoreStates;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import lombok.experimental.ExtensionMethod;
import net.irisshaders.batchedentityrendering.impl.FullyBufferedMultiBufferSource;
import net.irisshaders.iris.pathways.HandRenderer;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@ExtensionMethod(BufferSourceExtension	.class)
@Mixin			(HandRenderer			.class)
public class HandRendererMixin {


	@Shadow
	@Final
	private FullyBufferedMultiBufferSource bufferSource;

	@Inject(
			method	= "<init>",
			at		= @At("TAIL")
	)
	public void bindAcceleratedBufferSourceHand(CallbackInfo ci) {
		bufferSource
				.getAcceleratable			()
				.bindAcceleratedBufferSource(IrisCompatBuffersProvider.HAND);
	}

	@Inject(
			method	= "renderSolid",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
					shift	= At.Shift.BEFORE
			)
	)
	public void startRenderSolidFast(
			Matrix4fc				modelMatrix,
			float					tickDelta,
			Camera					camera,
			GameRenderer			gameRenderer,
			WorldRenderingPipeline	pipeline,
			CallbackInfo			ci
	) {
		CoreFeature.setRenderingHand();
	}

	@Inject(
			method	= "renderSolid",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
					shift	= At.Shift.AFTER
			)
	)
	public void stopRenderSolidFast(
			Matrix4fc				modelMatrix,
			float					tickDelta,
			Camera					camera,
			GameRenderer			gameRenderer,
			WorldRenderingPipeline	pipeline,
			CallbackInfo			ci
	) {
		CoreFeature									.resetRenderingHand	();

		CoreStates									.recordBuffers		();
		IrisCompatBuffers.ENTITY_HAND				.prepareBuffers		();
		IrisCompatBuffers.BLOCK_HAND				.prepareBuffers		();
		IrisCompatBuffers.POS_HAND					.prepareBuffers		();
		IrisCompatBuffers.POS_COLOR_HAND			.prepareBuffers		();
		IrisCompatBuffers.POS_TEX_HAND				.prepareBuffers		();
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.prepareBuffers		();
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.prepareBuffers		();
		CoreStates									.restoreBuffers		();

		IrisCompatBuffers.ENTITY_HAND				.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.BLOCK_HAND				.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_HAND					.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_COLOR_HAND			.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_TEX_HAND				.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.drawBuffers		(LayerDrawType.ALL);

		IrisCompatBuffers.ENTITY_HAND				.clearBuffers		();
		IrisCompatBuffers.BLOCK_HAND				.clearBuffers		();
		IrisCompatBuffers.POS_HAND					.clearBuffers		();
		IrisCompatBuffers.POS_COLOR_HAND			.clearBuffers		();
		IrisCompatBuffers.POS_TEX_HAND				.clearBuffers		();
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.clearBuffers		();
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.clearBuffers		();
	}

	@Inject(
			method	= "renderTranslucent",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
					shift	= At.Shift.BEFORE
			)
	)
	public void startRenderTranslucentFast(
			Matrix4fc				modelMatrix,
			float					tickDelta,
			Camera					camera,
			GameRenderer			gameRenderer,
			WorldRenderingPipeline	pipeline,
			CallbackInfo			ci
	) {
		CoreFeature.setRenderingHand();
	}

	@Inject(
			method	= "renderTranslucent",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
					shift	= At.Shift.AFTER
			)
	)
	public void stopRenderTranslucentFast(
			Matrix4fc				modelMatrix,
			float					tickDelta,
			Camera					camera,
			GameRenderer			gameRenderer,
			WorldRenderingPipeline	pipeline,
			CallbackInfo			ci
	) {
		CoreFeature									.resetRenderingHand	();

		CoreStates									.recordBuffers		();
		IrisCompatBuffers.ENTITY_HAND				.prepareBuffers		();
		IrisCompatBuffers.BLOCK_HAND				.prepareBuffers		();
		IrisCompatBuffers.POS_HAND					.prepareBuffers		();
		IrisCompatBuffers.POS_COLOR_HAND			.prepareBuffers		();
		IrisCompatBuffers.POS_TEX_HAND				.prepareBuffers		();
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.prepareBuffers		();
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.prepareBuffers		();
		CoreStates									.restoreBuffers		();

		IrisCompatBuffers.ENTITY_HAND				.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.BLOCK_HAND				.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_HAND					.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_COLOR_HAND			.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_TEX_HAND				.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.drawBuffers		(LayerDrawType.ALL);
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.drawBuffers		(LayerDrawType.ALL);

		IrisCompatBuffers.ENTITY_HAND				.clearBuffers		();
		IrisCompatBuffers.BLOCK_HAND				.clearBuffers		();
		IrisCompatBuffers.POS_HAND					.clearBuffers		();
		IrisCompatBuffers.POS_COLOR_HAND			.clearBuffers		();
		IrisCompatBuffers.POS_TEX_HAND				.clearBuffers		();
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.clearBuffers		();
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.clearBuffers		();
	}
}
