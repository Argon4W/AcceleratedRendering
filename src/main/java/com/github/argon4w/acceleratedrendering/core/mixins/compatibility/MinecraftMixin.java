package com.github.argon4w.acceleratedrendering.core.mixins.compatibility;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.DebugOutput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(
            method  = "<init>",
            at      = @At("TAIL")
    )
    public void setDebugContext(GameConfig gameConfig, CallbackInfo ci) {
        if (		CoreFeature.isLoaded				()
				&&	CoreFeature.isDebugContextEnabled	()
		) {
            DebugOutput.enable();
        }
    }
}
