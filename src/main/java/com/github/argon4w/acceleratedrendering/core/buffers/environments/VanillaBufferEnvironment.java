package com.github.argon4w.acceleratedrendering.core.buffers.environments;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.IDrawMethod;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes.MeshUploadingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.TransformProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.*;
import com.github.argon4w.acceleratedrendering.core.programs.processing.IPolygonProcessor;
import com.github.argon4w.acceleratedrendering.core.programs.processing.LoadPolygonProcessorEvent;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

import java.util.Set;

public class VanillaBufferEnvironment implements IBufferEnvironment {

	private final VertexFormat						vertexFormat;
	private final VertexLayout						layout;
	private final IDrawMethod						method;

	private final IShaderProgramOverrides			shaderProgramOverrides;
	private final MeshUploadingProgramDispatcher	meshUploadingProgramDispatcher;
	private final TransformProgramDispatcher		transformProgramDispatcher;
	private final ICullingProgramSelector			cullingProgramSelector;
	private final IPolygonProcessor					polygonProcessor;

	public VanillaBufferEnvironment(
			VertexFormat		vertexFormat,
			ResourceLocation	uploadingProgramKey,
			ResourceLocation	transformProgramKey
	) {
		var defaultTransformOverride		= new TransformProgramDispatcher	.Default(transformProgramKey, 4L * 4L);
		var defaultUploadingOverride		= new MeshUploadingProgramDispatcher.Default(uploadingProgramKey, 7L * 4L);

		this.vertexFormat					= vertexFormat;
		this.layout							= new VertexLayout(vertexFormat);
		this.method							= CoreFeature.getDrawMethod();

		this.shaderProgramOverrides			= ModLoader.get()	.postEventWithReturn(new LoadShaderProgramOverridesEvent(this.vertexFormat)).getOverrides(defaultTransformOverride, defaultUploadingOverride);
		this.polygonProcessor				= ModLoader.get()	.postEventWithReturn(new LoadPolygonProcessorEvent		(this.vertexFormat)).getProcessor();
		this.cullingProgramSelector			= this.method		.getCullingProgramSelector								(this.vertexFormat);

		this.meshUploadingProgramDispatcher	= new MeshUploadingProgramDispatcher();
		this.transformProgramDispatcher		= new TransformProgramDispatcher	();
	}

	@Override
	public void clear() {
		meshUploadingProgramDispatcher.clear();
	}

	@Override
	public void setupBufferState() {
		vertexFormat.setupBufferState();
	}

	@Override
	public Set<VertexFormat> getVertexFormats() {
		return Set.of(vertexFormat);
	}

	@Override
	public VertexLayout getLayout() {
		return layout;
	}

	@Override
	public ProgramOverride getProgramOverride(RenderType renderType) {
		return shaderProgramOverrides.getOverride(renderType);
	}

	@Override
	public ProgramOverride getProgramOverride(int overrideId) {
		return shaderProgramOverrides.getOverride(overrideId);
	}

	@Override
	public MeshUploadingProgramDispatcher selectMeshUploadingProgramDispatcher() {
		return meshUploadingProgramDispatcher;
	}

	@Override
	public TransformProgramDispatcher selectTransformProgramDispatcher() {
		return transformProgramDispatcher;
	}

	@Override
	public ICullingProgramDispatcher selectCullingProgramDispatcher(RenderType renderType) {
		return cullingProgramSelector.select(renderType);
	}

	@Override
	public IPolygonProgramDispatcher selectProcessingProgramDispatcher(VertexFormat.Mode mode) {
		return polygonProcessor.select(mode);
	}

	@Override
	public boolean isAccelerated(VertexFormat vertexFormat) {
		return this.vertexFormat == vertexFormat;
	}

	@Override
	public IDrawMethod getDrawMethod() {
		return method;
	}

	@Override
	public int getVertexSize() {
		return vertexFormat.getVertexSize();
	}

	@Override
	public int getOverrideCount() {
		return shaderProgramOverrides.getCount();
	}
}
