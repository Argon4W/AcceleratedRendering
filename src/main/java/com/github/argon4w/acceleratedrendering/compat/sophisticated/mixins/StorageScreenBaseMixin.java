package com.github.argon4w.acceleratedrendering.compat.sophisticated.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
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
			at		= @At("HEAD")
	)
	public void startBackgroundBatching(
			GuiGraphics								guiGraphics,
			int										mouseX,
			int										mouseY,
			float									partialTick,
			CallbackInfo							ci,
			@Share("depth")			LocalFloatRef	depth,
			@Share("accelerated")	LocalBooleanRef	accelerated
	) {
		if (		CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	()
		) {
			depth		.set(0.0f);
			accelerated	.set(GuiBatchingController.INSTANCE.startBatching(guiGraphics));
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					shift	= At.Shift.BEFORE,
					ordinal	= 0
			)
	)
	public void flushBackgroundBatching(
			GuiGraphics								guiGraphics,
			int										mouseX,
			int										mouseY,
			float									partialTick,
			CallbackInfo							ci,
			@Share("depth")			LocalFloatRef	depth,
			@Share("accelerated")	LocalBooleanRef	accelerated
	) {
		if (		CoreFeature						.isLoaded						()
				&&	ModsFeature						.isEnabled						()
				&&	ModsFeature						.shouldAccelerateSophisticated	()
				&& !AcceleratedItemRenderingFeature	.shouldMergeGuiItemBatches		()
				&&	accelerated						.get							()
		) {
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
			method	= "renderSuper",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/PoseStack;translate(DDD)V",
					shift	= At.Shift.AFTER,
					ordinal	= 0
			)
	)
	public void startItemBatching(
			GuiGraphics								guiGraphics,
			int										mouseX,
			int										mouseY,
			float									partialTick,
			CallbackInfo							ci,
			@Share("depth")			LocalFloatRef	depth,
			@Share("accelerated")	LocalBooleanRef	accelerated
	) {
		if (		CoreFeature						.isLoaded						()
				&&	ModsFeature						.isEnabled						()
				&&	ModsFeature						.shouldAccelerateSophisticated	()
				&& !AcceleratedItemRenderingFeature	.shouldMergeGuiItemBatches		()
				&&	accelerated						.get							()
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
			GuiGraphics								guiGraphics,
			int										mouseX,
			int										mouseY,
			float									partialTick,
			CallbackInfo							ci,
			@Share("depth")			LocalFloatRef	depth,
			@Share("accelerated")	LocalBooleanRef	accelerated
	) {
		if (		CoreFeature.isLoaded						()
				&&	ModsFeature.isEnabled						()
				&&	ModsFeature.shouldAccelerateSophisticated	()
				&&	accelerated.get								()
		) {
			depth.set(depth.get() + GuiBatchingController.INSTANCE.flushBatching(guiGraphics));
		}
	}

	@Inject(
			method	= "renderSuper",
			at		= @At("TAIL")
	)
	public void liftGlobalLayer(
			GuiGraphics								guiGraphics,
			int										mouseX,
			int										mouseY,
			float									partialTick,
			CallbackInfo							ci,
			@Share("depth")			LocalFloatRef	depth,
			@Share("accelerated")	LocalBooleanRef	accelerated
	) {
		if (accelerated.get()) {
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
}
