package com.github.argon4w.acceleratedrendering.features.items.mixins.compatibility;

import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings	("UnstableApiUsage")
@Mixin				(GuiLayerManager.class)
public class GuiLayerManagerMixin {

	@WrapOperation(
			method	= "render",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/neoforged/bus/api/IEventBus;post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;"
			)
	)
	private static Event disableAdditionalGuiAcceleration(
			IEventBus			instance,
			Event				event,
			Operation<Event>	original
	) {
		AcceleratedEntityRenderingFeature	.useVanillaPipeline	();
		AcceleratedItemRenderingFeature		.useVanillaPipeline	();
		AcceleratedTextRenderingFeature		.useVanillaPipeline	();

		var result = original.call(instance, event);

		AcceleratedEntityRenderingFeature	.resetPipeline		();
		AcceleratedItemRenderingFeature		.resetPipeline		();
		AcceleratedTextRenderingFeature		.resetPipeline		();

		return result;
	}
}
