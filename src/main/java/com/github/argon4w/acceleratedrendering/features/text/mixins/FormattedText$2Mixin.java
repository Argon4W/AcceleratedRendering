package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.features.text.cache.ISeekableFormattedText;
import lombok.EqualsAndHashCode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@Mixin(targets = "net.minecraft.network.chat.FormattedText$2")
public class FormattedText$2Mixin implements ISeekableFormattedText {

	@Shadow @Final String val$text;
}
