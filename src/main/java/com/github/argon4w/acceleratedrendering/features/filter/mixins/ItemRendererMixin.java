package com.github.argon4w.acceleratedrendering.features.filter.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.filter.FilterFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
		value		= ItemRenderer.class,
		priority	= 1001
)
public class ItemRendererMixin {

	@WrapOperation(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
			)
	)
	public void filterItem(
			ItemRenderer	instance,
			BakedModel		bakedModel,
			ItemStack		model,
			int				stack,
			int				combinedLight,
			PoseStack		combinedOverlay,
			VertexConsumer	poseStack,
			Operation<Void>	original
	) {
		var pass =	!	CoreFeature		.isLoaded			()
				||	!	FilterFeature	.isEnabled			()
				||	!	FilterFeature	.shouldFilterItems	()
				||		FilterFeature	.testItem			(model);

		if (!pass) {
			AcceleratedEntityRenderingFeature	.useVanillaPipeline();
			AcceleratedItemRenderingFeature		.useVanillaPipeline();
			AcceleratedTextRenderingFeature		.useVanillaPipeline();
		}

		original.call(
				instance,
				bakedModel,
				model,
				stack,
				combinedLight,
				combinedOverlay,
				poseStack
		);

		if (!pass) {
			AcceleratedEntityRenderingFeature	.resetPipeline();
			AcceleratedItemRenderingFeature		.resetPipeline();
			AcceleratedTextRenderingFeature		.resetPipeline();
		}
	}
}
