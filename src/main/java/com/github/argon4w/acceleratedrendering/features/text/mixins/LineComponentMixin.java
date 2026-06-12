package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.features.text.cache.ISeekableFormattedText;
import lombok.EqualsAndHashCode;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@EqualsAndHashCode(cacheStrategy = EqualsAndHashCode.CacheStrategy.LAZY)
@Mixin(targets = "net.minecraft.client.StringSplitter$LineComponent")
public class LineComponentMixin implements ISeekableFormattedText {

	@Shadow @Final String	contents;
	@Shadow @Final Style	style;
}
