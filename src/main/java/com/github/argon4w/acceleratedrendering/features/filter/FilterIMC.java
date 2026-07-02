package com.github.argon4w.acceleratedrendering.features.filter;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.InterModComms.IMCMessage;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;

import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings	("removal")
@EventBusSubscriber	(
		modid	= AcceleratedRenderingModEntry	.MOD_ID,
		value	= Dist							.CLIENT,
		bus		= Bus							.MOD
)
public class FilterIMC {

	public static Set<Supplier<MenuType			<?>>> MENU_TYPE_BLACKLIST_SUPPLIERS			= Set.of();
	public static Set<Supplier<EntityType		<?>>> ENTITY_TYPE_BLACKLIST_SUPPLIERS		= Set.of();
	public static Set<Supplier<BlockEntityType	<?>>> BLOCK_ENTITY_TYPE_BLACKLIST_SUPPLIERS	= Set.of();
	public static Set<Supplier<Item>				> ITEM_BLACKLIST_SUPPLIERS				= Set.of();
	public static Set<Supplier<String>				> STAGE_BLACKLIST_SUPPLIERS				= Set.of();

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void processIMC(InterModProcessEvent event) {
		var menuTypeBlacklistBuilder		= ImmutableSet.<Supplier<MenuType		<?>>>builder();
		var entityTypeBlacklistBuilder		= ImmutableSet.<Supplier<EntityType		<?>>>builder();
		var blockEntityTypeBlacklistBuilder	= ImmutableSet.<Supplier<BlockEntityType<?>>>builder();
		var itemBlacklistBuilder			= ImmutableSet.<Supplier<Item>				>builder();
		var stageBlacklistBuilder			= ImmutableSet.<Supplier<String>			>builder();

		event.getIMCStream("menu_type_blacklist"		::equals).map(IMCMessage::messageSupplier).forEach(supplier -> menuTypeBlacklistBuilder			.add((Supplier<MenuType			<?>	>) supplier));
		event.getIMCStream("entity_type_blacklist"		::equals).map(IMCMessage::messageSupplier).forEach(supplier -> entityTypeBlacklistBuilder		.add((Supplier<EntityType		<?>	>) supplier));
		event.getIMCStream("block_entity_type_blacklist"::equals).map(IMCMessage::messageSupplier).forEach(supplier -> blockEntityTypeBlacklistBuilder	.add((Supplier<BlockEntityType	<?>	>) supplier));
		event.getIMCStream("item_blacklist"				::equals).map(IMCMessage::messageSupplier).forEach(supplier -> itemBlacklistBuilder				.add((Supplier<Item					>) supplier));
		event.getIMCStream("stage_blacklist"			::equals).map(IMCMessage::messageSupplier).forEach(supplier -> stageBlacklistBuilder			.add((Supplier<String				>) supplier));

		MENU_TYPE_BLACKLIST_SUPPLIERS			= menuTypeBlacklistBuilder			.build();
		ENTITY_TYPE_BLACKLIST_SUPPLIERS			= entityTypeBlacklistBuilder		.build();
		BLOCK_ENTITY_TYPE_BLACKLIST_SUPPLIERS	= blockEntityTypeBlacklistBuilder	.build();
		ITEM_BLACKLIST_SUPPLIERS				= itemBlacklistBuilder				.build();
		STAGE_BLACKLIST_SUPPLIERS				= stageBlacklistBuilder				.build();
	}
}
