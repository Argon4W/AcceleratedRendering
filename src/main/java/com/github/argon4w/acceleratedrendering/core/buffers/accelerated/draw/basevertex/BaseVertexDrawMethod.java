package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.basevertex;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.IDrawMethod;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.programs.culling.EmptyCullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.mojang.blaze3d.vertex.VertexFormat;

public class BaseVertexDrawMethod implements IDrawMethod {

	public static final BaseVertexDrawMethod INSTANCE = new BaseVertexDrawMethod();

	@Override
	public ICullingProgramSelector getCullingProgramSelector(VertexFormat vertexFormat) {
		return EmptyCullingProgramSelector.INSTANCE;
	}

	@Override
	public IDrawContextPool getDrawContextPool(int size) {
		return new BaseVertexDrawContextPool(size);
	}

	@Override
	public IElementPool getElementPool(int size) {
		return new BaseVertexElementPool(size);
	}
}
