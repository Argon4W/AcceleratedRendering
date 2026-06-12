package com.github.argon4w.acceleratedrendering.features.items.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.utils.DirectionUtils;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedQuadsRenderer;
import com.github.argon4w.acceleratedrendering.features.items.BakedModelExtension;
import com.github.argon4w.acceleratedrendering.features.items.colors.FixedColors;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ExtensionMethod(value = {VertexConsumerExtension	.class, BakedModelExtension.class	})
@Mixin			(value = {ModelBlockRenderer		.class								}, priority = 999)
public class ModelBlockRendererMixin {

	@Inject(
			cancellable	= true,
			method		= "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/BakedModel;FFFIILnet/minecraftforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V",
			at			= @At("HEAD"),
			remap		= false
	)
	public void renderModelFast(
			PoseStack.Pose		pose,
			VertexConsumer		consumer,
			BlockState			state,
			BakedModel			model,
			float				red,
			float				green,
			float				blue,
			int					packedLight,
			int					packedOverlay,
			ModelData			modelData,
			RenderType			renderType,
			CallbackInfo		ci
	) {
		var extension1 = consumer	.getAccelerated();
		var extension2 = model		.getAccelerated();

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
			return;
		}

		if (extension2.isAccelerated()) {
			ci			.cancel			();
			extension2	.renderBlockFast(
					state,
					RandomSource.create(42),
					pose,
					extension1,
					packedLight,
					packedOverlay,
					FastColor.ARGB32.color(
							255,
							(int) (red		* 255.0f),
							(int) (green	* 255.0f),
							(int) (blue		* 255.0f)
					),
					modelData,
					renderType
			);
			return;
		}

		if (!AcceleratedItemRenderingFeature.shouldBakeMeshForQuad()) {
			return;
		}

		ci.cancel();

		var randomSource = RandomSource.create();

		for (var direction : DirectionUtils.FULL) {
			randomSource.setSeed	(42L);
			extension1	.doRender	(
					AcceleratedQuadsRenderer.INSTANCE,
					AcceleratedQuadsRenderer.context(
							model.getQuads(
									state,
									direction,
									randomSource,
									modelData,
									renderType
							),
							new FixedColors(FastColor.ARGB32.color(
									255,
									(int) (red		* 255.0f),
									(int) (green	* 255.0f),
									(int) (blue		* 255.0f)
							))
					),
					pose.pose	(),
					pose.normal	(),
					packedLight,
					packedOverlay,
					-1
			);
		}
	}
}
