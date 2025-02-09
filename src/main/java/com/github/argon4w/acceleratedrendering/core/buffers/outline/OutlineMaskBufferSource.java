package com.github.argon4w.acceleratedrendering.core.buffers.outline;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedOutlineBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.builders.OutlineMask;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;

public class OutlineMaskBufferSource implements IAcceleratedOutlineBufferSource {

    private final IAcceleratedBufferSource bufferSource;

    private int color;

    public OutlineMaskBufferSource(IAcceleratedBufferSource bufferSource) {
        this.bufferSource = bufferSource;
        this.color = -1;
    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        return new OutlineMask(bufferSource.getBuffer(pRenderType), color);
    }

    @Override
    public void setColor(int color) {
        this.color = ARGB.color(255, color);
    }

    @Override
    public void drawBuffers() {
        bufferSource.drawBuffers();
    }

    @Override
    public void clearBuffers() {
        bufferSource.clearBuffers();
    }

    @Override
    public IBufferEnvironment getBufferEnvironment() {
        return bufferSource.getBufferEnvironment();
    }
}
