package com.github.argon4w.acceleratedrendering.features.items.mixins.compatibility;

import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeGui.class)
public class ForgeGuiMixin {

	@WrapOperation(
			method	= {
					"pre",
					"post"
			},
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"
			),
			remap	= false
	)
	private static boolean disableAdditionalGuiAcceleration(
			IEventBus			instance,
			Event				event,
			Operation<Boolean>	original
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
