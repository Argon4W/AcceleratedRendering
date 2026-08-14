package com.github.argon4w.acceleratedrendering.compat.vanilla.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.item.DyeColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BannerRenderer.class)
public class BannerRendererMixin {

	@WrapOperation(
			method = "renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Z)V",
			at = @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/blockentity/BannerRenderer;renderPatternLayer(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;Lnet/minecraft/world/item/DyeColor;)V",
					ordinal	= 0
			)
	)
	private static void wrapPatternLayer1(
			PoseStack			poseStack,
			MultiBufferSource	buffer,
			int					packedLight,
			int					packedOverlay,
			ModelPart			flagPart,
			Material			material,
			DyeColor			color,
			Operation<Void>		original
	) {
		var pass = CoreFeature.isLoaded			()
				&& ModsFeature.isEnabled		()
				&& ModsFeature.shouldFixVanilla	();

		if (pass) {
			CoreFeature.forceIncrementDefaultLayer();
		}

		original.call(
				poseStack,
				buffer,
				packedLight,
				packedOverlay,
				flagPart,
				material,
				color
		);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}

	@WrapOperation(
			method = "renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLnet/minecraft/world/item/DyeColor;Lnet/minecraft/world/level/block/entity/BannerPatternLayers;Z)V",
			at = @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/blockentity/BannerRenderer;renderPatternLayer(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;Lnet/minecraft/world/item/DyeColor;)V",
					ordinal	= 1
			)
	)
	private static void wrapPatternLayer2(
			PoseStack				poseStack,
			MultiBufferSource		buffer,
			int						packedLight,
			int						packedOverlay,
			ModelPart				flagPart,
			Material				material,
			DyeColor				color,
			Operation<Void>			original,
			@Local(name = "i") int	index
	) {
		var pass = CoreFeature.isLoaded			()
				&& ModsFeature.isEnabled		()
				&& ModsFeature.shouldFixVanilla	();

		if (pass) {
			CoreFeature.forceAddDefaultLayer(index + 1);
		}

		original.call(
				poseStack,
				buffer,
				packedLight,
				packedOverlay,
				flagPart,
				material,
				color
		);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}
}
