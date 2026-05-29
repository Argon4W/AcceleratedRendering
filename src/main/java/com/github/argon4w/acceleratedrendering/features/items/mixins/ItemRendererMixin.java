package com.github.argon4w.acceleratedrendering.features.items.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.utils.DirectionUtils;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedQuadsRenderer;
import com.github.argon4w.acceleratedrendering.features.items.BakedModelExtension;
import com.github.argon4w.acceleratedrendering.features.items.colors.ItemLayerColors;
import com.github.argon4w.acceleratedrendering.features.items.contexts.AcceleratedQuadsRenderContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@ExtensionMethod(value = {VertexConsumerExtension	.class, BakedModelExtension.class	})
@Mixin			(value = {ItemRenderer				.class								})
public class ItemRendererMixin {

	@SuppressWarnings	("deprecation")
	@WrapOperation		(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
			)
	)
	public void renderFast(
			ItemRenderer	instance,
			BakedModel		pModel,
			ItemStack		pStack,
			int				pCombinedLight,
			int				pCombinedOverlay,
			PoseStack		pPoseStack,
			VertexConsumer	pBuffer,
			Operation<Void>	original
	) {
		var extension1 = pBuffer.getAccelerated();
		var extension2 = pModel	.getAccelerated();

		if (			!		CoreFeature						.isLoaded						()
				||		!		AcceleratedItemRenderingFeature	.isEnabled						()
				||		!		AcceleratedItemRenderingFeature	.shouldUseAcceleratedPipeline	()
				||	(	!		CoreFeature						.isRenderingLevel				()

				&&		!	(	CoreFeature						.isRenderingHand				()
				&&			(	extension2						.isAcceleratedInHand			()
				||				AcceleratedItemRenderingFeature	.shouldAccelerateInHand			()))

				&&		!	(	CoreFeature						.isRenderingGui					()
				&&			(	extension2						.isAcceleratedInGui				()
				||				AcceleratedItemRenderingFeature	.shouldAccelerateInGui			())))
				||		!		extension1						.isAccelerated					()
		) {
			original.call(
					instance,
					pModel,
					pStack,
					pCombinedLight,
					pCombinedOverlay,
					pPoseStack,
					pBuffer
			);
			return;
		}

		var pose	= pPoseStack	.last	();
		var random	= RandomSource	.create	(42L);

		if (extension2.isAccelerated()) {
			extension2.renderItemFast(
					pStack,
					random,
					pose,
					extension1,
					pCombinedLight,
					pCombinedOverlay
			);
			return;
		}

		if (!AcceleratedItemRenderingFeature.shouldBakeMeshForQuad()) {
			original.call(
					instance,
					pModel,
					pStack,
					pCombinedLight,
					pCombinedOverlay,
					pPoseStack,
					pBuffer
			);
			return;
		}

		var color = new ItemLayerColors(pStack);

		for (var direction : DirectionUtils.FULL) {
			random		.setSeed	(42L);
			extension1	.doRender	(
					AcceleratedQuadsRenderer.INSTANCE,
					new AcceleratedQuadsRenderContext(
							pModel.getQuads(
									null,
									direction,
									random
							),
							color
					),
					pose.pose	(),
					pose.normal	(),
					pCombinedLight,
					pCombinedOverlay,
					-1
			);
		}
	}
}
