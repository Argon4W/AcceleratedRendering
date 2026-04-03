package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record HighlightDrawContext(
		Matrix4f	transform,
		Matrix3f	normal,
		int			highlightX,
		int			highlightY,
		int			blitOffset,
		int			color
) implements IGuiElementContext {

	@Override
	public float depth() {
		return blitOffset;
	}
}
