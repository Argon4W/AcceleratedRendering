package com.github.argon4w.acceleratedrendering.compat.vanilla.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

	@SuppressWarnings	("rawtypes")
	@WrapOperation		(
			method	= "lambda$renderArmorPiece$0",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V"
			),
			remap	= false
	)
	public void setupTrimLayer(
			HumanoidArmorLayer	instance,
			ArmorMaterial		armorMaterial,
			PoseStack			poseStack,
			MultiBufferSource	bufferSource,
			int					packedLight,
			ArmorTrim			armorTrim,
			Model				model,
			boolean				innerTexture,
			Operation<Void>		original
	) {
		if (		!ModsFeature.isEnabled			()
				||	!ModsFeature.shouldFixVanilla	()
		) {
			original.call(
					instance,
					armorMaterial,
					poseStack,
					bufferSource,
					packedLight,
					armorTrim,
					model,
					innerTexture
			);
			return;
		}

		CoreFeature.forceIncrementDefaultLayer();

		original.call(
				instance,
				armorMaterial,
				poseStack,
				bufferSource,
				packedLight,
				armorTrim,
				model,
				innerTexture
		);

		CoreFeature.resetDefaultLayer();
	}
}
