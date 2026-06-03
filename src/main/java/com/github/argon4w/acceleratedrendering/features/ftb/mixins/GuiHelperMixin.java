package com.github.argon4w.acceleratedrendering.features.ftb.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ftb.mods.ftblibrary.ui.GuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GuiHelper.class)
public class GuiHelperMixin {

	@WrapMethod(
			method	= "drawItem",
			remap	= false
	)
	private static void drawItem(
			GuiGraphics	graphics,
			ItemStack	stack,
			int			hash,
			boolean		renderOverlay,
			String		text,
			Operation<Void> original
	) {
		if (		!	stack		.isEmpty			()
				&&		CoreFeature	.isLoaded			()
				&&		ModsFeature	.isEnabled			()
				&&		ModsFeature	.shouldAccelerateFtb()
		) {
			var pose = graphics.pose();

			pose.pushPose	();
			pose.translate	(
					-8.0F,
					-8.0F,
					-150.0F
			);

			pose
					.last		()
					.normal		()
					.identity	();

			graphics.renderItem(stack, 0, 0);

			if (renderOverlay) {
				graphics.renderItemDecorations(
						Minecraft.getInstance().font,
						stack,
						0,
						0,
						text
				);
			}

			graphics.pose().popPose();
		} else {
			original.call(
					graphics,
					stack,
					hash,
					renderOverlay,
					text
			);
		}
	}
}
