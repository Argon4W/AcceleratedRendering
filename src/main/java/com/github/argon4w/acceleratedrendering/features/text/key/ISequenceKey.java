package com.github.argon4w.acceleratedrendering.features.text.key;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.resources.ResourceLocation;

public interface ISequenceKey {

	IntArrayList		getTexts		();
	ResourceLocation	getFont			();
	float				getAdvance		();
	int					getColor		();
	boolean				hasColor		();
	boolean				isBold			();
	boolean				isItalic		();
	boolean				isStrikethrough	();
	boolean				isUnderlined	();
	boolean				isShadow		();
	boolean				isOutline		();
	ISequenceKey		bake			();
}
