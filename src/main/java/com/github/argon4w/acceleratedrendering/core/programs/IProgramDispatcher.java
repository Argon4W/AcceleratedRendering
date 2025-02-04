package com.github.argon4w.acceleratedrendering.core.programs;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.ElementBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.builders.AcceleratedBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface IProgramDispatcher {

    void dispatch(VertexFormat.Mode mode, ElementBuffer elementBuffer, AcceleratedBufferBuilder builder);
}
