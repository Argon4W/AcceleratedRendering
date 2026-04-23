package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record BlitDrawContext(
		Matrix4f			transform,
		Matrix3f			normal,
		ResourceLocation	atlasLocation,
		int					minX,
		int					maxX,
		int					minY,
		int					maxY,
		int					blitOffset,
		int					blitColor,
		int					blitLight,
		int					blitOverlay,
		float				minU,
		float				maxU,
		float				minV,
		float				maxV
) implements IGuiElementContext {

	@Override
	public float depth() {
		return blitOffset;
	}
}
