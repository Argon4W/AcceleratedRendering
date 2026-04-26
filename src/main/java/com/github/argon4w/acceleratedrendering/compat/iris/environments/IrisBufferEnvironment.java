package com.github.argon4w.acceleratedrendering.compat.iris.environments;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.IDrawMethod;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramSelector;
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
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

import java.util.Set;

public class IrisBufferEnvironment implements IBufferEnvironment {

	private final IBufferEnvironment vanillaSubSet;
	private final IBufferEnvironment irisSubSet;

	public IrisBufferEnvironment(
			IBufferEnvironment	vanillaSubSet,
			VertexFormat		vanillaVertexFormat,
			VertexFormat		irisVertexFormat,
			ResourceLocation	meshUploadingProgramKey,
			ResourceLocation	transformProgramKey
	) {
		this.vanillaSubSet	= vanillaSubSet;
		this.irisSubSet		= new IrisSubSet(
				vanillaVertexFormat,
				irisVertexFormat,
				meshUploadingProgramKey,
				transformProgramKey
		);
	}

	private IBufferEnvironment getSubSet() {
		return IrisApi.getInstance().isShaderPackInUse() && ImmediateState.isRenderingLevel ? irisSubSet : vanillaSubSet;
	}

	@Override
	public void setupBufferState() {
		getSubSet().setupBufferState();
	}

	@Override
	public Set<VertexFormat> getVertexFormats() {
		return irisSubSet.getVertexFormats();
	}

	@Override
	public VertexLayout getLayout() {
		return getSubSet().getLayout();
	}

	@Override
	public IServerBuffer getImmediateMeshBuffer() {
		return getSubSet().getImmediateMeshBuffer();
	}

	@Override
	public ITransformShaderProgramOverride getTransformProgramOverride(RenderType renderType) {
		return getSubSet().getTransformProgramOverride(renderType);
	}

	@Override
	public IUploadingShaderProgramOverride getUploadingProgramOverride(RenderType renderType) {
		return getSubSet().getUploadingProgramOverride(renderType);
	}

	@Override
	public MeshUploadingProgramDispatcher selectMeshUploadingProgramDispatcher() {
		return getSubSet().selectMeshUploadingProgramDispatcher();
	}

	@Override
	public TransformProgramDispatcher selectTransformProgramDispatcher() {
		return getSubSet().selectTransformProgramDispatcher();
	}

	@Override
	public ICullingProgramDispatcher selectCullingProgramDispatcher(RenderType renderType) {
		return getSubSet().selectCullingProgramDispatcher(renderType);
	}

	@Override
	public IPolygonProgramDispatcher selectProcessingProgramDispatcher(VertexFormat.Mode mode) {
		return getSubSet().selectProcessingProgramDispatcher(mode);
	}

	@Override
	public boolean isAccelerated(VertexFormat vertexFormat) {
		return getSubSet().isAccelerated(vertexFormat);
	}

	@Override
	public IDrawMethod getDrawMethod() {
		return getSubSet().getDrawMethod();
	}

	@Override
	public int getVertexSize() {
		return getSubSet().getVertexSize();
	}

	public static class IrisSubSet implements IBufferEnvironment {

		private final VertexFormat						vanillaVertexFormat;
		private final VertexFormat						irisVertexFormat;
		private final VertexLayout						layout;
		private final IDrawMethod						method;

		private final IShaderProgramOverrides			shaderProgramOverrides;
		private final MeshUploadingProgramDispatcher	meshUploadingProgramDispatcher;
		private final TransformProgramDispatcher		transformProgramDispatcher;
		private final ICullingProgramSelector			cullingProgramSelector;
		private final IPolygonProcessor					polygonProcessor;

		public IrisSubSet(
				VertexFormat		vanillaVertexFormat,
				VertexFormat		irisVertexFormat,
				ResourceLocation	uploadingProgramKey,
				ResourceLocation	transformProgramKey
		) {
			var defaultTransformOverride		= new TransformProgramDispatcher	.Default(transformProgramKey, 4L * 4L);
			var defaultUploadingOverride		= new MeshUploadingProgramDispatcher.Default(uploadingProgramKey, 9L * 4L);

			this.vanillaVertexFormat			= vanillaVertexFormat;
			this.irisVertexFormat				= irisVertexFormat;
			this.layout							= new VertexLayout(this.irisVertexFormat);
			this.method							= CoreFeature.getDrawMethod();

			this.shaderProgramOverrides			= ModLoader.get()	.postEventWithReturn(new LoadShaderProgramOverridesEvent(this.irisVertexFormat)).getOverrides(defaultTransformOverride, defaultUploadingOverride);
			this.polygonProcessor				= ModLoader.get()	.postEventWithReturn(new LoadPolygonProcessorEvent		(this.irisVertexFormat)).getProcessor();
			this.cullingProgramSelector			= this.method		.getCullingProgramSelector								(this.irisVertexFormat);

			this.meshUploadingProgramDispatcher	= new MeshUploadingProgramDispatcher();
			this.transformProgramDispatcher		= new TransformProgramDispatcher	();
		}

		@Override
		public void setupBufferState() {
			irisVertexFormat.setupBufferState();
		}

		@Override
		public boolean isAccelerated(VertexFormat vertexFormat) {
			return this.vanillaVertexFormat == vertexFormat || this.irisVertexFormat == vertexFormat;
		}

		@Override
		public Set<VertexFormat> getVertexFormats() {
			return Set.of(vanillaVertexFormat, irisVertexFormat);
		}

		@Override
		public VertexLayout getLayout() {
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
		public IDrawMethod getDrawMethod() {
			return method;
		}

		@Override
		public int getVertexSize() {
			return irisVertexFormat.getVertexSize();
		}
	}
}
