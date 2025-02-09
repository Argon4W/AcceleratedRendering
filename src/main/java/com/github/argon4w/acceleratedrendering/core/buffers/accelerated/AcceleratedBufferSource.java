package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.buffers.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.gl.buffers.MappedBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;

import static org.lwjgl.opengl.GL46.*;

public class AcceleratedBufferSource extends MultiBufferSource.BufferSource implements IAcceleratedBufferSource {

    private final IBufferEnvironment bufferEnvironment;
    private final AcceleratedBufferSetPool acceleratedBufferSetPool;
    private final Map<RenderType, AcceleratedBufferBuilder> acceleratedBuilders;

    private AcceleratedBufferSetPool.BufferSet bufferSet;

    public AcceleratedBufferSource(IBufferEnvironment bufferEnvironment) {
        super(null, null);

        this.bufferEnvironment = bufferEnvironment;
        this.acceleratedBufferSetPool = new AcceleratedBufferSetPool(this.bufferEnvironment);
        this.acceleratedBuilders = new Object2ObjectLinkedOpenHashMap<>();

        this.bufferSet = acceleratedBufferSetPool.getBufferSet();
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
        return bufferEnvironment;
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        AcceleratedBufferBuilder builder = acceleratedBuilders.get(renderType);

        if (builder != null) {
            return builder;
        }

        MappedBuffer elementBuffer = bufferSet.getElementBuffer();

        if (elementBuffer == null) {
            drawBuffers();
            clearBuffers();
            elementBuffer = bufferSet.getElementBuffer();
        }

        builder = new AcceleratedBufferBuilder(
                elementBuffer,
                bufferSet,
                renderType
        );

        acceleratedBuilders.put(renderType, builder);
        return builder;
    }

    @Override
    public void drawBuffers() {
        if (acceleratedBuilders.isEmpty()) {
            return;
        }

        bufferSet.bindTransformBuffers();
        bufferEnvironment.selectTransformProgram().dispatch(bufferSet.getVertexCount());

        BufferUploader.reset();
        bufferSet.bindVertexArray();

        for (RenderType renderType : acceleratedBuilders.keySet()) {
            AcceleratedBufferBuilder builder = acceleratedBuilders.get(renderType);
            MappedBuffer elementBuffer = builder.getElementBuffer();

            if (elementBuffer.getPosition() == 0) {
                continue;
            }

            VertexFormat.Mode mode = renderType.mode;
            int vertexCount = builder.getVertexCount();

            elementBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, 5);
            bufferSet.bindCullingBuffers(elementBuffer.getBufferSize());

            bufferEnvironment.selectProcessingProgramDispatcher(mode).dispatch(mode, vertexCount);
            bufferEnvironment.selectCullProgramDispatcher(renderType).dispatch(mode, vertexCount);

            renderType.setupRenderState();
            bufferSet.bindDrawBuffers();
            bufferEnvironment.setupBufferState();

            CompiledShaderProgram shader = RenderSystem.getShader();

            shader.setDefaultUniforms(mode,
                    RenderSystem.getModelViewMatrix(),
                    RenderSystem.getProjectionMatrix(),
                    Minecraft.getInstance().getWindow()
            );
            shader.apply();

            glDrawElementsIndirect(
                    mode.asGLMode,
                    VertexFormat.IndexType.INT.asGLType,
                    0L
            );
            glMemoryBarrier(GL_ELEMENT_ARRAY_BARRIER_BIT | GL_COMMAND_BARRIER_BIT);

            shader.clear();
            renderType.clearRenderState();
        }

        bufferSet.resetVertexArray();
        acceleratedBuilders.clear();
    }

    @Override
    public void clearBuffers() {
        bufferSet.reset();
        bufferSet.setInFlight();
        bufferSet = acceleratedBufferSetPool.getBufferSet();
    }
}
