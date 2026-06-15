package com.github.argon4w.acceleratedrendering.features.text.extensions;

import com.github.argon4w.acceleratedrendering.features.text.IAcceleratedFont;
import net.minecraft.client.gui.Font;

public class FontExtension {

	public static IAcceleratedFont getAccelerated(Font in) {
		return (IAcceleratedFont) in;
	}
}
