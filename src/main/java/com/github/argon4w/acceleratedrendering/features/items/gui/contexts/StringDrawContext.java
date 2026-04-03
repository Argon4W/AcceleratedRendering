package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import net.minecraft.client.gui.Font;
import org.joml.Matrix4f;

public record StringDrawContext(
		Matrix4f			transform,
		Font				font,
		String				text,
		float				textX,
		float				textY,
		int					textColor,
		boolean				dropShadow,
		Font.DisplayMode	displayMode,
		int					backgroundColor,
		int					packedLight,
		boolean				bidirectional
) {

}
