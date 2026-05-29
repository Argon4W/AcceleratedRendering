package com.github.argon4w.acceleratedrendering.features.create.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.utils.DirectionUtils;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedQuadsRenderer;
import com.github.argon4w.acceleratedrendering.features.items.BakedModelExtension;
import com.github.argon4w.acceleratedrendering.features.items.colors.FixedColors;
import com.github.argon4w.acceleratedrendering.features.items.contexts.AcceleratedQuadsRenderContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@ExtensionMethod(value = {VertexConsumerExtension	.class, BakedModelExtension.class	})
@Mixin			(value = {PartialItemModelRenderer	.class								})
public class PartialItemModelRendererMixin {

	@SuppressWarnings("deprecation")
	@WrapMethod			(
			method	= "renderBakedItemModel",
			remap	= false
	)
	public void renderBakedModelFast(
			BakedModel		bakedModel,
			int				packedLight,
			PoseStack		poseStack,
			VertexConsumer	buffer,
			Operation<Void>	original
	) {
		var extension1 = buffer		.getAccelerated();
		var extension2 = bakedModel	.getAccelerated();

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
					bakedModel,
					packedLight,
					poseStack,
					buffer
			);
			return;
		}

		var pose	= poseStack		.last	();
		var random	= RandomSource	.create	(42L);

		if (extension2.isAccelerated()) {
			extension2.renderItemFast(
					ItemStack.EMPTY,
					random,
					pose,
					extension1,
					packedLight,
					OverlayTexture.NO_OVERLAY
			);
			return;
		}

		if (!AcceleratedItemRenderingFeature.shouldBakeMeshForQuad()) {
			original.call(
					bakedModel,
					packedLight,
					poseStack,
					buffer
			);
			return;
		}

		var color = new FixedColors(-1);

		for (var direction : DirectionUtils.FULL) {
			random		.setSeed	(42L);
			extension1	.doRender	(
					AcceleratedQuadsRenderer.INSTANCE,
					new AcceleratedQuadsRenderContext(
							bakedModel.getQuads(
									null,
									direction,
									random
							),
							color
					),
					pose.pose	(),
					pose.normal	(),
					packedLight,
					OverlayTexture.NO_OVERLAY,
					-1
			);
		}
	}
}
