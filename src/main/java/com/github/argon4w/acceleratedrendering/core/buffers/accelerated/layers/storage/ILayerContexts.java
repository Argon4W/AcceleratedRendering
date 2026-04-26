package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool.IDrawContext;

public interface ILayerContexts extends Iterable<IDrawContext> {

	void	add		(IDrawContext drawContext);
	void	reset	();
	void	prepare	();
	boolean	isEmpty	();
}
