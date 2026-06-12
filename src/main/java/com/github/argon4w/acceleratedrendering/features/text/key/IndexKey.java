package com.github.argon4w.acceleratedrendering.features.text.key;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.resources.ResourceLocation;

public record IndexKey(ISequenceKey key, int id) implements ISequenceKey {

	@Override
	public IntArrayList getTexts() {
		return key.getTexts();
	}

	@Override
	public ResourceLocation getFont() {
		return key.getFont();
	}

	@Override
	public float getAdvance() {
		return key.getAdvance();
	}

	@Override
	public int getColor() {
		return key.getColor();
	}

	@Override
	public boolean hasColor() {
		return key.hasColor();
	}

	@Override
	public boolean isBold() {
		return key.isBold();
	}

	@Override
	public boolean isItalic() {
		return key.isItalic();
	}

	@Override
	public boolean isStrikethrough() {
		return key.isStrikethrough();
	}

	@Override
	public boolean isUnderlined() {
		return key.isUnderlined();
	}

	@Override
	public boolean isShadow() {
		return key.isShadow();
	}

	@Override
	public boolean isOutline() {
		return key.isOutline();
	}

	@Override
	public ISequenceKey bake() {
		return key.bake();
	}
}
