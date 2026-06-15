package com.github.argon4w.acceleratedrendering.features.culling;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import com.github.argon4w.acceleratedrendering.core.backends.programs.BarrierFlags;
import com.github.argon4w.acceleratedrendering.core.programs.LoadComputeShaderEvent;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.github.argon4w.acceleratedrendering.core.utils.ResourceLocationUtils;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber	(
		modid	= AcceleratedRenderingModEntry	.MOD_ID,
		value	= Dist							.CLIENT,
		bus		= Bus							.MOD
)
public class OrientationCullingPrograms {

	public static final ResourceLocation CORE_ENTITY_QUAD_CULLING_KEY				= ResourceLocationUtils.create("core_entity_quad_culling");
	public static final ResourceLocation CORE_ENTITY_TRIANGLE_CULLING_KEY			= ResourceLocationUtils.create("core_entity_triangle_culling");
	public static final ResourceLocation CORE_BLOCK_QUAD_CULLING_KEY				= ResourceLocationUtils.create("core_block_quad_culling");
	public static final ResourceLocation CORE_BLOCK_TRIANGLE_CULLING_KEY			= ResourceLocationUtils.create("core_block_triangle_culling");
	public static final ResourceLocation CORE_POS_TEX_COLOR_QUAD_CULLING_KEY		= ResourceLocationUtils.create("core_pos_tex_color_quad_culling");
	public static final ResourceLocation CORE_POS_TEX_COLOR_TRIANGLE_CULLING_KEY	= ResourceLocationUtils.create("core_pos_tex_color_triangle_culling");
	public static final ResourceLocation CORE_POS_TEX_QUAD_CULLING_KEY				= ResourceLocationUtils.create("core_pos_tex_quad_culling");
	public static final ResourceLocation CORE_POS_TEX_TRIANGLE_CULLING_KEY			= ResourceLocationUtils.create("core_pos_tex_triangle_culling");

	@SubscribeEvent
	public static void onLoadComputeShaders(LoadComputeShaderEvent event) {
		event.loadComputeShader(
				CORE_ENTITY_QUAD_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/entity_quad_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_ENTITY_TRIANGLE_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/entity_triangle_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_BLOCK_QUAD_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/block_quad_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_BLOCK_TRIANGLE_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/block_triangle_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_POS_TEX_COLOR_QUAD_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/pos_tex_color_quad_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_POS_TEX_COLOR_TRIANGLE_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/pos_tex_color_triangle_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_POS_TEX_QUAD_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/pos_tex_quad_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);

		event.loadComputeShader(
				CORE_POS_TEX_TRIANGLE_CULLING_KEY,
				ResourceLocationUtils	.create("shaders/core/culling/pos_tex_triangle_culling_shader.compute"),
				BarrierFlags			.SHADER_STORAGE,
				BarrierFlags			.ATOMIC_COUNTER
		);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLoadCullingPrograms(LoadCullingProgramSelectorEvent event) {
		event.loadFor(DefaultVertexFormat.NEW_ENTITY, parent -> new OrientationCullingProgramSelector(
				parent,
				CORE_ENTITY_QUAD_CULLING_KEY,
				CORE_ENTITY_TRIANGLE_CULLING_KEY
		));

		event.loadFor(DefaultVertexFormat.BLOCK, parent -> new OrientationCullingProgramSelector(
				parent,
				CORE_BLOCK_QUAD_CULLING_KEY,
				CORE_BLOCK_TRIANGLE_CULLING_KEY
		));

		event.loadFor(DefaultVertexFormat.POSITION_TEX_COLOR, parent -> new OrientationCullingProgramSelector(
				parent,
				CORE_POS_TEX_COLOR_QUAD_CULLING_KEY,
				CORE_POS_TEX_COLOR_TRIANGLE_CULLING_KEY
		));

		event.loadFor(DefaultVertexFormat.POSITION_TEX, parent -> new OrientationCullingProgramSelector(
				parent,
				CORE_POS_TEX_QUAD_CULLING_KEY,
				CORE_POS_TEX_TRIANGLE_CULLING_KEY
		));
	}
}
