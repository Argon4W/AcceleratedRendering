package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffers;
import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffersProvider;
import com.github.argon4w.acceleratedrendering.core.CoreStates;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import lombok.experimental.ExtensionMethod;
import net.irisshaders.batchedentityrendering.impl.RenderBuffersExt;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.shadows.ShadowCompositeRenderer;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@ExtensionMethod(BufferSourceExtension	.class)
@Mixin			(ShadowRenderer			.class)
public class ShadowRendererMixin {

	@Shadow @Final private RenderBuffers	buffers;
	@Shadow @Final private RenderBuffersExt	renderBuffersExt;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void bindAcceleratedShadowBufferSources(
			IrisRenderingPipeline	pipeline,
			ProgramSource			shadow,
			PackDirectives			directives,
			ShadowRenderTargets		shadowRenderTargets,
			ShadowCompositeRenderer	compositeRenderer,
			CustomUniforms			customUniforms,
			boolean					separateHardwareSamplers,
			CallbackInfo			ci
	) {
		renderBuffersExt.beginLevelRendering();

		buffers.bufferSource			().getAcceleratable().bindAcceleratedBufferSource(IrisCompatBuffersProvider.SHADOW);
		buffers.crumblingBufferSource	().getAcceleratable().bindAcceleratedBufferSource(IrisCompatBuffersProvider.SHADOW);
		buffers.outlineBufferSource		().getAcceleratable().bindAcceleratedBufferSource(IrisCompatBuffersProvider.SHADOW);

		renderBuffersExt.endLevelRendering();
	}

	@Inject(
			method	= "renderShadows",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"
			)
	)
	public void endAllBatches(
			LevelRendererAccessor	levelRenderer,
			Camera					playerCamera,
			CallbackInfo			ci
	) {
		CoreStates								.recordBuffers	();
		IrisCompatBuffers.BLOCK_SHADOW			.prepareBuffers	();
		IrisCompatBuffers.ENTITY_SHADOW			.prepareBuffers	();
		IrisCompatBuffers.GLYPH_SHADOW			.prepareBuffers	();
		IrisCompatBuffers.POS_TEX_SHADOW		.prepareBuffers	();
		IrisCompatBuffers.POS_TEX_COLOR_SHADOW	.prepareBuffers	();
		CoreStates								.restoreBuffers	();

		IrisCompatBuffers.BLOCK_SHADOW			.drawBuffers	(LayerDrawType.ALL);
		IrisCompatBuffers.ENTITY_SHADOW			.drawBuffers	(LayerDrawType.ALL);
		IrisCompatBuffers.GLYPH_SHADOW			.drawBuffers	(LayerDrawType.ALL);
		IrisCompatBuffers.POS_TEX_SHADOW		.drawBuffers	(LayerDrawType.ALL);
		IrisCompatBuffers.POS_TEX_COLOR_SHADOW	.drawBuffers	(LayerDrawType.ALL);

		IrisCompatBuffers.BLOCK_SHADOW			.clearBuffers	();
		IrisCompatBuffers.ENTITY_SHADOW			.clearBuffers	();
		IrisCompatBuffers.GLYPH_SHADOW			.clearBuffers	();
		IrisCompatBuffers.POS_TEX_SHADOW		.clearBuffers	();
		IrisCompatBuffers.POS_TEX_COLOR_SHADOW	.clearBuffers	();
	}
}
