package com.github.argon4w.acceleratedrendering.core.buffers.fallback;

import com.github.argon4w.acceleratedrendering.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.pools.ByteBufferBuilderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.mixins.BufferBuilderAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class VanillaBatchingBufferSource extends MultiBufferSource.BufferSource implements IAcceleratedBufferSource {

    private final ByteBufferBuilderPool bufferPool;
    private final Object2ObjectSortedMap<RenderType, BufferBuilder> bufferBuilders;

    public VanillaBatchingBufferSource() {
        super(null, null);

        this.bufferPool = new ByteBufferBuilderPool(CoreFeature.getPooledBufferSetSize());
        this.bufferBuilders = new Object2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public void endLastBatch() {

    }

    @Override
    public void endBatch() {

    }

    @Override
    public void endBatch(RenderType pRenderType) {

    }

    @Override
    public IBufferEnvironment getBufferEnvironment() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clearBuffers() {
        bufferPool.reset();
        bufferBuilders.clear();
    }

    @Override
    public void drawBuffers() {
        if (bufferBuilders.isEmpty()) {
            return;
        }

        for (RenderType renderType : bufferBuilders.keySet()) {
            BufferBuilder builder = bufferBuilders.get(renderType);
            MeshData meshData = builder.build();

            if (meshData == null) {
                continue;
            }

            if (renderType.sortOnUpload) {
                meshData.sortQuads(((BufferBuilderAccessor) builder).getBuffer(), RenderSystem.getProjectionType().vertexSorting());
            }

            renderType.draw(meshData);
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderType pRenderType) {
        BufferBuilder builder = bufferBuilders.get(pRenderType);

        if (builder != null) {
            return builder;
        }

        ByteBufferBuilder buffer = bufferPool.get();

        if (buffer == null) {
            drawBuffers();
            clearBuffers();
            buffer = bufferPool.get();
        }

        builder = new BufferBuilder(
                buffer,
                pRenderType.mode,
                pRenderType.format
        );

        bufferBuilders.put(pRenderType, builder);
        return builder;
    }
}
