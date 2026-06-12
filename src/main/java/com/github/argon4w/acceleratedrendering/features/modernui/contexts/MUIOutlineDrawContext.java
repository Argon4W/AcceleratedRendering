package com.github.argon4w.acceleratedrendering.features.modernui.contexts;

import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.IStringDrawContext;
import icyllis.modernui.mc.text.ModernTextRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public record MUIOutlineDrawContext(
		Matrix4f				transform,
		ModernTextRenderer		renderer,
		FormattedCharSequence	text,
		float					textX,
		float					textY,
		int						textColor,
		int						backgroundColor,
		int						packedLight
) implements IStringDrawContext {

	@Override
	public void drawString(MultiBufferSource bufferSource) {
		renderer.drawText8xOutline(
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
