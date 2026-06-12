package com.github.argon4w.acceleratedrendering.features.text.extensions;

import com.github.argon4w.acceleratedrendering.features.text.IAcceleratedBakedGlyph;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public class BakedGlyphExtension {

	public static IAcceleratedBakedGlyph getAccelerated(BakedGlyph in) {
		return (IAcceleratedBakedGlyph) in;
	}
}
