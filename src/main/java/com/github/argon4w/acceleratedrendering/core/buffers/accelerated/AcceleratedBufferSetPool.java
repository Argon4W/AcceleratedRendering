package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.Sync;
import com.github.argon4w.acceleratedrendering.core.backends.VertexArray;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.DrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.ElementBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.MappedBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.VertexBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.programs.processing.IExtraVertexData;
import com.github.argon4w.acceleratedrendering.core.utils.ByteUtils;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.core.Direction;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL46.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER;

public class AcceleratedBufferSetPool {

    private final IBufferEnvironment bufferEnvironment;
    private final BufferSet[] bufferSets;
    private final int size;

    public AcceleratedBufferSetPool(IBufferEnvironment bufferEnvironment) {
        this.bufferEnvironment = bufferEnvironment;
        this.size = CoreFeature.getPooledBufferSetSize();
        this.bufferSets = new BufferSet[this.size];

        for (int i = 0; i < this.size; i++) {
            this.bufferSets[i] = new BufferSet();
        }
    }

    public BufferSet getBufferSet() {
        for (int i = 0; i < size; i++) {
            BufferSet buffer = bufferSets[i];

            if (buffer.isFree()) {
                buffer.setUsed();
                return buffer;
            }
        }

        BufferSet bufferSet = bufferSets[0];
        bufferSet.waitSync();
        bufferSet.setUsed();

        return bufferSet;
    }

    public class BufferSet {

        private final int size;
        private final DrawContextPool drawContextPool;
        private final ElementBufferPool elementBufferPool;
        private final MappedBuffer sharingBuffer;
        private final MappedBuffer decalBuffer;
        private final MappedBuffer rotationBuffer;
        private final MappedBufferPool varyingBuffer;
        private final VertexBufferPool vertexBuffer;
        private final VertexArray vertexArray;
        private final Sync sync;
        private final MutableInt sharing;
        private final MutableInt decal;

        private boolean used;
        private VertexFormat format;

        public BufferSet() {
            this.size = CoreFeature.getPooledElementBufferSize();
            this.drawContextPool = new DrawContextPool(this.size);
            this.elementBufferPool = new ElementBufferPool(this.size);
            this.sharingBuffer = new MappedBuffer(64L);
            this.decalBuffer = new MappedBuffer(64L);
            this.rotationBuffer = new MappedBuffer(4L * 4L * 4L * 6L);
            this.varyingBuffer = new MappedBufferPool(this.size);
            this.vertexBuffer = new VertexBufferPool(this.size, this);
            this.vertexArray = new VertexArray();
            this.sync = new Sync();
            this.sharing = new MutableInt(0);
            this.decal = new MutableInt(0);

            this.used = false;
            this.format = null;

            long rotation = this.rotationBuffer.reserve(4L * 4L * 4L * 6L);
            Direction[] directions = Direction.values();

            for (int i = 0; i < 6; i++) {
                Direction direction = directions[i];

                Matrix4f matrix = new Matrix4f().identity();
                matrix.rotate(direction.getRotation());
                matrix.rotateX((float) (- Math.PI / 2));
                matrix.rotateY((float) Math.PI);

                ByteUtils.putMatrix4f(rotation + i * 4L * 4L * 4L, matrix);
            }

            this.rotationBuffer.flush();
        }

        public void reset() {
            drawContextPool.reset();
            elementBufferPool.reset();
            varyingBuffer.reset();
            sharingBuffer.reset();
            decalBuffer.reset();
            vertexBuffer.reset();

            sharing.setValue(0);
            decal.setValue(0);
        }

        public void bindTransformBuffers() {
            vertexBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, 1);
            sharingBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, 2);
            decalBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, 5);
            rotationBuffer.bindBase(GL_SHADER_STORAGE_BUFFER, 7);
            bufferEnvironment.getServerMeshBuffer().bindBase(GL_SHADER_STORAGE_BUFFER, 4);
        }

        public void bindDrawBuffers() {
            if (format != bufferEnvironment.getActiveFormat()
                    || elementBufferPool.isResized()
                    || vertexBuffer.isResized()) {
                format = bufferEnvironment.getActiveFormat();
                elementBufferPool.bindElementBuffer();
                elementBufferPool.resetResized();
                vertexBuffer.bind(GL_ARRAY_BUFFER);
                vertexBuffer.resetResized();
                bufferEnvironment.setupBufferState();
            }

            drawContextPool.bindCommandBuffer();
        }

        public void prepare() {
            sharingBuffer.flush();
            decalBuffer.flush();
            vertexBuffer.prepare();
            elementBufferPool.prepare();
        }

        public VertexBufferPool.VertexBuffer getVertexBuffer() {
            return vertexBuffer.get();
        }

        public MappedBufferPool.Pooled getVaryingBuffer() {
            return varyingBuffer.get();
        }

        public ElementBufferPool.ElementSegment getElementSegment() {
            return elementBufferPool.get();
        }

        public DrawContextPool.IndirectDrawContext getDrawContext() {
            return drawContextPool.get();
        }

        public int getOffset(VertexFormatElement element) {
            return bufferEnvironment.getOffset(element);
        }

        public int getVertexSize() {
            return bufferEnvironment.getVertexSize();
        }

        public int getFlags(VertexFormat.Mode mode) {
            return bufferEnvironment.getFlags(mode);
        }

        public int getSharing() {
            return sharing.getAndIncrement();
        }

        public int getDecal() {
            return decal.getAndIncrement();
        }

        public long reserveSharing() {
            return sharingBuffer.reserve(4L * 4L * 4L + 4L * 4L * 3L);
        }

        public long reserveDecal() {
            return decalBuffer.reserve(4L * 4L * 4L + 4L * 4L * 3L + 4L * 4L);
        }

        public IExtraVertexData getExtraVertex(VertexFormat.Mode mode) {
            return bufferEnvironment.getExtraVertex(mode);
        }

        public void bindVertexArray() {
            vertexArray.bindVertexArray();
        }

        public void resetVertexArray() {
            vertexArray.unbindVertexArray();
        }

        public int getSize() {
            return size;
        }

        public void setUsed() {
            used = true;
        }

        public void setInFlight() {
            used = false;
            sync.setSync();
        }

        protected void waitSync() {
            if (!sync.isSyncSet()) {
                return;
            }

            sync.waitSync();
            sync.resetSync();
        }

        public boolean isFree() {
            if (used) {
                return false;
            }

            if (!sync.isSyncSet()) {
                return true;
            }

            if (!sync.isSyncSignaled()) {
                return false;
            }

            sync.resetSync();

            return true;
        }
    }
}
