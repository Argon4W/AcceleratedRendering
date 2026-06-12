package com.github.argon4w.acceleratedrendering.features.items.gui;

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;

@Getter
public class FontAdvanceEstimator implements FormattedCharSink {

	public static final FontAdvanceEstimator INSTANCE = new FontAdvanceEstimator(Minecraft.getInstance().font);

	private final	Font	fonts;
	private final	boolean	filter;
	private			float	advance;

	public FontAdvanceEstimator(Font font) {
		this.fonts		= font;
		this.filter		= font.filterFishyGlyphs;
		this.advance	= 0.0f;
	}

	@Override
	public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {
		var bold		= style		.isBold			();
		var location	= style		.getFont		();
		var fontSet		= fonts		.getFontSet		(location);
		var info		= fontSet	.getGlyphInfo	(codePoint, filter);

		advance += info.getAdvance(bold);

		return true;
	}

	public void add(int codePoint, Style style) {
		accept(
				0,
				style,
				codePoint
		);
	}

	public FontAdvanceEstimator reset() {
		advance = 0.0f;
		return this;
	}

	public float getAdvance(
			Style	textStyle,
			String	textString,
			boolean	textShadow,
			float	textX
	) {
		StringDecomposer.iterateFormatted(
				textString,
				textStyle,
				reset()
		);

		return textX + getAdvance() + (textShadow ? 1.0f : 0.0f);
	}

	public float getAdvance(
			FormattedCharSequence	textSequence,
			boolean					textShadow,
			float					textX
	) {
		textSequence.accept(reset());

		return textX + getAdvance() + (textShadow ? 1.0f : 0.0f);
	}
}
