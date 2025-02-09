package com.github.argon4w.acceleratedrendering.core.buffers.redirecting;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class RedirectingBufferSource extends MultiBufferSource.BufferSource {

    private final ObjectSet<IAcceleratedBufferSource> bufferSources;
    private final ReferenceSet<VertexFormat.Mode> modes;
    private final ObjectSet<String> fallbackNames;
    private final MultiBufferSource fallbackBufferSource;
    private final boolean supportSort;

    public RedirectingBufferSource(
            ObjectSet<IAcceleratedBufferSource> bufferSources,
            ReferenceSet<VertexFormat.Mode> modes,
            ObjectSet<String> fallbackNames,
            MultiBufferSource fallbackBufferSource,
            boolean supportSort
    ) {
        super(null, null);

        this.bufferSources = bufferSources;
        this.modes = modes;
        this.fallbackNames = fallbackNames;
        this.fallbackBufferSource = fallbackBufferSource;
        this.supportSort = supportSort;
    }

    @Override
    public void endBatch(RenderType pRenderType) {

    }

    @Override
    public void endBatch() {

    }

    @Override
    public void endLastBatch() {

    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        if (pRenderType.sortOnUpload && !supportSort) {
            return fallbackBufferSource.getBuffer(pRenderType);
        }

        if (!modes.contains(pRenderType.mode)) {
            return fallbackBufferSource.getBuffer(pRenderType);
        }

        if (fallbackNames.contains(pRenderType.toString())) {
            return fallbackBufferSource.getBuffer(pRenderType);
        }

        for (IAcceleratedBufferSource bufferSource1 : bufferSources) {
            if (bufferSource1.getBufferEnvironment().isAccelerated(pRenderType.format)) {
                return bufferSource1.getBuffer(pRenderType);
            }
        }

        return fallbackBufferSource.getBuffer(pRenderType);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ObjectSet<IAcceleratedBufferSource> bufferSources;
        private final ReferenceSet<VertexFormat.Mode> modes;
        private final ObjectSet<String> fallbackNames;

        private boolean supportSort;
        private MultiBufferSource fallbackBufferSource;

        private Builder() {
            this.bufferSources = new ObjectArraySet<>();
            this.modes = new ReferenceOpenHashSet<>();
            this.fallbackNames = new ObjectOpenHashSet<>();

            this.supportSort = false;
            this.fallbackBufferSource = null;
        }

        public Builder fallback(MultiBufferSource fallback) {
            this.fallbackBufferSource = fallback;
            return this;
        }

        public Builder bufferSource(IAcceleratedBufferSource source) {
            this.bufferSources.add(source);
            return this;
        }

        public Builder mode(VertexFormat.Mode mode) {
            this.modes.add(mode);
            return this;
        }

        public Builder fallbackName(String name) {
            this.fallbackNames.add(name);
            return this;
        }

        public Builder supportSort() {
            this.supportSort = true;
            return this;
        }

        public RedirectingBufferSource build() {
            return new RedirectingBufferSource(
                    bufferSources,
                    modes,
                    fallbackNames,
                    fallbackBufferSource,
                    supportSort
            );
        }
    }
}
