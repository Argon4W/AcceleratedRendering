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
			method	= "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V"
			)
	)
	public void setupTrimLayer(
			HumanoidArmorLayer		instance,
			Holder<ArmorMaterial>	armorMaterial,
			PoseStack				poseStack,
			MultiBufferSource		bufferSource,
			int						packedLight,
			ArmorTrim				trim,
			Model					model,
			boolean					innerTexture,
			Operation<Void>			original
	) {
		var pass = CoreFeature.isLoaded			()
				&& ModsFeature.isEnabled		()
				&& ModsFeature.shouldFixVanilla	();

		if (pass) {
			CoreFeature.forceIncrementDefaultLayer();
		}

		original.call(
				instance,
				armorMaterial,
				poseStack,
				bufferSource,
				packedLight,
				trim,
				model,
				innerTexture
		);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}
}
