package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IDrawContextPool.IDrawContext;
import com.google.common.collect.Iterators;

import java.util.Iterator;

public class SeparatedLayerStorage implements ILayerStorage {

	private final ILayerContexts[]	contexts;
	private final ILayerContexts	opaqueContexts;
	private final ILayerContexts	translucentContexts;

	public SeparatedLayerStorage(int size) {
		this.contexts				= new ILayerContexts		[3];
		this.opaqueContexts			= new SimpleLayerContexts	(size);
		this.translucentContexts	= new SimpleLayerContexts	(size);

		contexts[0] = this.translucentContexts;
		contexts[1] = this.opaqueContexts;
		contexts[2] = new AllContexts();
	}

	@Override
	public ILayerContexts get(LayerDrawType type) {
		return contexts[type.ordinal()];
	}

	@Override
	public void reset() {
		opaqueContexts		.reset();
		translucentContexts	.reset();
	}

	public class AllContexts implements ILayerContexts {

		@Override
		public void add(IDrawContext drawContext) {
			throw new UnsupportedOperationException("Unsupported Operation.");
		}

		@Override
		public void reset() {
			opaqueContexts		.reset();
			translucentContexts	.reset();
		}

		@Override
		public void prepare() {

		}

		@Override
		public boolean isEmpty() {
			return		opaqueContexts		.isEmpty()
					&&	translucentContexts	.isEmpty();
		}

		@Override
		public Iterator<IDrawContext> iterator() {
			return Iterators.concat(
					opaqueContexts		.iterator(),
					translucentContexts	.iterator()
			);
		}
	}
}
