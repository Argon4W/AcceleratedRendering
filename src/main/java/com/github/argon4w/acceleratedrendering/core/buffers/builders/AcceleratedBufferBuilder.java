package com.github.argon4w.acceleratedrendering.core.buffers.builders;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSetPool;
import com.github.argon4w.acceleratedrendering.core.gl.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferUtils;
import com.github.argon4w.acceleratedrendering.core.utils.IntElementUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.Set;

public class AcceleratedBufferBuilder implements VertexConsumer, IVertexConsumerExtension {

    private final MappedBuffer elementBuffer;
    private final AcceleratedBufferSetPool.BufferSet bufferSet;
    private final RenderType renderType;
    private final VertexFormat.Mode mode;
    private final int polygonSize;

    private final long posOffset;
    private final long colorOffset;
    private final long uv0Offset;
    private final long uv1Offset;
    private final long uv2Offset;
    private final long normalOffset;

    private PoseStack.Pose pose;
    private int elementCount;
    private int vertexCount;
    private long vertex;
    private long varying;
    private long transform;
    private int sharing;

    public AcceleratedBufferBuilder(
        MappedBuffer elementBuffer,
        AcceleratedBufferSetPool.BufferSet bufferSet,
        RenderType renderType

    ) {
        this.elementBuffer = elementBuffer;
        this.bufferSet = bufferSet;
        this.renderType = renderType;
        this.mode = this.renderType.mode;
        this.polygonSize = this.mode.primitiveLength;

        this.posOffset = bufferSet.getOffset(VertexFormatElement.POSITION);
        this.colorOffset = bufferSet.getOffset(VertexFormatElement.COLOR);
        this.uv0Offset = bufferSet.getOffset(VertexFormatElement.UV0);
        this.uv1Offset = bufferSet.getOffset(VertexFormatElement.UV1);
        this.uv2Offset = bufferSet.getOffset(VertexFormatElement.UV2);
        this.normalOffset = bufferSet.getOffset(VertexFormatElement.NORMAL);

        this.pose = null;
        this.elementCount = 0;
        this.vertexCount = 0;
        this.vertex = -1;
        this.varying = -1;
        this.transform = -1;
        this.sharing = -1;
    }

    private void putElements(int size) {
        IntElementUtils.putElements(
            mode,
            elementBuffer,
            bufferSet.getElement(size),
            size
        );
    }

    @Override
    public VertexConsumer addVertex(
        PoseStack.Pose pPose,
        float pX,
        float pY,
        float pZ
    ) {
        beginTransform(pPose);
        return addVertex(
            pX,
            pY,
            pZ
        );
    }

    @Override
    public VertexConsumer addVertex(
        float pX,
        float pY,
        float pZ
    ) {
        vertexCount++;
        elementCount++;

        if (elementCount >= polygonSize) {
            putElements(polygonSize);
            elementCount = 0;
        }

        vertex = bufferSet.reserveVertex();

        MemoryUtil.memPutFloat(vertex + posOffset + 0L, pX);
        MemoryUtil.memPutFloat(vertex + posOffset + 4L, pY);
        MemoryUtil.memPutFloat(vertex + posOffset + 8L, pZ);
        bufferSet.addExtraVertex(vertex);

        varying = bufferSet.reserveVarying();
        MemoryUtil.memPutInt(varying + 0 * 4L, 0);
        MemoryUtil.memPutInt(varying + 1 * 4L, sharing);

        return this;
    }

    @Override
    public VertexConsumer setColor(
        int pRed,
        int pGreen,
        int pBlue,
        int pAlpha
    ) {
        if (colorOffset == -1) {
            return this;
        }

        if (vertex == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutByte(varying + 2 * 4L + 0L, (byte) pRed);
        MemoryUtil.memPutByte(varying + 2 * 4L + 1L, (byte) pGreen);
        MemoryUtil.memPutByte(varying + 2 * 4L + 2L, (byte) pBlue);
        MemoryUtil.memPutByte(varying + 2 * 4L + 3L, (byte) pAlpha);

        return this;
    }

    @Override
    public VertexConsumer setUv(float pU, float pV) {
        if (uv0Offset == -1) {
            return this;
        }

        if (vertex == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutFloat(vertex + uv0Offset + 0L, pU);
        MemoryUtil.memPutFloat(vertex + uv0Offset + 4L, pV);

        return this;
    }

    @Override
    public VertexConsumer setUv1(int pU, int pV) {
        if (uv1Offset == -1) {
            return this;
        }

        if (vertex == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutShort(varying + 4 * 4L + 0L, (short) pU);
        MemoryUtil.memPutShort(varying + 4 * 4L + 2L, (short) pV);

        return this;
    }

    @Override
    public VertexConsumer setUv2(int pU, int pV) {
        if (uv2Offset == -1) {
            return this;
        }

        if (vertex == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        MemoryUtil.memPutShort(varying + 3 * 4L + 0L, (short) pU);
        MemoryUtil.memPutShort(varying + 3 * 4L + 2L, (short) pV);

        return this;
    }

    @Override
    public VertexConsumer setNormal(
        PoseStack.Pose pPose,
        float pNormalX,
        float pNormalY,
        float pNormalZ
    ) {
        if (transform == -1) {
            return VertexConsumer.super.setNormal(
                pPose,
                pNormalX,
                pNormalY,
                pNormalZ
            );
        }

        if (this.pose != pPose) {
            ByteBufferUtils.putMatrix3x4f(transform + 4L * 4L * 4L, pose.normal());
        }

        return setNormal(
            pNormalX,
            pNormalY,
            pNormalZ
        );
    }

    @Override
    public VertexConsumer setNormal(
        float pNormalX,
        float pNormalY,
        float pNormalZ
    ) {
        if (normalOffset == -1) {
            return this;
        }

        if (vertex == -1) {
            throw new IllegalStateException("Vertex not building!");
        }

        ByteBufferUtils.putNormal(vertex + normalOffset + 0L, pNormalX);
        ByteBufferUtils.putNormal(vertex + normalOffset + 1L, pNormalY);
        ByteBufferUtils.putNormal(vertex + normalOffset + 2L, pNormalZ);

        return this;
    }

    @Override
    public void addVertex(
        float pX,
        float pY,
        float pZ,
        int pColor,
        float pU,
        float pV,
        int pPackedOverlay,
        int pPackedLight,
        float pNormalX,
        float pNormalY,
        float pNormalZ
    ) {
        vertexCount++;
        elementCount++;

        if (elementCount >= polygonSize) {
            putElements(polygonSize);
            elementCount = 0;
        }

        long vertex = bufferSet.reserveVertex();

        MemoryUtil.memPutFloat(vertex + posOffset + 0L, pX);
        MemoryUtil.memPutFloat(vertex + posOffset + 4L, pY);
        MemoryUtil.memPutFloat(vertex + posOffset + 8L, pZ);
        bufferSet.addExtraVertex(vertex);

        if (uv0Offset != -1) {
            MemoryUtil.memPutFloat(vertex + uv0Offset + 0L, pU);
            MemoryUtil.memPutFloat(vertex + uv0Offset + 4L, pV);
        }

        if (normalOffset != -1) {
            ByteBufferUtils.putNormal(vertex + normalOffset + 0L, pNormalX);
            ByteBufferUtils.putNormal(vertex + normalOffset + 1L, pNormalY);
            ByteBufferUtils.putNormal(vertex + normalOffset + 2L, pNormalZ);
        }

        long varying = bufferSet.reserveVarying();

        MemoryUtil.memPutInt(varying + 0L * 4L, 0);
        MemoryUtil.memPutInt(varying + 1L * 4L, -1);
        MemoryUtil.memPutInt(varying + 2L * 4L, ARGB.toABGR(pColor));
        MemoryUtil.memPutInt(varying + 3L * 4L, pPackedLight);
        MemoryUtil.memPutInt(varying + 4L * 4L, pPackedOverlay);
    }

    @Override
    public void beginTransform(PoseStack.Pose pose) {
        if (this.pose == pose) {
            return;
        }

        this.pose = pose;
        this.sharing = bufferSet.getSharing();
        this.transform = bufferSet.reserveSharing();

        long normal = transform + 4L * 4L * 4L;
        long flags = normal + 4L * 3L * 4L;
        long mesh = flags + 4L;
        long extra = mesh + 4L;

        ByteBufferUtils.putMatrix4f(transform, pose.pose());
        ByteBufferUtils.putMatrix3x4f(normal, pose.normal());
        MemoryUtil.memPutInt(flags, bufferSet.getSharingFlags());
        MemoryUtil.memPutInt(mesh, -1);

        bufferSet.addExtraSharings(extra);
    }

    @Override
    public void endTransform() {
        this.pose = null;
        this.sharing = -1;
        this.transform = -1;
    }

    @Override
    public void addClientMesh(
        RenderType renderType,
        ByteBuffer vertexBuffer,
        int size,
        int color,
        int light,
        int overlay
    ) {
        putElements(size);
        vertexCount += size;

        long vertex = bufferSet.reservePolygons(size);
        long varying = bufferSet.reserveVaryings(size);
        long length = (long) size * bufferSet.getVertexSize();

        ByteBufferUtils.putByteBuffer(
            vertexBuffer,
            vertex,
            length
        );

        MemoryUtil.memPutInt(varying + 1L * 4L, sharing);
        MemoryUtil.memPutInt(varying + 2L * 4L, ARGB.toABGR(color));
        MemoryUtil.memPutInt(varying + 3L * 4L, light);
        MemoryUtil.memPutInt(varying + 4L * 4L, overlay);

        for (int i = 0; i < size; i++) {
            MemoryUtil.memPutInt(varying + i * 5L * 4L, i);
        }
    }

    @Override
    public void addServerMesh(
        RenderType renderType,
        int offset,
        int size,
        int color,
        int light,
        int overlay
    ) {
        putElements(size);

        bufferSet.reservePolygons(size);
        vertexCount += size;

        long varying = bufferSet.reserveVaryings(size);
        long mesh = transform + 4L * 4L * 4L + 4L * 3L * 4L + 4L;

        MemoryUtil.memPutInt(mesh, offset / bufferSet.getVertexSize());
        MemoryUtil.memPutInt(varying + 1L * 4L, sharing);
        MemoryUtil.memPutInt(varying + 2L * 4L, ARGB.toABGR(color));
        MemoryUtil.memPutInt(varying + 3L * 4L, light);
        MemoryUtil.memPutInt(varying + 4L * 4L, overlay);

        for (int i = 0; i < size; i++) {
            MemoryUtil.memPutInt(varying + i * 5L * 4L, i);
        }
    }

    @Override
    public boolean supportAcceleratedRendering() {
        return true;
    }

    @Override
    public Set<RenderType> getRenderTypes() {
        return Set.of(renderType);
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public MappedBuffer getElementBuffer() {
        return elementBuffer;
    }
}
