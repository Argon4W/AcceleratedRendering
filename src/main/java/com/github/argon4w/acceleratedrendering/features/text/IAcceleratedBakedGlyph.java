package com.github.argon4w.acceleratedrendering.features.text;

import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedBakedGlyphRenderer;

public interface IAcceleratedBakedGlyph {

	AcceleratedBakedGlyphRenderer getRenderer(boolean italic);
}
