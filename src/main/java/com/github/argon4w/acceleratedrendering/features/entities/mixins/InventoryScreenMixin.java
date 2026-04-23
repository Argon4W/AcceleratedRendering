package com.github.argon4w.acceleratedrendering.features.entities.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.CoreStates;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.platform.Lighting;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@ExtensionMethod(BufferSourceExtension	.class)
@Mixin			(InventoryScreen		.class)
public class InventoryScreenMixin {

	@WrapMethod(method = "lambda$renderEntityInInventory$1")
	private static void renderEntityInInventoryFast(
			EntityRenderDispatcher	entityRenderDispatcher,
			LivingEntity			entity,
			GuiGraphics				guiGraphics,
			Operation<Void>			operation
	) {
		if (		!guiGraphics.bufferSource().getAcceleratable()	.isBufferSourceAcceleratable	()
				||	!AcceleratedEntityRenderingFeature				.isEnabled						()
				||	!AcceleratedEntityRenderingFeature				.shouldUseAcceleratedPipeline	()
				||	!AcceleratedEntityRenderingFeature				.shouldAccelerateInGui			()
				||	!CoreFeature									.isLoaded						()
		) {
			operation.call(
					entityRenderDispatcher,
					entity,
					guiGraphics
			);
			return;
		}

		if (CoreFeature.isGuiBatching()) {
			CoreFeature.forceSetDefaultLayer				(2);
			CoreFeature.forceSetDefaultLayerBeforeFunction	(Lighting::setupForEntityInInventory);
			CoreFeature.forceSetDefaultLayerAfterFunction	(Lighting::setupFor3DItems);
		}

		CoreFeature.setRenderingGui();

		operation.call(
				entityRenderDispatcher,
				entity,
				guiGraphics
		);

		CoreFeature.resetRenderingGui();

		if (CoreFeature.isGuiBatching()) {
			CoreFeature.resetDefaultLayer				();
			CoreFeature.resetDefaultLayerBeforeFunction	();
			CoreFeature.resetDefaultLayerAfterFunction	();
		} else {
			CoreStates						.recordBuffers	();
			CoreBuffers.ENTITY				.prepareBuffers	();
			CoreBuffers.BLOCK				.prepareBuffers	();
			CoreBuffers.POS					.prepareBuffers	();
			CoreBuffers.POS_COLOR			.prepareBuffers	();
			CoreBuffers.POS_TEX				.prepareBuffers	();
			CoreBuffers.POS_TEX_COLOR		.prepareBuffers	();
			CoreBuffers.POS_COLOR_TEX_LIGHT	.prepareBuffers	();
			CoreStates						.restoreBuffers	();

			CoreBuffers.ENTITY				.drawBuffers	(LayerDrawType.ALL);
			CoreBuffers.BLOCK				.drawBuffers	(LayerDrawType.ALL);
			CoreBuffers.POS					.drawBuffers	(LayerDrawType.ALL);
			CoreBuffers.POS_COLOR			.drawBuffers	(LayerDrawType.ALL);
			CoreBuffers.POS_TEX				.drawBuffers	(LayerDrawType.ALL);
			CoreBuffers.POS_TEX_COLOR		.drawBuffers	(LayerDrawType.ALL);
			CoreBuffers.POS_COLOR_TEX_LIGHT	.drawBuffers	(LayerDrawType.ALL);

			CoreBuffers.ENTITY				.clearBuffers	();
			CoreBuffers.BLOCK				.clearBuffers	();
			CoreBuffers.POS					.clearBuffers	();
			CoreBuffers.POS_COLOR			.clearBuffers	();
			CoreBuffers.POS_TEX				.clearBuffers	();
			CoreBuffers.POS_TEX_COLOR		.clearBuffers	();
			CoreBuffers.POS_COLOR_TEX_LIGHT	.clearBuffers	();
		}
	}
}
