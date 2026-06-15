package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
		modid	= AcceleratedRenderingModEntry	.MOD_ID,
		value	= Dist							.CLIENT,
		bus		= Bus							.MOD
)
public class CoreModEvents {

	@SubscribeEvent
	public static void onRegisterClientReloadListener(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(new CoreReloads());
	}
}
