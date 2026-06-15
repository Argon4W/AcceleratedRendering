package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreReloads;
import com.github.argon4w.acceleratedrendering.features.text.extensions.FontExtension;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedSequenceEffectRenderer;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedStyledSequenceRenderer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ExtensionMethod(FontExtension	.class)
@Mixin			(CoreReloads	.class)
public class CoreReloadsMixin {

	@Inject(
			method	= "onResourceManagerReload",
			at		= @At("TAIL")
	)
	public void onReload(ResourceManager resourceManager, CallbackInfo ci) {
		AcceleratedStyledSequenceRenderer.INSTANCE		.reload();
		AcceleratedSequenceEffectRenderer.INSTANCE		.reload();
		Minecraft.getInstance().font.getAccelerated()	.reload();
	}
}
