package com.github.argon4w.acceleratedrendering.features.text.extensions;

import net.minecraft.network.chat.Style;

public class StyleExtension {

	public static final int COLOR_BIT			= 1 << (24 + 0);
	public static final int BOLD_BIT			= 1 << (24 + 1);
	public static final int ITALIC_BIT			= 1 << (24 + 2);
	public static final int STRIKETHROUGH_BIT	= 1 << (24 + 3);
	public static final int UNDERLINE_BIT		= 1 << (24 + 4);
	public static final int OBFUSCATED_BIT		= 1 << (24 + 5);
	public static final int SHADOW_BIT			= 1 << (24 + 6);
	public static final int OUTLINE_BIT			= 1 << (24 + 7);

	public static int getFlag(
			Style	in,
			boolean	shadow,
			boolean	outline
	) {
		var result = 0;

		if (!in.isEmpty()) {
			var textColor = in.getColor();

			if (textColor != null) {
				result |= textColor.getValue() & 0xFFFFFF;
				result |= COLOR_BIT;
			}

			if (in.isBold()) {
				result |= BOLD_BIT;
			}

			if (in.isItalic()) {
				result |= ITALIC_BIT;
			}

			if (in.isStrikethrough()) {
				result |= STRIKETHROUGH_BIT;
			}

			if (in.isUnderlined()) {
				result |= UNDERLINE_BIT;
			}

			if (in.isObfuscated()) {
				result |= OBFUSCATED_BIT;
			}
		}

		if (shadow) {
			result |= SHADOW_BIT;
		}

		if (outline) {
			result |= OUTLINE_BIT;
		}

		return result;
	}
}
