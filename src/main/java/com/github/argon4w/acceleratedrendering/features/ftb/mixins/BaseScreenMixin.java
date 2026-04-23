package com.github.argon4w.acceleratedrendering.features.ftb.mixins;

import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(BaseScreen.class)
public class BaseScreenMixin {

	@Inject(
			method	= "draw",
			at		= {
					@At(
							value	= "INVOKE",
							target	= "Ldev/ftb/mods/ftblibrary/ui/Panel;draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
							shift	= At.Shift.BEFORE
					),
					@At(
							value	= "INVOKE",
							target	= "Ldev/ftb/mods/ftblibrary/ui/ModalPanel;draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
							shift	= At.Shift.BEFORE
					)
			},
			remap	= false
	)
	public void startBatching(
			GuiGraphics		graphics,
			Theme			theme,
			int				x,
			int				y,
			int				w,
			int				h,
			CallbackInfo	ci
	) {
		if (ModsFeature.shouldAccelerateFtb()) {
			GuiBatchingController.INSTANCE.startBatching(graphics);
		} else {
			AcceleratedEntityRenderingFeature	.useVanillaPipeline();
			AcceleratedItemRenderingFeature		.useVanillaPipeline();
			AcceleratedTextRenderingFeature		.useVanillaPipeline();
		}
	}

	@Inject(
			method	= "draw",
			at		= {
					@At(
							value	= "INVOKE",
							target	= "Ldev/ftb/mods/ftblibrary/ui/Panel;draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
							shift	= At.Shift.AFTER
					),
					@At(
							value	= "INVOKE",
							target	= "Ldev/ftb/mods/ftblibrary/ui/ModalPanel;draw(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
							shift	= At.Shift.AFTER
					)
			},
			remap	= false
	)
	public void stopBatching(
			GuiGraphics		graphics,
			Theme			theme,
			int				x,
			int				y,
			int				w,
			int				h,
			CallbackInfo	ci
	) {
		if (ModsFeature.shouldAccelerateFtb()) {
			GuiBatchingController.INSTANCE.flushBatching(graphics);
		} else {
			AcceleratedEntityRenderingFeature	.resetPipeline();
			AcceleratedItemRenderingFeature		.resetPipeline();
			AcceleratedTextRenderingFeature		.resetPipeline();
		}
	}
}
