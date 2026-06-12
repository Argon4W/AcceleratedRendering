package com.github.argon4w.acceleratedrendering.features.text.cache;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;

public class WithColorSink implements FormattedCharSink {

	public static final WithColorSink INSTANCE = new WithColorSink();

	private FormattedCharSink	sink;
	private int					color;

	@Override
	public boolean accept(
			int		position,
			Style	codepointStyle,
			int		codePoint
	) {
		if (sink != null) {
			return sink.accept(
					position,
					codepointStyle.withColor(color),
					codePoint
			);
		}

		return true;
	}

	public static FormattedCharSink of(FormattedCharSink sink, int color) {
		INSTANCE.sink	= sink;
		INSTANCE.color	= color;

		return INSTANCE;
	}
}
