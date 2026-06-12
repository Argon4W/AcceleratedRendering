package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.features.text.cache.ISeekableFormattedText;
import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@Mixin(targets = "net.minecraft.network.chat.FormattedText$3")
public class FormattedText$3Mixin implements ISeekableFormattedText {

	@Shadow @Final String val$text;
	@Shadow @Final Style val$style;
}
