package com.github.argon4w.acceleratedrendering.core.buffers.environments;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderPrograms;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.MeshUploadingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.TransformProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ITransformShaderProgramOverride;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IUploadingShaderProgramOverride;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

import java.util.Set;

public interface IBufferEnvironment {

	int									getVertexSize						();
	Set<VertexFormat>					getVertexFormats					();
	VertexLayout						getLayout							();
	IServerBuffer						getImmediateMeshBuffer				();
	MeshUploadingProgramDispatcher		selectMeshUploadingProgramDispatcher();
	TransformProgramDispatcher			selectTransformProgramDispatcher	();
	ITransformShaderProgramOverride		getTransformProgramOverride			(RenderType			renderType);
	IUploadingShaderProgramOverride		getUploadingProgramOverride			(RenderType			renderType);
	ICullingProgramDispatcher			selectCullingProgramDispatcher		(RenderType			renderType);
	IPolygonProgramDispatcher			selectProcessingProgramDispatcher	(VertexFormat.Mode	mode);
	boolean								isAccelerated						(VertexFormat		vertexFormat);
	void								setupBufferState					();

	class Presets {

		public static final IBufferEnvironment BLOCK				= new VanillaBufferEnvironment(DefaultVertexFormat.BLOCK,						ComputeShaderPrograms.CORE_BLOCK_MESH_UPLOADING_KEY,				ComputeShaderPrograms.CORE_BLOCK_VERTEX_TRANSFORM_KEY);
		public static final IBufferEnvironment ENTITY				= new VanillaBufferEnvironment(DefaultVertexFormat.NEW_ENTITY,					ComputeShaderPrograms.CORE_ENTITY_MESH_UPLOADING_KEY,				ComputeShaderPrograms.CORE_ENTITY_VERTEX_TRANSFORM_KEY);
		public static final IBufferEnvironment POS					= new VanillaBufferEnvironment(DefaultVertexFormat.POSITION,					ComputeShaderPrograms.CORE_POS_MESH_UPLOADING_KEY,					ComputeShaderPrograms.CORE_POS_VERTEX_TRANSFORM_KEY);
		public static final IBufferEnvironment POS_COLOR			= new VanillaBufferEnvironment(DefaultVertexFormat.POSITION_COLOR,				ComputeShaderPrograms.CORE_POS_COLOR_MESH_UPLOADING_KEY,			ComputeShaderPrograms.CORE_POS_COLOR_VERTEX_TRANSFORM_KEY);
		public static final IBufferEnvironment POS_TEX				= new VanillaBufferEnvironment(DefaultVertexFormat.POSITION_TEX,				ComputeShaderPrograms.CORE_POS_TEX_MESH_UPLOADING_KEY,				ComputeShaderPrograms.CORE_POS_TEX_VERTEX_TRANSFORM_KEY);
		public static final IBufferEnvironment POS_TEX_COLOR		= new VanillaBufferEnvironment(DefaultVertexFormat.POSITION_TEX_COLOR,			ComputeShaderPrograms.CORE_POS_TEX_COLOR_MESH_UPLOADING_KEY,		ComputeShaderPrograms.CORE_POS_TEX_COLOR_VERTEX_TRANSFORM_KEY);
		public static final IBufferEnvironment POS_COLOR_TEX_LIGHT	= new VanillaBufferEnvironment(DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,	ComputeShaderPrograms.CORE_POS_COLOR_TEX_LIGHT_MESH_UPLOADING_KEY,	ComputeShaderPrograms.CORE_POS_COLOR_TEX_LIGHT_VERTEX_TRANSFORM_KEY);
	}
}
