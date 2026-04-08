package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record FillDrawContext(
		Matrix4f	transform,
		Matrix3f	normal,
		RenderType	renderType,
		int			minX,
		int			minY,
		int			maxX,
		int			maxY,
		int			blitOffset,
		int			color,
		int			light,
		int			overlay
) implements IGuiElementContext {

	@Override
	public float depth() {
		return blitOffset;
	}
}
