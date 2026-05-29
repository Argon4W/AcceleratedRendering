package com.github.argon4w.acceleratedrendering.features.items.mixins.compatibility;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ForgeHooksClient.class)
public class ClientHooksMixin {

	@WrapOperation(
			method	= "drawScreenInternal",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"
			),
			remap	= false
	)
	private static boolean disableAdditionalScreenAcceleration(
			IEventBus			instance,
			Event				event,
			Operation<Boolean>	original
	) {
		if (!CoreFeature.isLoaded()) {
			return original.call(instance, event);
		}

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
