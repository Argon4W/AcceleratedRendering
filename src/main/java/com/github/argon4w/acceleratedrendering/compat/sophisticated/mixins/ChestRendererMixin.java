package com.github.argon4w.acceleratedrendering.compat.sophisticated.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.p3pp3rf1y.sophisticatedstorage.client.render.ChestRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(ChestRenderer.class)
public class ChestRendererMixin {

	@WrapOperation(
			method	= "render(Lnet/p3pp3rf1y/sophisticatedstorage/block/ChestBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIZ)V",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/p3pp3rf1y/sophisticatedstorage/client/render/ChestRenderer$ChestSubRenderer;renderTier(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;FII)V"
			)
	)
	public void onRenderTier(
			@Coerce Object		instance,
			PoseStack			poseStack,
			MultiBufferSource	bufferSource,
			float				lidAngle,
			int					packedLight,
			int					packedOverlay,
			Operation<Void>		original
	) {
		var pass =	CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	();

		if (pass) {
			CoreFeature.forceIncrementDefaultLayer();
		}

		original.call(
				instance,
				poseStack,
				bufferSource,
				lidAngle,
				packedLight,
				packedOverlay
		);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}

	@WrapOperation(
			method	= "render(Lnet/p3pp3rf1y/sophisticatedstorage/block/ChestBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IIZ)V",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/p3pp3rf1y/sophisticatedstorage/client/render/ChestRenderer$ChestSubRenderer;renderHiddenTier(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
			)
	)
	public void onRenderHiddenTier(
			@Coerce Object		instance,
			PoseStack			poseStack,
			MultiBufferSource	bufferSource,
			int					packedLight,
			int					packedOverlay,
			Operation<Void>		original
	) {
		var pass =	CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	();

		if (pass) {
			CoreFeature.forceIncrementDefaultLayer();
		}

		original.call(
				instance,
				poseStack,
				bufferSource,
				packedLight,
				packedOverlay
		);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}
}
