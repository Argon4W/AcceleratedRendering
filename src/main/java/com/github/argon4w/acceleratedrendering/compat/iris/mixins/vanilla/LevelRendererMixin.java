package com.github.argon4w.acceleratedrendering.compat.iris.mixins.vanilla;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.CoreStates;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
		value		= LevelRenderer.class,
		priority	= 999
)
public class LevelRendererMixin {

	@Inject(
			method	= "renderLevel",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V",
					ordinal = 1
			)
	)
	public void drawIrisAllCoreBuffers(
			PoseStack		poseStack,
			float			partialTick,
			long			finishNanoTime,
			boolean			renderBlockOutline,
			Camera			camera,
			GameRenderer	gameRenderer,
			LightTexture	lightTexture,
			Matrix4f		projectionMatrix,
			CallbackInfo	ci
	) {
		if (!CoreFeature.isLoaded()) {
			return;
		}

		CoreStates						.recordBuffers	();
		CoreBuffers.ENTITY				.prepareBuffers	();
		CoreBuffers.BLOCK				.prepareBuffers	();
		CoreBuffers.POS					.prepareBuffers	();
		CoreBuffers.POS_COLOR			.prepareBuffers	();
		CoreBuffers.POS_TEX				.prepareBuffers	();
		CoreBuffers.POS_TEX_COLOR		.prepareBuffers	();
		CoreBuffers.POS_COLOR_TEX_LIGHT	.prepareBuffers	();
		CoreStates						.restoreBuffers	();

		CoreBuffers.ENTITY				.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.BLOCK				.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS					.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_COLOR			.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_TEX				.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_TEX_COLOR		.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_COLOR_TEX_LIGHT	.drawBuffers	(LayerDrawType.ALL);

		CoreBuffers.ENTITY				.clearBuffers	();
		CoreBuffers.BLOCK				.clearBuffers	();
		CoreBuffers.POS					.clearBuffers	();
		CoreBuffers.POS_COLOR			.clearBuffers	();
		CoreBuffers.POS_TEX				.clearBuffers	();
		CoreBuffers.POS_TEX_COLOR		.clearBuffers	();
		CoreBuffers.POS_COLOR_TEX_LIGHT	.clearBuffers	();
	}

	@Inject(
			method	= "renderLevel",
			at		= @At(
					value	= "CONSTANT",
					args	= "stringValue=translucent",
					ordinal	= 1
			)
	)
	public void drawIrisOpaqueCoreBuffers(
			PoseStack		poseStack,
			float			partialTick,
			long			finishNanoTime,
			boolean			renderBlockOutline,
			Camera			camera,
			GameRenderer	gameRenderer,
			LightTexture	lightTexture,
			Matrix4f		projectionMatrix,
			CallbackInfo	ci
	) {
		if (!CoreFeature.isLoaded()) {
			return;
		}

		CoreStates						.recordBuffers	();
		CoreBuffers.ENTITY				.prepareBuffers	();
		CoreBuffers.BLOCK				.prepareBuffers	();
		CoreBuffers.POS					.prepareBuffers	();
		CoreBuffers.POS_COLOR			.prepareBuffers	();
		CoreBuffers.POS_TEX				.prepareBuffers	();
		CoreBuffers.POS_TEX_COLOR		.prepareBuffers	();
		CoreBuffers.POS_COLOR_TEX_LIGHT	.prepareBuffers	();
		CoreStates						.restoreBuffers	();

		CoreBuffers.ENTITY				.drawBuffers	(LayerDrawType.OPAQUE);
		CoreBuffers.BLOCK				.drawBuffers	(LayerDrawType.OPAQUE);
		CoreBuffers.POS					.drawBuffers	(LayerDrawType.OPAQUE);
		CoreBuffers.POS_COLOR			.drawBuffers	(LayerDrawType.OPAQUE);
		CoreBuffers.POS_TEX				.drawBuffers	(LayerDrawType.OPAQUE);
		CoreBuffers.POS_TEX_COLOR		.drawBuffers	(LayerDrawType.OPAQUE);
		CoreBuffers.POS_COLOR_TEX_LIGHT	.drawBuffers	(LayerDrawType.OPAQUE);
	}

	@Inject(
			method	= "renderLevel",
			at		= @At(
					value	= "CONSTANT",
					args	= "stringValue=translucent",
					ordinal	= 1,
					shift	= At.Shift.AFTER
			)
	)
	public void drawIrisTranslucentCoreBuffers(
			PoseStack		poseStack,
			float			partialTick,
			long			finishNanoTime,
			boolean			renderBlockOutline,
			Camera			camera,
			GameRenderer	gameRenderer,
			LightTexture	lightTexture,
			Matrix4f		projectionMatrix,
			CallbackInfo	ci
	) {
		if (!CoreFeature.isLoaded()) {
			return;
		}

		CoreBuffers.ENTITY				.drawBuffers	(LayerDrawType.TRANSLUCENT);
		CoreBuffers.BLOCK				.drawBuffers	(LayerDrawType.TRANSLUCENT);
		CoreBuffers.POS					.drawBuffers	(LayerDrawType.TRANSLUCENT);
		CoreBuffers.POS_COLOR			.drawBuffers	(LayerDrawType.TRANSLUCENT);
		CoreBuffers.POS_TEX				.drawBuffers	(LayerDrawType.TRANSLUCENT);
		CoreBuffers.POS_TEX_COLOR		.drawBuffers	(LayerDrawType.TRANSLUCENT);
		CoreBuffers.POS_COLOR_TEX_LIGHT	.drawBuffers	(LayerDrawType.TRANSLUCENT);

		CoreBuffers.ENTITY				.clearBuffers	();
		CoreBuffers.BLOCK				.clearBuffers	();
		CoreBuffers.POS					.clearBuffers	();
		CoreBuffers.POS_COLOR			.clearBuffers	();
		CoreBuffers.POS_TEX				.clearBuffers	();
		CoreBuffers.POS_TEX_COLOR		.clearBuffers	();
		CoreBuffers.POS_COLOR_TEX_LIGHT	.clearBuffers	();
	}

	@WrapOperation(
			method	= "renderLevel",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V"
			)
	)
	public void preventDrawVanillaCoreBuffers(MultiBufferSource.BufferSource instance, Operation<Void> original) {
		instance.endLastBatch();
	}

	@Inject(
			method	= "close",
			at		= @At("TAIL")
	)
	public void deleteIrisBuffers(CallbackInfo ci) {
		if (!CoreFeature.isLoaded()) {
			return;
		}

		IrisCompatBuffers.BLOCK_SHADOW				.delete();
		IrisCompatBuffers.ENTITY_SHADOW				.delete();
		IrisCompatBuffers.GLYPH_SHADOW				.delete();
		IrisCompatBuffers.POS_TEX_SHADOW			.delete();
		IrisCompatBuffers.POS_TEX_COLOR_SHADOW		.delete();

		IrisCompatBuffers.ENTITY_HAND				.delete();
		IrisCompatBuffers.BLOCK_HAND				.delete();
		IrisCompatBuffers.POS_HAND					.delete();
		IrisCompatBuffers.POS_COLOR_HAND			.delete();
		IrisCompatBuffers.POS_TEX_HAND				.delete();
		IrisCompatBuffers.POS_TEX_COLOR_HAND		.delete();
		IrisCompatBuffers.POS_COLOR_TEX_LIGHT_HAND	.delete();
	}
}
