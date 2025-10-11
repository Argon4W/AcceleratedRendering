package com.github.argon4w.acceleratedrendering.configs;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage.LayerStorageType;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshInfoCacheType;
import com.github.argon4w.acceleratedrendering.core.buffers.blocks.states.BlockBufferBindingStateType;
import com.github.argon4w.acceleratedrendering.core.buffers.blocks.cache.BlockBufferBindingCacheType;
import com.github.argon4w.acceleratedrendering.core.meshes.MeshType;
import com.github.argon4w.acceleratedrendering.features.filter.FilterType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class FeatureConfig {

	public static	final	FeatureConfig												CONFIG;
	public static	final	ModConfigSpec												SPEC;

	public			final	ModConfigSpec.IntValue										corePooledRingBufferSize;
	public			final	ModConfigSpec.IntValue										corePooledBatchingSize;
	public			final	ModConfigSpec.IntValue										coreCachedImageSize;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					coreDebugContextEnabled;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					coreForceTranslucentAcceleration;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					coreCacheIdenticalPose;
	public			final	ModConfigSpec.ConfigValue<MeshInfoCacheType>				coreMeshInfoCacheType;
	public			final	ModConfigSpec.ConfigValue<LayerStorageType>					coreLayerStorageType;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					coreUploadMeshImmediately;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					restoringFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<BlockBufferBindingCacheType>		restoringBindingCacheType;
	public			final	ModConfigSpec.ConfigValue<BlockBufferBindingStateType>	restoringShaderStorageType;
	public			final	ModConfigSpec.ConfigValue<BlockBufferBindingStateType>	restoringAtomicCounterType;
	public			final	ModConfigSpec.IntValue										restoringShaderStorageRange;
	public			final	ModConfigSpec.IntValue										restoringAtomicCounterRange;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedEntityRenderingFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<PipelineSetting>					acceleratedEntityRenderingDefaultPipeline;
	public			final	ModConfigSpec.ConfigValue<MeshType>							acceleratedEntityRenderingMeshType;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedEntityRenderingGuiAcceleration;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedTextRenderingFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<PipelineSetting>					acceleratedTextRenderingDefaultPipeline;
	public			final	ModConfigSpec.ConfigValue<MeshType>							acceleratedTextRenderingMeshType;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedItemRenderingFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedItemRenderingBakeMeshForQuads;
	public			final	ModConfigSpec.ConfigValue<PipelineSetting>					acceleratedItemRenderingDefaultPipeline;
	public			final	ModConfigSpec.ConfigValue<MeshType>							acceleratedItemRenderingMeshType;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedItemRenderingHandAcceleration;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedItemRenderingGuiAcceleration;
	public 			final	ModConfigSpec.ConfigValue<FeatureStatus>					acceleratedItemRenderingGuiItemBatching;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					orientationCullingFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					orientationCullingDefaultCulling;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					orientationCullingIgnoreCullState;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					filterFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					filterEntityFilter;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					filterBlockEntityFilter;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					filterItemFilter;
	public			final	ModConfigSpec.ConfigValue<FilterType>						filterEntityFilterType;
	public			final	ModConfigSpec.ConfigValue<FilterType>						filterBlockEntityFilterType;
	public			final	ModConfigSpec.ConfigValue<FilterType>						filterItemFilterType;
	public			final	ModConfigSpec.ConfigValue<List<? extends String>>			filterEntityFilterValues;
	public			final	ModConfigSpec.ConfigValue<List<? extends String>>			filterBlockEntityFilterValues;
	public			final	ModConfigSpec.ConfigValue<List<? extends String>>			filterItemFilterValues;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					irisCompatFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					irisCompatOrientationCullingCompat;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					irisCompatShadowCulling;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					irisCompatPolygonProcessing;

	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					curiosCompatFeatureStatus;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					curiosCompatLayerAcceleration;
	public			final	ModConfigSpec.ConfigValue<FeatureStatus>					curiosItemFilter;
	public			final	ModConfigSpec.ConfigValue<FilterType>						curiosItemFilterType;
	public			final	ModConfigSpec.ConfigValue<List<? extends String>>			curiosItemFilterValues;

	static {
		Pair<FeatureConfig, ModConfigSpec> pair	= new ModConfigSpec.Builder()	.configure	(FeatureConfig::new);
		CONFIG									= pair							.getLeft	();
		SPEC									= pair							.getRight	();
	}

	private FeatureConfig(ModConfigSpec.Builder builder) {
		builder
				.comment				("Core Settings")
				.comment				("Core Settings allows you to change setting that are related to all rendering features.")
				.translation			("acceleratedrendering.configuration.core_settings")
				.push					("core_settings");

		corePooledRingBufferSize						= builder
				.gameRestart			()
				.comment				("Count of buffer sets that holds data for in-flight frame rendering.")
				.comment				("Changing this value may affects your FPS. Smaller value means less in-flight frames, while larger values means more in-flight frames. More in-flight frames means more FPS but more VRAM.")
				.translation			("acceleratedrendering.configuration.core_settings.pooled_ring_buffer_size")
				.defineInRange			("pooled_ring_buffer_size",		8,	1,	Integer.MAX_VALUE);

		corePooledBatchingSize							= builder
				.gameRestart			()
				.comment				("Count of batches of RenderTypes that is allowed in a draw call.")
				.comment				("Changing this value may affects your FPS. Smaller value means less batches allowed in a draw call, while larger values means more batches. More batches means more FPS but more VRAM and more CPU pressure on handling RenderTypes.")
				.translation			("acceleratedrendering.configuration.core_settings.pooled_batching_size")
				.defineInRange			("pooled_batching_size",		32,	1,	Integer.MAX_VALUE);

		coreCachedImageSize								= builder
				.comment				("Count of images that cached for static mesh culling.")
				.comment				("Changing this value may affects your FPS. Smaller value means less images allowed to be cached, while larger means more cached images. More cached images means more FPS but more RAM pressure.")
				.translation			("acceleratedrendering.configuration.core_settings.cached_image_size")
				.defineInRange			("cached_image_size",			32,	1,	Integer.MAX_VALUE);

		coreDebugContextEnabled							= builder
				.comment				("- DISABLED: Debug context will be disabled, which may cause significant rendering glitches on some NVIDIA cards because of the \"theaded optimization\".")
				.comment				("- ENABLED: Debug context will be enabled, which can prevent NVIDIA driver from applying the \"threaded optimization\" that causes the glitches.")
				.translation			("acceleratedrendering.configuration.core_settings.debug_context")
				.gameRestart			()
				.defineEnum				("debug_context",						FeatureStatus.ENABLED);

		coreForceTranslucentAcceleration				= builder
				.comment				("- DISABLED: Translucent RenderType will fallback to vanilla rendering pipeline if the accelerated pipeline does not support translucent sorting unless mods explicitly enable force translucent acceleration temporarily when rendering their own faces.")
				.comment				("- ENABLED: Translucent RenderType will still be rendered in accelerated pipeline even if the pipeline does not support translucent sorting unless mods explicitly disable force translucent acceleration temporarily when rendering their own faces.")
				.translation			("acceleratedrendering.configuration.core_settings.force_translucent_acceleration")
				.defineEnum				("force_translucent_acceleration",		FeatureStatus.ENABLED);

		coreCacheIdenticalPose							= builder
				.comment				("- DISABLED: Poses with identical transform matrix and normal matrix that used to transform vertices will not be cached in buffer which slightly decreases CPU pressure but increase VRAM usage unless mods explicitly disable it when rendering.")
				.comment				("- ENABLED: Poses with identical transform matrix and normal matrix that used to transform vertices will be cached in buffer which save VRAM but slightly increase CPU pressure unless mods explicitly disable it when rendering.")
				.translation			("acceleratedrendering.configuration.core_settings.cache_identical_pose")
				.defineEnum				("cache_identical_pose",				FeatureStatus.ENABLED);

		coreMeshInfoCacheType							= builder
				.comment				("- SIMPLE: The most basic implementation of cache. Usually used for testing if other cache types are working correctly.")
				.comment				("- HANDLE: Faster implementation of cache using VarHandle and flatten values to improve performance on read/write operations.")
				.comment				("- UNSAFE: Fastest implementation of cache using unsafe memory operations that skip multiple safety checks to read/write.")
				.translation			("acceleratedrendering.configuration.core_settings.mesh_info_cache_type")
				.gameRestart			()
				.defineEnum				("mesh_info_cache_type",				MeshInfoCacheType.HANDLE);

		coreLayerStorageType							= builder
				.comment				("- SORTED: The basic implementation of batching layer storage that renders opaque and translucent geometries together in a single stage with better performance but slight visual glitches on translucent geometries.")
				.comment				("- SEPARATED: The visually-precise implementation of batching layer storage that separates opaque and translucent geometries into two rendering stages to prevent visual glitches, slightly slower than basic implementation.")
				.translation			("acceleratedrendering.configuration.core_settings.layer_storage_type")
				.gameRestart			()
				.defineEnum				("layer_storage_type",					LayerStorageType.SEPARATED);

		coreUploadMeshImmediately						= builder
				.comment				("- DISABLED: Meshes that is going to be accelerated will be collected and uploaded together at the end for choosing better uploading method and increasing memory access efficiency to reach the best performance. Also this method allows mesh cache with bigger capacity (up to VRAM limit), but it may not follow the correct draw order.")
				.comment				("- ENABLED: Meshes that is going to be accelerated will be uploaded immediately after the draw command. It is less efficient and only have about 2GB mesh cache (generally enough) but will follow the original draw order to get the most compatibility.")
				.translation			("acceleratedrendering.configuration.core_settings.upload_mesh_immediately")
				.defineEnum				("upload_mesh_immediately",				FeatureStatus.DISABLED);

		builder
				.comment				("Block Buffer Restoring Settings")
				.comment				("A few mods and shader packs will use their on block buffers when rendering, which may introduce conflicts when working with Accelerated Rendering that also uses block buffers.")
				.comment				("Block Buffer Restoring can record the binding of block buffers before the acceleration and restore them after the acceleration to work correctly with them.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring")
				.push					("block_buffer_binding_restoring");

		restoringFeatureStatus							= builder
				.comment				("- DISABLED: Disable block buffer restoring, which is faster but may cause visual glitches with mods and shaders that uses block buffers.")
				.comment				("- ENABLED: Enable block buffer restoring, which may be slower due to recording and restoring block buffer bindings that ensures working correctly with mods and shaders that use block buffers.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.ENABLED);

		restoringBindingCacheType						= builder
				.comment				("- SIMPLE: The most basic implementation of cache. Usually used for testing if other cache types are working properly.")
				.comment				("- HANDLE: Faster implementation of cache using VarHandle and flatten values to improve performance on read/write operations.")
				.comment				("- UNSAFE: Fastest implementation of cache using unsafe memory operations that skip multiple safety checks to read/write.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring.binding_cache_type")
				.gameRestart			()
				.defineEnum				("binding_cache_type",					BlockBufferBindingCacheType.HANDLE);

		restoringShaderStorageType						= builder
				.comment				("- IGNORE: Shader storage buffers will not be restored which improves FPS but reduces compatibility with mods and shaders that ues shader storage buffers.")
				.comment				("- ENABLED: Shader storage buffers will be restored, which is slight slower but has better compatibility with mods and shaders that ues shader storage buffers.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring.shader_storage_type")
				.gameRestart			()
				.defineEnum				("shader_storage_type",					BlockBufferBindingStateType.RESTORED);

		restoringAtomicCounterType						= builder
				.comment				("- IGNORE: Atomic counter buffers will not be restored which improves FPS but reduces compatibility with mods and shaders that ues atomic counter buffers.")
				.comment				("- ENABLED: Atomic counter buffers will be restored, which is slight slower but has better compatibility with mods and shaders that ues atomic counter buffers.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring.atomic_counter_type")
				.gameRestart			()
				.defineEnum				("atomic_counter_type",					BlockBufferBindingStateType.RESTORED);

		restoringShaderStorageRange						= builder
				.comment				("Range of shader storage buffer bindings that will be restored.")
				.comment				("Changing this value may affects your FPS. Smaller value means less shader storage buffer restored but less compatibility, while larger values means more shader storage buffer restored and better compatibility. More shader storage buffers means less FPS.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring.shader_storage_range")
				.gameRestart			()
				.defineInRange			("shader_storage_range",		9,	0,	9);

		restoringAtomicCounterRange						= builder
				.comment				("Range of atomic counter buffer bindings that will be restored.")
				.comment				("Changing this value may affects your FPS. Smaller value means less atomic counter buffer restored but less compatibility, while larger values means more atomic counter buffer restored and better compatibility. More atomic counter buffers means less FPS.")
				.translation			("acceleratedrendering.configuration.core_settings.block_buffer_binding_restoring.atomic_counter_range")
				.gameRestart			()
				.defineInRange			("atomic_counter_range",		1,	0,	1);

		builder.pop();

		builder.pop();

		builder
				.comment				("Accelerated Entity Rendering Settings")
				.comment				("Accelerated Entity Rendering uses GPU to cache and transform vertices while rendering entities, instead of generating and transforming vertices every time the entities are rendered in CPU.")
				.translation			("acceleratedrendering.configuration.accelerated_entity_rendering")
				.push					("accelerated_entity_rendering");

		acceleratedEntityRenderingFeatureStatus			= builder
				.comment				("- DISABLED: Disable accelerated entity rendering.")
				.comment				("- ENABLED: Enable accelerated entity rendering.")
				.translation			("acceleratedrendering.configuration.accelerated_entity_rendering.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.ENABLED);

		acceleratedEntityRenderingDefaultPipeline		= builder
				.comment				("- VANILLA: Entities will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own entities.")
				.comment				("- ACCELERATED: All entities will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own entities.")
				.translation			("acceleratedrendering.configuration.accelerated_entity_rendering.default_pipeline")
				.defineEnum				("default_pipeline",					PipelineSetting.ACCELERATED);

		acceleratedEntityRenderingMeshType				= builder
				.gameRestart			()
				.comment				("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
				.comment				("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
				.translation			("acceleratedrendering.configuration.accelerated_entity_rendering.mesh_type")
				.defineEnum				("mesh_type",							MeshType.SERVER);

		acceleratedEntityRenderingGuiAcceleration		= builder
				.comment				("- DISABLED: Accelerated Rendering will not accelerate entities when rendering it in a GUI unless mods explicitly enable it temporarily when rendering their own entities.")
				.comment				("- ENABLED: Accelerated Rendering will still accelerate entities when rendering it in a GUI unless mods explicitly disable it temporarily when rendering their own entities.")
				.translation			("acceleratedrendering.configuration.accelerated_entity_rendering.gui_acceleration")
				.defineEnum				("gui_acceleration",					FeatureStatus.ENABLED);

		builder.pop();

		builder
				.comment				("Accelerated Item Rendering Settings")
				.comment				("Accelerated Item Rendering uses GPU to cache and transform vertices while rendering item models, instead of generating and transforming vertices every time the item models are rendered in CPU.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering")
				.push					("accelerated_item_rendering");

		acceleratedItemRenderingFeatureStatus			= builder
				.comment				("- DISABLED: Disable accelerated item rendering.")
				.comment				("- ENABLED: Enable accelerated item rendering.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.ENABLED);

		acceleratedItemRenderingBakeMeshForQuads		= builder
				.comment				("- DISABLED: Accelerated Rendering will not bake mesh for quads provided by dynamic item models (something that is not SimpleBakedModel) unless mods explicitly enable it temporarily when rendering their own item models.")
				.comment				("- ENABLED: Accelerated Rendering will bake mesh for all quads provided by dynamic item models (something that is not SimpleBakedModel) unless mods explicitly disable it temporarily when rendering their own item models, which will accelerate the rendering of these models but will crash if they keep allocating new quad data. (but who will?)")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.bake_mesh_for_quads")
				.defineEnum				("bake_mesh_for_quads",					FeatureStatus.ENABLED);

		acceleratedItemRenderingDefaultPipeline			= builder
				.comment				("- VANILLA: Item models will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own item models.")
				.comment				("- ACCELERATED: All item models will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own item models.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.default_pipeline")
				.defineEnum				("default_pipeline",					PipelineSetting.ACCELERATED);

		acceleratedItemRenderingMeshType				= builder
				.gameRestart			()
				.comment				("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
				.comment				("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.mesh_type")
				.defineEnum				("mesh_type",							MeshType.SERVER);

		acceleratedItemRenderingHandAcceleration		= builder
				.comment				("- DISABLED: Accelerated Rendering will not accelerate item models that are marked as \"too small to make up the cost of acceleration\" when rendering it in hand unless mods explicitly enable it temporarily when rendering their own item models.")
				.comment				("- ENABLED: Accelerated Rendering will still accelerate item models that are marked as \"too small to make up the cost of acceleration\" when rendering it in hand unless mods explicitly disable it temporarily when rendering their own item models, which may slightly reduce the FPS but accelerate vanilla-like modded item models with large amount of vertices.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.hand_acceleration")
				.defineEnum				("hand_acceleration",					FeatureStatus.ENABLED);

		acceleratedItemRenderingGuiAcceleration			= builder
				.comment				("- DISABLED: Accelerated Rendering will not accelerate item models that are marked as \"too small to make up the cost of acceleration\" when rendering it in a container GUI unless mods explicitly enable it temporarily when rendering their own item models.")
				.comment				("- ENABLED: Accelerated Rendering will still accelerate item models that are marked as \"too small to make up the cost of acceleration\" when rendering it in a container GUI unless mods explicitly disable it temporarily when rendering their own item models, which may slightly reduce the FPS but accelerate vanilla-like modded item models with large amount of vertices.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.gui_acceleration")
				.defineEnum				("gui_acceleration",					FeatureStatus.ENABLED);

		acceleratedItemRenderingGuiItemBatching			= builder
				.comment				("- DISABLED: Items in the container GUI will be rendered as per item per batch if the GUI Acceleration is enabled, which is inefficient and may cause slight reduction in FPS, but it has better compatibility in modded container GUI.")
				.comment				("- ENABLED: Items in the container will be rendered together in a single batch if the GUI Acceleration is enabled, which is much more efficient but has little compatibility in modded container GUI.")
				.translation			("acceleratedrendering.configuration.accelerated_item_rendering.gui_item_batching")
				.defineEnum				("gui_item_batching",					FeatureStatus.ENABLED);

		builder.pop();

		builder
				.comment				("Accelerated Text Rendering Settings")
				.comment				("Accelerated Text Rendering uses GPU to cache and transform vertices while rendering text through BakedGlyph, instead of generating and transforming vertices every time the text are rendered in CPU.")
				.translation			("acceleratedrendering.configuration.accelerated_text_rendering")
				.push					("accelerated_text_rendering");

		acceleratedTextRenderingFeatureStatus			= builder
				.comment				("- DISABLED: Disable accelerated text rendering.")
				.comment				("- ENABLED: Enable accelerated text rendering.")
				.translation			("acceleratedrendering.configuration.accelerated_text_rendering.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.ENABLED);

		acceleratedTextRenderingDefaultPipeline			= builder
				.comment				("- VANILLA: Text will not be rendered into the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own text.")
				.comment				("- ACCELERATED: All text will be rendered in the accelerated pipeline unless mods explicitly disable it temporarily when rendering their own text.")
				.translation			("acceleratedrendering.configuration.accelerated_text_rendering.default_pipeline")
				.defineEnum				("default_pipeline",					PipelineSetting.ACCELERATED);

		acceleratedTextRenderingMeshType				= builder
				.gameRestart			()
				.comment				("- CLIENT: Cached mesh will be stored on the client side (CPU), which will use less VRAM but take more time to upload to the server side (GPU) during rendering.")
				.comment				("- SERVER: Cached mesh will be stored on the server side (GPU), which may speed up rendering but will use more VRAM to store the mesh.")
				.translation			("acceleratedrendering.configuration.accelerated_text_rendering.mesh_type")
				.defineEnum				("mesh_type",							MeshType.SERVER);

		builder.pop();

		builder
				.comment				("Simple Orientation Face Culling Settings")
				.comment				("Simple Orientation face culling uses an compute shader before the draw call to discard faces that is not visible on screen by checking if it is facing to the screen using a determinant of 3 * 3 matrix.")
				.translation			("acceleratedrendering.configuration.orientation_culling")
				.push					("orientation_culling");

		orientationCullingFeatureStatus					= builder
				.comment				("- DISABLED: Disable simple orientation face culling.")
				.comment				("- ENABLED: Enable simple orientation face culling.")
				.translation			("acceleratedrendering.configuration.orientation_culling.feature_status")
				.defineEnum				("feature_Status",						FeatureStatus.ENABLED);

		orientationCullingDefaultCulling				= builder
				.comment				("- DISABLED: Faces will not be culled unless mods explicitly enable it temporarily when rendering their own faces.")
				.comment				("- ENABLED: All faces will be culled unless mods explicitly disable it temporarily when rendering their own faces.")
				.translation			("acceleratedrendering.configuration.orientation_culling.default_culling")
				.defineEnum				("default_culling",						FeatureStatus.ENABLED);

		orientationCullingIgnoreCullState				= builder
				.comment				("- DISABLED: Simple orientation face culling will not cull entities that are not declared as \"cullable\".")
				.comment				("- ENABLED: Simple orientation face culling will cull all entities even if they are not declared as \"cullable\".")
				.translation			("acceleratedrendering.configuration.orientation_culling.ignore_cull_state")
				.defineEnum				("ignore_cull_state",					FeatureStatus.DISABLED);

		builder.pop();

		builder
				.comment				("Filters Settings")
				.comment				("Filters allows you to prevent specific entities/block entities/items from being accelerated when rendering for better compatibility.")
				.translation			("acceleratedrendering.configuration.filter")
				.push					("filter");

		filterFeatureStatus								= builder
				.comment				("- DISABLED: Filters will be disabled and all entities, block entities and items will be accelerated when rendering.")
				.comment				("- ENABLED: Filters will test if the entities, block entities and items should be accelerated when rendering based on the filter values and the filter type.")
				.translation			("acceleratedrendering.configuration.filter.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.DISABLED);

		filterEntityFilter								= builder
				.comment				("- DISABLED: Entity filter will be disabled and all entities will be accelerated.")
				.comment				("- ENABLED: Entity filter will test if the entities should be accelerated when rendering based on the filter values and the filter type.")
				.translation			("acceleratedrendering.configuration.filter.entity_filter")
				.defineEnum				("entity_filter",						FeatureStatus.DISABLED);

		filterBlockEntityFilter							= builder
				.comment				("- DISABLED: Block entity filter will be disabled and all block entities will be accelerated.")
				.comment				("- ENABLED: Block entity filter will test if the block entities should be accelerated when rendering based on the filter values and the filter type.")
				.translation			("acceleratedrendering.configuration.filter.block_entity_filter")
				.defineEnum				("block_entity_filter",					FeatureStatus.DISABLED);

		filterItemFilter								= builder
				.comment				("- DISABLED: Item filter will be disabled and all items will be accelerated.")
				.comment				("- ENABLED: Item filter will test if the items should be accelerated when rendering based on the filter values and the filter type.")
				.translation			("acceleratedrendering.configuration.filter.item_filter")
				.defineEnum				("item_filter",							FeatureStatus.DISABLED);

		filterEntityFilterType							= builder
				.comment				("- BLACKLIST: Entities that are not in the filter values can pass the filter and be accelerated when rendering.")
				.comment				("- WHITELIST: Entities that are in the filter values can pass the filter and be accelerated when rendering.")
				.translation			("acceleratedrendering.configuration.filter.entity_filter_type")
				.defineEnum				("entity_filter_type",					FilterType.BLACKLIST);

		filterBlockEntityFilterType						= builder
				.comment				("- BLACKLIST: Block entities that are not in the filter values can pass the filter and be accelerated when rendering.")
				.comment				("- WHITELIST: Block entities that are in the filter values can pass the filter and be accelerated when rendering.")
				.translation			("acceleratedrendering.configuration.filter.block_entity_filter_type")
				.defineEnum				("block_entity_filter_type",			FilterType.BLACKLIST);

		filterItemFilterType							= builder
				.comment				("- BLACKLIST: Items that are not in the filter values can pass the filter and be accelerated when rendering.")
				.comment				("- WHITELIST: Items that are in the filter values can pass the filter and be accelerated when rendering.")
				.translation			("acceleratedrendering.configuration.filter.item_filter_type")
				.defineEnum				("item_filter_type",					FilterType.BLACKLIST);

		filterEntityFilterValues						= builder
				.comment				("You can configure the entity filter by this list.")
				.comment				("Entity filter will use this list and the filter type to determine if a entity can pass the filter.")
				.translation			("acceleratedrendering.configuration.filter.entity_filter_values")
				.gameRestart			()
				.defineListAllowEmpty	("entity_filter_values",				new ArrayList<>(), () -> "minecraft:empty", object -> object instanceof String string && ResourceLocation.tryParse(string) != null);

		filterBlockEntityFilterValues					= builder
				.comment				("You can configure the block entity filter by this list.")
				.comment				("Block entity filter will use this list and the filter type to determine if a block entity can pass the filter.")
				.translation			("acceleratedrendering.configuration.filter.block_entity_filter_values")
				.gameRestart			()
				.defineListAllowEmpty	("block_entity_filter_values",			new ArrayList<>(), () -> "minecraft:empty", object -> object instanceof String string && ResourceLocation.tryParse(string) != null);

		filterItemFilterValues							= builder
				.comment				("You can configure the item filter by this list.")
				.comment				("Item filter will use this list and the filter type to determine if an item can pass the filter.")
				.translation			("acceleratedrendering.configuration.filter.item_filter_values")
				.gameRestart			()
				.defineListAllowEmpty	("item_filter_values",					new ArrayList<>(), () -> "minecraft:empty", object -> object instanceof String string && ResourceLocation.tryParse(string) != null);

		builder.pop();

		builder
				.comment				("Iris Compatibility Settings")
				.comment				("Iris Compatibility Settings allows Accelerated Rendering to work correctly with Iris.")
				.translation			("acceleratedrendering.configuration.iris_compatibility")
				.push					("iris_compatibility");

		irisCompatFeatureStatus							= builder
				.comment				("- DISABLED: Accelerated Rendering will be incompatible with Iris and cause visual glitches when working with Iris.")
				.comment				("- ENABLED: Accelerated Rendering will use compute shaders that fits Iris's vertex formats, which make it compatible with Iris.")
				.translation			("acceleratedrendering.configuration.iris_compatibility.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.ENABLED);

		irisCompatOrientationCullingCompat				= builder
				.comment				("- DISABLED: Simple Orientation culling will not work with Iris because the culling shader is for vanilla's vertex formats.")
				.comment				("- ENABLED: Simple Orientation culling will use another culling shader that fits iris's vertex format, which make it compatible with Iris.")
				.translation			("acceleratedrendering.configuration.iris_compatibility.orientation_culling_compatibility")
				.defineEnum				("orientation_culling_compatibility",	FeatureStatus.ENABLED);

		irisCompatShadowCulling							= builder
				.comment				("- DISABLED: Entities will not be culled when they are rendered as shadows unless mods explicitly enable it temporarily when rendering their own shadows. Which reduce FPS due to redundant faces.")
				.comment				("- ENABLED: Entities will be culled when they are rendered as shadows unless mods explicitly disable it temporarily when rendering their own shadows. Redundant faces will be culled and improve FPS, but it may cause incorrect shadows.")
				.translation			("acceleratedrendering.configuration.iris_compatibility.shadow_culling")
				.defineEnum				("shadow_culling",						FeatureStatus.ENABLED);

		irisCompatPolygonProcessing						= builder
				.comment				("- DISABLED: Extra information in vertices provided by Iris will not be included or calculated in the accelerated pipeline unless mods explicitly enable it temporarily when rendering their own faces, which may cause visual glitches or incorrect rendering.")
				.comment				("- ENABLED: Extra information in vertices provided by Iris will be included and calculated in the accelerated pipeline by a compute shader unless mods explicitly disable it temporarily when rendering their own faces.")
				.translation			("acceleratedrendering.configuration.iris_compatibility.polygon_processing")
				.defineEnum				("polygon_processing",					FeatureStatus.ENABLED);

		builder.pop();

		builder
				.comment				("Curios Compatibility Settings")
				.comment				("Curios Compatibility Settings allows Accelerated Rendering to work correctly with Curios.")
				.translation			("acceleratedrendering.configuration.curios_compatibility")
				.push					("curios_compatibility");

		curiosCompatFeatureStatus						= builder
				.comment				("- DISABLED: Accelerated Rendering will not interrupt the acceleration of the Curios layer on entities.")
				.comment				("- ENABLED: Accelerated Rendering will interrupt the acceleration of Curios layer on entities to prevent some mods using extremely bad rendering code that breaks the caching of Accelerated Rendering.")
				.translation			("acceleratedrendering.configuration.curios_compatibility.feature_status")
				.defineEnum				("feature_status",						FeatureStatus.ENABLED);

		curiosCompatLayerAcceleration					= builder
				.comment				("- DISABLED: Curios layer will not be accelerated by default to prevent some mods using extremely bad rendering code that breaks the caching of Accelerated Rendering unless mods explicitly enable the acceleration when rendering their accessories or equipments.")
				.comment				("- ENABLED: Curios layer will be accelerated by default unless mods explicitly enable the acceleration when rendering their accessories or equipments.")
				.translation			("acceleratedrendering.configuration.curios_compatibility.layer_acceleration")
				.defineEnum				("layer_acceleration",					FeatureStatus.DISABLED);

		curiosItemFilter								= builder
				.comment				("- DISABLED: Curios item filter will be disabled and acceleration of the rendering of all curios accessories/equipments will be determined by the \"layer acceleration\" option.")
				.comment				("- ENABLED: Curios item filter will test if the acceleration of the curios accessories/equipments should be prevented based on the filter values and the filter type.")
				.translation			("acceleratedrendering.configuration.curios_compatibility.item_filter")
				.defineEnum				("item_filter",							FeatureStatus.DISABLED);

		curiosItemFilterType							= builder
				.comment				("- BLACKLIST: Curios items that are not in the filter values can pass the filter and not being prevented to be accelerated.")
				.comment				("- WHITELIST: Curios items that are in the filter values can pass the filter and not being prevented to be accelerated.")
				.translation			("acceleratedrendering.configuration.curios_compatibility.item_filter_type")
				.defineEnum				("item_filter_type",					FilterType.BLACKLIST);

		curiosItemFilterValues							= builder
				.comment				("You can configure the curios item filter by this list.")
				.comment				("Curios item filter will use this list and the filter type to determine if a curios item can pass the filter.")
				.translation			("acceleratedrendering.configuration.curios_compatibility.item_filter_values")
				.gameRestart			()
				.defineListAllowEmpty	("item_filter_values",					new ArrayList<>(), () -> "minecraft:air", object -> object instanceof String string && ResourceLocation.tryParse(string) != null);

		builder.pop();
	}
}
