package com.github.argon4w.acceleratedrendering.compat.sophisticated.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import net.minecraft.client.gui.GuiGraphics;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(StorageScreenBase.class)
public class StorageScreenBaseMixin {

	@Inject(
			method	= "renderSuper",
			at		= @At("HEAD"),
			remap	= false
	)
	public void startBackgroundBatching(
			GuiGraphics		guiGraphics,
			int				mouseX,
			int				mouseY,
			float			partialTick,
			CallbackInfo	ci
	) {
		if (		CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	()
		) {
			GuiBatchingController.INSTANCE.startBatching(guiGraphics);
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
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
		if (		CoreFeature						.isLoaded						()
				&&	ModsFeature						.isEnabled						()
				&&	ModsFeature						.shouldAccelerateSophisticated	()
				&& !AcceleratedItemRenderingFeature	.shouldMergeGuiItemBatches		()
		) {
			GuiBatchingController.INSTANCE.flushBatching(guiGraphics);
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
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
		if (		CoreFeature						.isLoaded						()
				&&	ModsFeature						.isEnabled						()
				&&	ModsFeature						.shouldAccelerateSophisticated	()
				&& !AcceleratedItemRenderingFeature	.shouldMergeGuiItemBatches		()
		) {
			GuiBatchingController.INSTANCE.startBatching(guiGraphics);
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/p3pp3rf1y/sophisticatedcore/client/gui/StorageScreenBase;renderLabels(Lnet/minecraft/client/gui/GuiGraphics;II)V",
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
		if (		CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	()
		) {
			GuiBatchingController.INSTANCE.flushBatching(guiGraphics);
		}
	}
}
