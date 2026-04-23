package com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record ComponentStringDrawContext(
		Matrix4f			transform,
		Font				font,
		Component			text,
		float				textX,
		float				textY,
		int					textColor,
		boolean				dropShadow,
		Font.DisplayMode	displayMode,
		int					backgroundColor,
		int					packedLight
) implements IStringDrawContext {

	public static final Matrix3f NORMAL = new Matrix3f().identity();

	@Override
	public Matrix3f normal() {
		return NORMAL;
	}

	@Override
	public float depth() {
		return 0.0f;
	}

	@Override
	public void drawString(MultiBufferSource bufferSource) {
		font.drawInBatch(
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				transform,
				bufferSource,
				displayMode,
				backgroundColor,
				packedLight
		);
	}
}
