package com.github.argon4w.acceleratedrendering.core.buffers.fallback;

import com.github.argon4w.acceleratedrendering.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.outline.IOutlineBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;

public class FallbackOutlineBufferSource implements IOutlineBufferSource {

    private final OutlineBufferSource vanillaBufferSource;
    private final IOutlineBufferSource vanillaBatchingBufferSource;

    public FallbackOutlineBufferSource(
            OutlineBufferSource outlineBufferSource,
            IOutlineBufferSource vanillaBatchingBufferSource
    ) {
        this.vanillaBufferSource = outlineBufferSource;
        this.vanillaBatchingBufferSource = vanillaBatchingBufferSource;
    }

    @Override
    public void setColor(int color) {
        vanillaBufferSource.setColor(
            ARGB.red(color),
            ARGB.green(color),
            ARGB.blue(color),
                255
        );
        vanillaBatchingBufferSource.setColor(color);
    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        return CoreFeature.shouldUseVanillaBatching()
                ? vanillaBatchingBufferSource.getBuffer(pRenderType)
                : vanillaBufferSource.getBuffer(pRenderType);
    }
}
