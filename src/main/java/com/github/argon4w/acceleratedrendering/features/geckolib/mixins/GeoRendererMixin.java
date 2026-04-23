package com.github.argon4w.acceleratedrendering.features.geckolib.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(GeoRenderer			.class)
public interface GeoRendererMixin {

	@SuppressWarnings	("unchecked")
	@Inject				(
			method		= "renderCubesOfBone",
			cancellable	= true,
			at			= @At(
					value	= "INVOKE",
					target	= "Lsoftware/bernie/geckolib/cache/object/GeoBone;getCubes()Ljava/util/List;",
					shift	= At.Shift.BEFORE
			)
	)
	default void renderCubesOfBoneFast(
			PoseStack		poseStack,
			GeoBone			bone,
			VertexConsumer	buffer,
			int				packedLight,
			int				packedOverlay,
			int				colour,
			CallbackInfo	ci
	) {
		var extension = buffer.getAccelerated();

		if (			AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&		ModsFeature							.isEnabled						()
				&&		ModsFeature							.shouldAccelerateGecko			()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			var pose = poseStack.last();

			ci			.cancel		();
			extension	.doRender	(
					(IAcceleratedRenderer<Void>) bone,
					null,
					pose.pose	(),
					pose.normal	(),
					packedLight,
					packedOverlay,
					colour
			);
		}
	}
}
