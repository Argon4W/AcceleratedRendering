package com.github.argon4w.acceleratedrendering.features.entities.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityShadowRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(
		value		= EntityRenderDispatcher.class,
		priority	= 999
)
public class EntityRenderDispatcherMixin {

	@Unique private static final Matrix3f							SHADOW_NORMAL_MATRIX	= new Matrix3f().identity				();
	@Unique private static final AcceleratedEntityShadowRenderer	SHADOW_RENDERER			= new AcceleratedEntityShadowRenderer	();

	@Inject(
			method		= "renderBlockShadow",
			at			= @At("HEAD"),
			cancellable	= true
	)
	private static void fastBlockShadow(
			PoseStack.Pose	pPose,
			VertexConsumer	pVertexConsumer,
			ChunkAccess		pChunk,
			LevelReader		pLevel,
			BlockPos		pPos,
			double			pX,
			double			pY,
			double			pZ,
			float			pSize,
			float			pWeight,
			CallbackInfo	ci
	) {
		var extension = pVertexConsumer.getAccelerated();

		if (		CoreFeature							.isLoaded						()
				&&	CoreFeature							.isRenderingLevel				()
				&&	AcceleratedEntityRenderingFeature	.isEnabled						()
				&&	AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	extension							.isAccelerated					()
		) {
			ci			.cancel		();
			extension	.doRender	(
					SHADOW_RENDERER,
					new AcceleratedEntityShadowRenderer.Context(
							pLevel,
							pChunk,
							pPos,
							new Vector3f(
									(float) pX,
									(float) pY,
									(float) pZ
							),
							pSize,
							pWeight
					),
					pPose.pose(),
					SHADOW_NORMAL_MATRIX,
					LightTexture	.FULL_BRIGHT,
					OverlayTexture	.NO_OVERLAY,
					-1
			);
		}
	}
}
