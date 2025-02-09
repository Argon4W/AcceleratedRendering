package com.github.argon4w.acceleratedrendering.core.mixins;

import com.github.argon4w.acceleratedrendering.CoreFeature;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "lambda$addMainPass$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"))
    public void endOutlineBatches(
        FogParameters fogParameters,
        DeltaTracker deltaTracker,
        Camera camera,
        ProfilerFiller profiler,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix,
        ResourceHandle<RenderTarget> resourcehandle2,
        ResourceHandle<RenderTarget> resourcehandle,
        ResourceHandle<RenderTarget> resourcehandle3,
        ResourceHandle<RenderTarget> resourcehandle4,
        Frustum frustum,
        boolean renderBlockOutline,
        ResourceHandle<RenderTarget> resourcehandle1,
        CallbackInfo ci
    ) {
        CoreFeature.POS_TEX_COLOR.drawBuffers();
        CoreFeature.OUTLINE_BATCHING.drawBuffers();
        CoreFeature.POS_TEX_COLOR.clearBuffers();
        CoreFeature.OUTLINE_BATCHING.clearBuffers();
    }

    @Inject(method = "lambda$addMainPass$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 0))
    public void endAllEntityBatches(
        FogParameters fogParameters,
        DeltaTracker deltaTracker,
        Camera camera,
        ProfilerFiller profiler,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix,
        ResourceHandle<RenderTarget> resourcehandle2,
        ResourceHandle<RenderTarget> resourcehandle,
        ResourceHandle<RenderTarget> resourcehandle3,
        ResourceHandle<RenderTarget> resourcehandle4,
        Frustum frustum,
        boolean renderBlockOutline,
        ResourceHandle<RenderTarget> resourcehandle1,
        CallbackInfo ci
    ) {
        CoreFeature.ENTITY.drawBuffers();
        CoreFeature.POS_TEX.drawBuffers();
        CoreFeature.CORE_BATCHING.drawBuffers();
        CoreFeature.ENTITY.clearBuffers();
        CoreFeature.POS_TEX.clearBuffers();
        CoreFeature.CORE_BATCHING.clearBuffers();
    }
}
