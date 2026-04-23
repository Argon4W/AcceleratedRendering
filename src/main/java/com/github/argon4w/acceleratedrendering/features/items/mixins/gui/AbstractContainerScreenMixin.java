package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

	@Inject(
			method	= "render",
			at		= @At("HEAD")
	)
	public void startBackgroundBatching(
			GuiGraphics		guiGraphics,
			int				mouseX,
			int				mouseY,
			float			partialTick,
			CallbackInfo	ci
	) {
		GuiBatchingController.INSTANCE.startBatching(guiGraphics);
	}

	@Inject(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
					shift	= At.Shift.BEFORE
			)
	)
	public void flushBackgroundBatching(
			GuiGraphics		guiGraphics,
			int				mouseX,
			int				mouseY,
			float			partialTick,
			CallbackInfo	ci
	) {
		if (!AcceleratedItemRenderingFeature.shouldMergeGuiItemBatches()) {
			GuiBatchingController.INSTANCE.flushBatching(guiGraphics);
		}
	}

	@Inject(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
					shift	= At.Shift.AFTER
			)
	)
	public void startItemBatching(
			GuiGraphics		guiGraphics,
			int				mouseX,
			int				mouseY,
			float			partialTick,
			CallbackInfo	ci
	) {
		if (!AcceleratedItemRenderingFeature.shouldMergeGuiItemBatches()) {
			GuiBatchingController.INSTANCE.startBatching(guiGraphics);
		}
	}

	@Inject(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
					shift	= At.Shift.AFTER
			)
	)
	public void flushItemBatching(
			GuiGraphics		guiGraphics,
			int				mouseX,
			int				mouseY,
			float			partialTick,
			CallbackInfo	ci
	) {
		GuiBatchingController.INSTANCE.flushBatching(guiGraphics);
	}

	@WrapMethod(
			method	= "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;IIII)V",
			remap	= false
	)
	private static void startRenderHighlight(
			GuiGraphics		guiGraphics,
			int				highlightX,
			int				highLightY,
			int				blitOffset,
			int				color,
			Operation<Void>	original
	) {
		if (!CoreFeature.isGuiBatching()) {
			original.call(
					guiGraphics,
					highlightX,
					highLightY,
					blitOffset,
					color
			);
			return;
		}

		var last = guiGraphics.pose().last();

		GuiBatchingController.INSTANCE.submitHighlight(
				last.pose	(),
				last.normal	(),
				highlightX,
				highLightY,
				blitOffset,
				color
		);
	}
}
