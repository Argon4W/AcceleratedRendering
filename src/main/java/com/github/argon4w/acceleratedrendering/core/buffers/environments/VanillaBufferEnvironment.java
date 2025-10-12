package com.github.argon4w.acceleratedrendering.core.buffers.environments;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexFormatMemoryLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
import com.github.argon4w.acceleratedrendering.core.programs.culling.LoadCullingProgramSelectorEvent;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.IPolygonProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.MeshUploadingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.TransformProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IShaderProgramOverrides;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ITransformShaderProgramOverride;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.IUploadingShaderProgramOverride;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.LoadShaderProgramOverridesEvent;
import com.github.argon4w.acceleratedrendering.core.programs.processing.IPolygonProcessor;
import com.github.argon4w.acceleratedrendering.core.programs.processing.LoadPolygonProcessorEvent;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

import java.util.Set;

public class VanillaBufferEnvironment implements IBufferEnvironment {

	private final VertexFormat							vertexFormat;
	private final IMemoryLayout<VertexFormatElement>	layout;

	private final IShaderProgramOverrides				shaderProgramOverrides;
	private final MeshUploadingProgramDispatcher		meshUploadingProgramDispatcher;
	private final TransformProgramDispatcher			transformProgramDispatcher;
	private final ICullingProgramSelector				cullingProgramSelector;
	private final IPolygonProcessor						polygonProcessor;

	public VanillaBufferEnvironment(
			VertexFormat		vertexFormat,
			ResourceLocation	uploadingProgramKey,
			ResourceLocation	transformProgramKey
	) {
		var defaultTransformOverride		= new TransformProgramDispatcher	.Default(transformProgramKey, 4L * 4L);
		var defaultUploadingOverride		= new MeshUploadingProgramDispatcher.Default(uploadingProgramKey, 5L * 4L);

		this.vertexFormat					= vertexFormat;
		this.layout							= new VertexFormatMemoryLayout				(vertexFormat);

		this.shaderProgramOverrides			= ModLoader.get().postEventWithReturn		(new LoadShaderProgramOverridesEvent(this.vertexFormat)).getOverrides	(defaultTransformOverride, defaultUploadingOverride);
		this.cullingProgramSelector			= ModLoader.get().postEventWithReturn		(new LoadCullingProgramSelectorEvent(this.vertexFormat)).getSelector	();
		this.polygonProcessor				= ModLoader.get().postEventWithReturn		(new LoadPolygonProcessorEvent		(this.vertexFormat)).getProcessor	();

		this.meshUploadingProgramDispatcher	= new MeshUploadingProgramDispatcher		();
		this.transformProgramDispatcher		= new TransformProgramDispatcher			();
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
	public IMemoryLayout<VertexFormatElement> getLayout() {
		return layout;
	}

	@Override
	public IServerBuffer getImmediateMeshBuffer() {
		return ServerMesh.Builder.BUFFERS.get(layout).get(0);
	}

	@Override
	public ITransformShaderProgramOverride getTransformProgramOverride(RenderType renderType) {
		return shaderProgramOverrides.getTransformOverrides().get(renderType);
	}

	@Override
	public IUploadingShaderProgramOverride getUploadingProgramOverride(RenderType renderType) {
		return shaderProgramOverrides.getUploadingOverrides().get(renderType);
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
	public int getVertexSize() {
		return vertexFormat.getVertexSize();
	}
}
