package com.github.argon4w.acceleratedrendering.core.programs.processing;

import com.github.argon4w.acceleratedrendering.core.programs.IPolygonProgramDispatcher;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.neoforged.fml.ModLoader;

public interface IPolygonProcessor {

    IPolygonProgramDispatcher select(VertexFormat.Mode mode);
    IExtraVertexData getExtraVertex(VertexFormat.Mode mode);

     static IPolygonProcessor empty() {
        return EmptyPolygonProcessor.INSTANCE;
    }

    static IPolygonProcessor get(VertexFormat vertexFormat) {
         return ModLoader.postEventWithReturn(new LoadPolygonProcessorEvent(vertexFormat)).processor;
    }
}
