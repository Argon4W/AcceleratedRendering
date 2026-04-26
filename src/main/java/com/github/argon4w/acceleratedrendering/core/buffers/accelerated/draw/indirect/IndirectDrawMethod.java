package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.indirect;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.IDrawMethod;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraftforge.fml.ModLoader;

public class IndirectDrawMethod implements IDrawMethod {

	public static final IDrawMethod INSTANCE = new IndirectDrawMethod();

	@Override
	public ICullingProgramSelector getCullingProgramSelector(VertexFormat vertexFormat) {
		return ModLoader.get().postEventWithReturn(new LoadCullingProgramSelectorEvent(vertexFormat)).getSelector();
	}

	@Override
	public IDrawContextPool getDrawContextPool(int size) {
		return new IndirectDrawContextPool(size);
	}

	@Override
	public IElementPool getElementPool(int size) {
		return new IndirectElementBufferPool(size);
	}
}
