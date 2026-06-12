package com.github.argon4w.acceleratedrendering.features.modernui.mixins;

import com.github.argon4w.acceleratedrendering.features.text.cache.ISeekableFormattedCharSequence;
import icyllis.modernui.mc.text.FormattedTextWrapper;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FormattedTextWrapper.class)
public class FormattedTextWrapperMixin implements ISeekableFormattedCharSequence {

	@Shadow(remap = false) @Final public FormattedText mText;

	@Override
	public FormattedText getSource() {
		return mText;
	}
}
