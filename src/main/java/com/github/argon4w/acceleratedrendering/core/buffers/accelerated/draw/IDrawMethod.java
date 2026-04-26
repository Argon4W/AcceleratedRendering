package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.mojang.blaze3d.vertex.VertexFormat;

public interface IDrawMethod {

	ICullingProgramSelector	getCullingProgramSelector	(VertexFormat	vertexFormat);
	IDrawContextPool		getDrawContextPool			(int			size);
	IElementPool			getElementPool				(int			size);
}
