package com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record Outline8StringDrawContext(
		Matrix4f				transform,
		Font					font,
		FormattedCharSequence	text,
		float					textX,
		float					textY,
		int						textColor,
		int						backgroundColor,
		int						packedLight
) implements IStringDrawContext {

	@Override
	public void drawString(MultiBufferSource bufferSource) {
		font.drawInBatch8xOutline(
				text,
				textX,
				textY,
				textColor,
				backgroundColor,
				transform,
				bufferSource,
				packedLight
		);
	}
}
