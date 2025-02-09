package com.github.argon4w.acceleratedrendering.core.programs;

import com.github.argon4w.acceleratedrendering.AcceleratedRenderingModEntry;
import com.github.argon4w.acceleratedrendering.core.gl.programs.BarrierFlags;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.github.argon4w.acceleratedrendering.core.programs.processing.LoadPolygonProcessorEvent;
import com.github.argon4w.acceleratedrendering.core.programs.transform.FixedTransformProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.transform.LoadTransformProgramSelectorEvent;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

import java.util.function.UnaryOperator;

@EventBusSubscriber(modid = AcceleratedRenderingModEntry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ComputeShaderPrograms {

    public static final ResourceLocation CORE_ENTITY_VERTEX_TRANSFORM_KEY = AcceleratedRenderingModEntry.location("core_entity_vertex_transform");
    public static final ResourceLocation CORE_POS_TEX_COLOR_VERTEX_TRANSFORM_KEY = AcceleratedRenderingModEntry.location("core_pos_tex_color_vertex_transform");
    public static final ResourceLocation CORE_POS_TEX_VERTEX_TRANSFORM_KEY = AcceleratedRenderingModEntry.location("core_pos_tex_vertex_transform");
    public static final ResourceLocation CORE_PASS_THROUGH_POLYGON_CULLING_KEY = AcceleratedRenderingModEntry.location("core_pass_through_polygon_culling");
    public static final ResourceLocation COMPUTE_SHADER_PROGRAM_LOADER_KEY = AcceleratedRenderingModEntry.location("compute_shader_program_loader");

    @SubscribeEvent
    public static void onLoadComputeShaders(LoadComputeShaderEvent event) {
        event.loadComputeShader(
                CORE_ENTITY_VERTEX_TRANSFORM_KEY,
                AcceleratedRenderingModEntry.location("shaders/core/transform/entity_vertex_transform_shader.compute"),
                BarrierFlags.SHADER_STORAGE
        );

        event.loadComputeShader(
                CORE_POS_TEX_COLOR_VERTEX_TRANSFORM_KEY,
                AcceleratedRenderingModEntry.location("shaders/core/transform/pos_tex_color_vertex_transform_shader.compute"),
                BarrierFlags.SHADER_STORAGE
        );

        event.loadComputeShader(
                CORE_POS_TEX_VERTEX_TRANSFORM_KEY,
                AcceleratedRenderingModEntry.location("shaders/core/transform/pos_tex_vertex_transform_shader.compute"),
                BarrierFlags.SHADER_STORAGE
        );

        event.loadComputeShader(
                CORE_PASS_THROUGH_POLYGON_CULLING_KEY,
                AcceleratedRenderingModEntry.location("shaders/core/culling/pass_through_polygon_culling_shader.compute"),
                BarrierFlags.SHADER_STORAGE,
                BarrierFlags.ATOMIC_COUNTER
        );
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLoadTransformPrograms(LoadTransformProgramSelectorEvent event) {
        event.loadFor(DefaultVertexFormat.NEW_ENTITY, parent -> new FixedTransformProgramSelector(
                parent,
                CORE_ENTITY_VERTEX_TRANSFORM_KEY
        ));

        event.loadFor(DefaultVertexFormat.POSITION_TEX_COLOR, parent -> new FixedTransformProgramSelector(
                parent,
                CORE_POS_TEX_COLOR_VERTEX_TRANSFORM_KEY
        ));

        event.loadFor(DefaultVertexFormat.POSITION_TEX, parent -> new FixedTransformProgramSelector(
                parent,
                CORE_POS_TEX_VERTEX_TRANSFORM_KEY
        ));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLoadCullingPrograms(LoadCullingProgramSelectorEvent event) {
        event.loadFor(DefaultVertexFormat.NEW_ENTITY, UnaryOperator.identity());
        event.loadFor(DefaultVertexFormat.POSITION_TEX_COLOR, UnaryOperator.identity());
        event.loadFor(DefaultVertexFormat.POSITION_TEX, UnaryOperator.identity());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLoadPolygonProcessors(LoadPolygonProcessorEvent event) {
        event.loadFor(DefaultVertexFormat.NEW_ENTITY, UnaryOperator.identity());
        event.loadFor(DefaultVertexFormat.POSITION_TEX_COLOR, UnaryOperator.identity());
        event.loadFor(DefaultVertexFormat.POSITION_TEX, UnaryOperator.identity());
    }

    @SubscribeEvent
    public static void onRegisterResourceReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(COMPUTE_SHADER_PROGRAM_LOADER_KEY, ComputeShaderProgramLoader.INSTANCE);
    }
}
