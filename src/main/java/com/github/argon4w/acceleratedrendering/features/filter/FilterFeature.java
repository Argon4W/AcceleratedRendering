package com.github.argon4w.acceleratedrendering.features.filter;

import com.github.argon4w.acceleratedrendering.configs.FeatureConfig;
import com.github.argon4w.acceleratedrendering.configs.FeatureStatus;
import com.github.argon4w.acceleratedrendering.core.utils.RegistryFilter;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FilterFeature {

	private static final Deque	<FeatureStatus>			MENU_FILTER_CONTROLLER_STACK			= new ArrayDeque<>();
	private	static final Deque	<FeatureStatus>			ENTITIES_FILTER_CONTROLLER_STACK		= new ArrayDeque<>();
	private	static final Deque	<FeatureStatus>			BLOCK_ENTITIES_FILTER_CONTROLLER_STACK	= new ArrayDeque<>();
	private	static final Deque	<FeatureStatus>			ITEM_FILTER_CONTROLLER_STACK			= new ArrayDeque<>();
	private static final Deque	<FeatureStatus>			STAGE_FILTER_CONTROLLER_STACK			= new ArrayDeque<>();

	private static final Set	<MenuType<?>>			MENU_FILTER_VALUES;
	private	static final Set	<EntityType<?>>			ENTITY_FILTER_VALUES;
	private	static final Set	<BlockEntityType<?>>	BLOCK_ENTITY_FILTER_VALUES;
	private static final Set	<Item>					ITEM_FILTER_VALUES;
	private static final Set	<String>				STAGE_FILTER_VALUES;

	private static final Set	<MenuType<?>>			PREDEFINED_MENU_BLACKLIST_VALUES;
	private static final Set	<EntityType<?>>			PREDEFINED_ENTITY_BLACKLIST_VALUES;
	private static final Set	<BlockEntityType<?>>	PREDEFINED_BLOCK_ENTITY_BLACKLIST_VALUES;
	private static final Set	<Item>					PREDEFINED_ITEM_BLACKLIST_VALUES;
	private static final Set	<String>				PREDEFINED_STAGE_BLACKLIST_VALUES;

	private static final boolean						HAS_PREDEFINED_MENU_BLACKLIST;
	private static final boolean						HAS_PREDEFINED_ENTITY_BLACKLIST;
	private static final boolean						HAS_PREDEFINED_BLOCK_ENTITY_BLACKLIST;
	private static final boolean						HAS_PREDEFINED_ITEM_BLACKLIST;
	private static final boolean						HAS_PREDEFINED_STAGE_BLACKLIST;

	static {
		MENU_FILTER_VALUES			= RegistryFilter.filterValues	(BuiltInRegistries.MENU,				FeatureConfig.CONFIG.filterMenuFilterValues			.get());
		ENTITY_FILTER_VALUES		= RegistryFilter.filterValues	(BuiltInRegistries.ENTITY_TYPE,			FeatureConfig.CONFIG.filterEntityFilterValues		.get());
		BLOCK_ENTITY_FILTER_VALUES	= RegistryFilter.filterValues	(BuiltInRegistries.BLOCK_ENTITY_TYPE,	FeatureConfig.CONFIG.filterBlockEntityFilterValues	.get());
		ITEM_FILTER_VALUES			= RegistryFilter.filterValues	(BuiltInRegistries.ITEM,				FeatureConfig.CONFIG.filterItemFilterValues			.get());
		STAGE_FILTER_VALUES			= new ReferenceOpenHashSet<>	(										FeatureConfig.CONFIG.filterStageFilterValues		.get());

		PREDEFINED_MENU_BLACKLIST_VALUES			= FilterIMC.MENU_TYPE_BLACKLIST_SUPPLIERS			.stream().map(Supplier::get).collect(Collectors.toCollection(ReferenceOpenHashSet::new));
		PREDEFINED_ENTITY_BLACKLIST_VALUES			= FilterIMC.ENTITY_TYPE_BLACKLIST_SUPPLIERS			.stream().map(Supplier::get).collect(Collectors.toCollection(ReferenceOpenHashSet::new));
		PREDEFINED_BLOCK_ENTITY_BLACKLIST_VALUES	= FilterIMC.BLOCK_ENTITY_TYPE_BLACKLIST_SUPPLIERS	.stream().map(Supplier::get).collect(Collectors.toCollection(ReferenceOpenHashSet::new));
		PREDEFINED_ITEM_BLACKLIST_VALUES			= FilterIMC.ITEM_BLACKLIST_SUPPLIERS				.stream().map(Supplier::get).collect(Collectors.toCollection(ReferenceOpenHashSet::new));
		PREDEFINED_STAGE_BLACKLIST_VALUES			= FilterIMC.STAGE_BLACKLIST_SUPPLIERS				.stream().map(Supplier::get).collect(Collectors.toCollection(ReferenceOpenHashSet::new));

		HAS_PREDEFINED_MENU_BLACKLIST			= !PREDEFINED_MENU_BLACKLIST_VALUES			.isEmpty();
		HAS_PREDEFINED_ENTITY_BLACKLIST			= !PREDEFINED_ENTITY_BLACKLIST_VALUES		.isEmpty();
		HAS_PREDEFINED_BLOCK_ENTITY_BLACKLIST	= !PREDEFINED_BLOCK_ENTITY_BLACKLIST_VALUES	.isEmpty();
		HAS_PREDEFINED_ITEM_BLACKLIST			= !PREDEFINED_ITEM_BLACKLIST_VALUES			.isEmpty();
		HAS_PREDEFINED_STAGE_BLACKLIST			= !PREDEFINED_STAGE_BLACKLIST_VALUES		.isEmpty();
	}

	public static boolean isEnabled() {
		return FeatureConfig.CONFIG.filterFeatureStatus.get() == FeatureStatus.ENABLED;
	}

	public static boolean testMenu(AbstractContainerMenu menu) {
		return menu.menuType == null || (!PREDEFINED_MENU_BLACKLIST_VALUES.contains(menu.menuType) && getMenuFilterType().test(MENU_FILTER_VALUES, menu.menuType));
	}

	public static boolean testEntity(Entity entity) {
		return !PREDEFINED_ENTITY_BLACKLIST_VALUES.contains(entity.getType()) && getEntityFilterType().test(ENTITY_FILTER_VALUES, entity.getType());
	}

	public static boolean testBlockEntity(BlockEntity entity) {
		return !PREDEFINED_BLOCK_ENTITY_BLACKLIST_VALUES.contains(entity.getType()) && getBlockEntityFilterType().test(BLOCK_ENTITY_FILTER_VALUES, entity.getType());
	}

	public static boolean testItem(ItemStack itemStack) {
		return !PREDEFINED_ITEM_BLACKLIST_VALUES.contains(itemStack.getItem()) && getItemFilterType().test(ITEM_FILTER_VALUES, itemStack.getItem());
	}

	public static boolean testStage(RenderLevelStageEvent.Stage stage) {
		return !PREDEFINED_STAGE_BLACKLIST_VALUES.contains(stage.toString()) && getStageFilterType().test(STAGE_FILTER_VALUES, stage.toString());
	}

	public static boolean shouldFilterMenus() {
		return getMenuFilterSetting() == FeatureStatus.ENABLED || HAS_PREDEFINED_MENU_BLACKLIST;
	}

	public static boolean shouldFilterEntities() {
		return getEntityFilterSetting() == FeatureStatus.ENABLED || HAS_PREDEFINED_ENTITY_BLACKLIST;
	}

	public static boolean shouldFilterBlockEntities() {
		return getBlockEntityFilterSetting() == FeatureStatus.ENABLED || HAS_PREDEFINED_BLOCK_ENTITY_BLACKLIST;
	}

	public static boolean shouldFilterItems() {
		return getItemFilterSetting() == FeatureStatus.ENABLED || HAS_PREDEFINED_ITEM_BLACKLIST;
	}

	public static boolean shouldFilterStage() {
		return getStageFilterSetting() == FeatureStatus.ENABLED || HAS_PREDEFINED_STAGE_BLACKLIST;
	}

	public static FilterType getMenuFilterType() {
		return FeatureConfig.CONFIG.filterMenuFilterType.get();
	}

	public static FilterType getEntityFilterType() {
		return FeatureConfig.CONFIG.filterEntityFilterType.get();
	}

	public static FilterType getBlockEntityFilterType() {
		return FeatureConfig.CONFIG.filterBlockEntityFilterType.get();
	}

	public static FilterType getItemFilterType() {
		return FeatureConfig.CONFIG.filterItemFilterType.get();
	}

	public static FilterType getStageFilterType() {
		return FeatureConfig.CONFIG.filterStageFilterType.get();
	}

	public static void disableMenuFilter() {
		MENU_FILTER_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void disableEntityFilter() {
		ENTITIES_FILTER_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void disableBlockEntityFilter() {
		BLOCK_ENTITIES_FILTER_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void disableItemFilter() {
		ITEM_FILTER_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void disableStageFilter() {
		STAGE_FILTER_CONTROLLER_STACK.push(FeatureStatus.DISABLED);
	}

	public static void forceEnableMenuFilter() {
		MENU_FILTER_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceEnableEntityFilter() {
		ENTITIES_FILTER_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceEnableBlockEntityFilter() {
		BLOCK_ENTITIES_FILTER_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceEnableItemFilter() {
		ITEM_FILTER_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceEnableStageFilter() {
		STAGE_FILTER_CONTROLLER_STACK.push(FeatureStatus.ENABLED);
	}

	public static void forceSetMenuFilter(FeatureStatus status) {
		MENU_FILTER_CONTROLLER_STACK.push(status);
	}

	public static void forceSetEntityFilter(FeatureStatus status) {
		ENTITIES_FILTER_CONTROLLER_STACK.push(status);
	}

	public static void forceSetBlockEntityFilter(FeatureStatus status) {
		BLOCK_ENTITIES_FILTER_CONTROLLER_STACK.push(status);
	}

	public static void forceSetItemFilter(FeatureStatus status) {
		ITEM_FILTER_CONTROLLER_STACK.push(status);
	}

	public static void forceSetStageFilter(FeatureStatus status) {
		STAGE_FILTER_CONTROLLER_STACK.push(status);
	}

	public static void resetMenuFilter() {
		MENU_FILTER_CONTROLLER_STACK.pop();
	}

	public static void resetEntityFilter() {
		ENTITIES_FILTER_CONTROLLER_STACK.pop();
	}

	public static void resetBlockEntityFilter() {
		BLOCK_ENTITIES_FILTER_CONTROLLER_STACK.pop();
	}

	public static void resetItemFilter() {
		ITEM_FILTER_CONTROLLER_STACK.pop();
	}

	public static void resetStageFilter() {
		STAGE_FILTER_CONTROLLER_STACK.pop();
	}

	public static FeatureStatus getMenuFilterSetting() {
		return MENU_FILTER_CONTROLLER_STACK.isEmpty() ? getDefaultMenuFilterSetting() : MENU_FILTER_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getEntityFilterSetting() {
		return ENTITIES_FILTER_CONTROLLER_STACK.isEmpty() ? getDefaultEntityFilterSetting() : ENTITIES_FILTER_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getBlockEntityFilterSetting() {
		return BLOCK_ENTITIES_FILTER_CONTROLLER_STACK.isEmpty() ? getDefaultBlockEntityFilterSetting() : BLOCK_ENTITIES_FILTER_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getItemFilterSetting() {
		return ITEM_FILTER_CONTROLLER_STACK.isEmpty() ? getDefaultItemFilterSetting() : ITEM_FILTER_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getStageFilterSetting() {
		return STAGE_FILTER_CONTROLLER_STACK.isEmpty() ? getDefaultStageFilterSetting() : STAGE_FILTER_CONTROLLER_STACK.peek();
	}

	public static FeatureStatus getDefaultMenuFilterSetting() {
		return FeatureConfig.CONFIG.filterMenuFilter.get();
	}

	public static FeatureStatus getDefaultEntityFilterSetting() {
		return FeatureConfig.CONFIG.filterEntityFilter.get();
	}

	public static FeatureStatus getDefaultBlockEntityFilterSetting() {
		return FeatureConfig.CONFIG.filterBlockEntityFilter.get();
	}

	public static FeatureStatus getDefaultItemFilterSetting() {
		return FeatureConfig.CONFIG.filterItemFilter.get();
	}

	public static FeatureStatus getDefaultStageFilterSetting() {
		return FeatureConfig.CONFIG.filterStageFilter.get();
	}
}
