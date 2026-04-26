package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.basevertex.BaseVertexDrawMethod;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.indirect.IndirectDrawMethod;

public enum DrawMethodType {

	INDIRECT,
	BASEVERTEX;

	public IDrawMethod get() {
		return get(this);
	}

	public static IDrawMethod get(DrawMethodType drawMethodType) {
		return switch(drawMethodType) {
			case INDIRECT	-> IndirectDrawMethod	.INSTANCE;
			case BASEVERTEX	-> BaseVertexDrawMethod	.INSTANCE;
		};
	}
}
