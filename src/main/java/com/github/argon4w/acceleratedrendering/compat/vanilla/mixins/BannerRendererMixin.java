package com.github.argon4w.acceleratedrendering.compat.vanilla.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Mixin(BannerRenderer.class)
public class BannerRendererMixin {

	@WrapOperation(
			method = "renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLjava/util/List;Z)V",
			at = @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
					ordinal	= 0
			)
	)
	private static void wrapPatternLayer1(
			ModelPart		instance,
			PoseStack		poseStack,
			VertexConsumer	vertexConsumer,
			int				packedLight,
			int				packedOverlay,
			Operation<Void>	original
	) {
		var pass = CoreFeature.isLoaded			()
				&& ModsFeature.isEnabled		()
				&& ModsFeature.shouldFixVanilla	();

		if (pass) {
			CoreFeature.forceIncrementDefaultLayer();
		}

		original.call(
				instance,
				poseStack,
				vertexConsumer,
				packedLight,
				packedOverlay
		);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}

	@WrapOperation(
			method = "renderPatterns(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/resources/model/Material;ZLjava/util/List;Z)V",
			at = @At(
					value	= "INVOKE",
					target	= "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"
			)
	)
	private static void wrapPatternLayer2(
			Optional<?>				instance,
			Consumer<?>				action,
			Operation<Void>			original,
			@Local(name = "i") int	index
	) {
		var pass = CoreFeature.isLoaded			()
				&& ModsFeature.isEnabled		()
				&& ModsFeature.shouldFixVanilla	();

		if (pass) {
			CoreFeature.forceAddDefaultLayer(index + 1);
		}

		original.call(instance, action);

		if (pass) {
			CoreFeature.resetDefaultLayer();
		}
	}
}
