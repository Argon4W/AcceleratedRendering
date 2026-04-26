package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage.empty;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage.ILayerContexts;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool.IDrawContext;
import com.github.argon4w.acceleratedrendering.core.utils.EmptyIterator;

import java.util.Iterator;

public class EmptyLayerContexts implements ILayerContexts {

	public static final EmptyLayerContexts INSTANCE = new EmptyLayerContexts();

	@Override
	public void add(IDrawContext drawContext) {

	}

	@Override
	public void reset() {

	}

	@Override
	public void prepare() {

	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public Iterator<IDrawContext> iterator() {
		return EmptyIterator.of();
	}
}
