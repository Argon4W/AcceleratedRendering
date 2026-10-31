package com.github.argon4w.acceleratedrendering.features.ftb.mixins;

import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.ftb.mods.ftblibrary.ui.Panel;
import dev.ftb.mods.ftblibrary.ui.Theme;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Panel.class)
public class PanelMixin {

	@Inject(
			method	= "draw",
			at		= @At(
					value	= "INVOKE",
					target	= "Ldev/ftb/mods/ftblibrary/ui/Panel;setOffset(Z)V",
					ordinal	= 0,
					shift	= At.Shift.AFTER
			)
	)
	public void startBackgroundBatching(
			GuiGraphics						graphics,
			Theme							theme,
			int								x,
			int								y,
			int								w,
			int								h,
			CallbackInfo					ci,
			@Share("batch") LocalBooleanRef	batch
	) {
		if (		ModsFeature.isEnabled			()
				&&	ModsFeature.shouldAccelerateFtb	()
		) {
			batch.set(GuiBatchingController.INSTANCE.startBatching(graphics));
		}
	}

	@Inject(
			method	= "draw",
			at		= @At(
					value	= "INVOKE",
					target	= "Ldev/ftb/mods/ftblibrary/ui/Panel;drawOffsetBackground(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
					shift 	= At.Shift.BEFORE
			)
	)
	public void flushBackgroundBatching(
			GuiGraphics						graphics,
			Theme							theme,
			int								x,
			int								y,
			int								w,
			int								h,
			CallbackInfo					ci,
			@Share("batch") LocalBooleanRef	batch
	) {
		if (batch.get()) {
			GuiBatchingController.INSTANCE.flushBatching(graphics, false);
		}
	}

	@Inject(
			method	= "draw",
			at		= @At(
					value	= "INVOKE",
					target	= "Ldev/ftb/mods/ftblibrary/ui/Panel;drawOffsetBackground(Lnet/minecraft/client/gui/GuiGraphics;Ldev/ftb/mods/ftblibrary/ui/Theme;IIII)V",
					ordinal	= 0,
					shift	= At.Shift.AFTER
			)
	)
	public void startForegroundBatching(
			GuiGraphics						graphics,
			Theme							theme,
			int								x,
			int								y,
			int								w,
			int								h,
			CallbackInfo					ci,
			@Share("batch") LocalBooleanRef	batch
	) {
		if (batch.get()) {
			GuiBatchingController.INSTANCE.startBatching(graphics);
		}
	}

	@Inject(
			method	= "draw",
			at		= @At(
					value	= "INVOKE",
					target	= "Ldev/ftb/mods/ftblibrary/ui/Panel;setOffset(Z)V",
					ordinal	= 1,
					shift 	= At.Shift.BEFORE
			)
	)
	public void flushForegroundBatching(
			GuiGraphics						graphics,
			Theme							theme,
			int								x,
			int								y,
			int								w,
			int								h,
			CallbackInfo					ci,
			@Share("batch") LocalBooleanRef	batch
	) {
		if (batch.get()) {
			GuiBatchingController.INSTANCE.flushBatching(graphics, false);
		}
	}
}
