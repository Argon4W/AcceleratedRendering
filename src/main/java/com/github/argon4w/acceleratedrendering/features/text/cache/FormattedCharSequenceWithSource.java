package com.github.argon4w.acceleratedrendering.features.text.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

@AllArgsConstructor
public class FormattedCharSequenceWithSource implements FormattedCharSequence, ISeekableFormattedCharSequence {

	private final FormattedText			source;
	private final FormattedCharSequence	original;

	@Override
	public boolean accept(FormattedCharSink sink) {
		return original.accept(sink);
	}

	@Override
	public FormattedText getSource() {
		return source;
	}
}
