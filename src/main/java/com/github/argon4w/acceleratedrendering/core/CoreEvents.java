package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import com.github.argon4w.acceleratedrendering.core.utils.AvailabilityUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
		modid	= AcceleratedRenderingModEntry	.MOD_ID,
		bus		= Mod.EventBusSubscriber.Bus	.MOD,
		value	= Dist							.CLIENT
)
public class CoreEvents {

	@SubscribeEvent
	public static void onClientPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
		if (!AvailabilityUtils.isAvailable()) {
			event.getPlayer().displayClientMessage(Component.translatable("acceleratedrendering.component.not_available").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
		}
	}
}
