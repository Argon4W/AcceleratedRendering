package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.features.text.cache.FormattedCharSequenceWithSource;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

	@ModifyReturnValue(
			method	= "getVisualOrder",
			at		= @At("RETURN"),
			require	= 0
	)
	public FormattedCharSequence wrapVisualOrder(FormattedCharSequence original, FormattedText text) {
		return new FormattedCharSequenceWithSource(text, original);
	}
}
