package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

	@SuppressWarnings	("rawtypes")
	@WrapOperation		(
			method	= "renderBackground",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;renderTransparentBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"
			)
	)
	public void immediateDrawTransparentBackground(
			AbstractContainerScreen	instance,
			GuiGraphics				guiGraphics,
			Operation<Void>			original
	) {
		CoreFeature.forceBypassGuiBatching();

		original.call(instance, guiGraphics);

		CoreFeature.resetBypassGuiBatching();
	}

	@Inject(
			method	= "render",
			at		= @At("HEAD")
	)
	public void startBackgroundBatching(
			GuiGraphics							guiGraphics,
			int									mouseX,
			int									mouseY,
			float								partialTick,
			CallbackInfo						ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		depth	.set(0.0f);
		enabled	.set(GuiBatchingController.INSTANCE.startBatching(guiGraphics));
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
			GuiGraphics							guiGraphics,
			int									mouseX,
			int									mouseY,
			float								partialTick,
			CallbackInfo						ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (!AcceleratedItemRenderingFeature.shouldMergeGuiItemBatches() && enabled.get()) {
			depth.set(depth.get() + GuiBatchingController.INSTANCE.flushBatching(guiGraphics));

			var pose = guiGraphics.pose().last().pose();

			var previousDepth = GuiBatchingController.getGlobalDepth(
					pose.m22(),
					pose.m32(),
					0.0F
			);

			guiGraphics
					.pose			()
					.last			()
					.pose			()
					.translateLocal	(
							0.0f,
							0.0f,
							depth.get() - previousDepth
					);

			depth.set(0.0f);
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
			GuiGraphics							guiGraphics,
			int									mouseX,
			int									mouseY,
			float								partialTick,
			CallbackInfo						ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (!AcceleratedItemRenderingFeature.shouldMergeGuiItemBatches() && enabled.get()) {
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
			GuiGraphics							guiGraphics,
			int									mouseX,
			int									mouseY,
			float								partialTick,
			CallbackInfo						ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (enabled.get()) {
			depth.set(depth.get() + GuiBatchingController.INSTANCE.flushBatching(guiGraphics));
		}
	}

	@Inject(
			method	= "render",
			at		= {
					@At(
							value	= "INVOKE",
							target	= "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;",
							shift	= At.Shift.BEFORE,
							ordinal	= 1
					),
					@At("TAIL")
			}
	)
	public void liftGlobalLayer(
			GuiGraphics							guiGraphics,
			int									mouseX,
			int									mouseY,
			float								partialTick,
			CallbackInfo						ci,
			@Share("depth")		LocalFloatRef	depth,
			@Share("enabled")	LocalBooleanRef	enabled
	) {
		if (enabled.get()) {
			var pose = guiGraphics.pose().last().pose();

			var previousDepth = GuiBatchingController.getGlobalDepth(
					pose.m22(),
					pose.m32(),
					0.0F
			);

			guiGraphics
					.pose			()
					.last			()
					.pose			()
					.translateLocal	(
							0.0f,
							0.0f,
							depth.get() - previousDepth
					);
		}
	}

	@WrapMethod(method = "renderSlotHighlight(Lnet/minecraft/client/gui/GuiGraphics;IIII)V")
	private static void startRenderHighlight(
			GuiGraphics		guiGraphics,
			int				highlightX,
			int				highLightY,
			int				blitOffset,
			int				color,
			Operation<Void>	original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
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
