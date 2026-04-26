package com.github.argon4w.acceleratedrendering.compat.iris.programs.culling;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatFeature;
import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import com.github.argon4w.acceleratedrendering.core.programs.culling.ICullingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.features.culling.OrientationCullingFeature;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.resources.ResourceLocation;

public class IrisCullingProgramDispatcher implements ICullingProgramDispatcher {

	private static	final int				GROUP_SIZE 			= 128;
	private static	final int				DISPATCH_COUNT_Y_Z	= 1;

	private			final VertexFormat.Mode	mode;
	private			final ComputeProgram	program;
	private			final Uniform			viewMatrixUniform;
	private			final Uniform			projectMatrixUniform;
	private			final Uniform			polygonCountUniform;
	private			final Uniform			vertexOffsetUniform;
	private			final Uniform			varyingOffsetUniform;

	public IrisCullingProgramDispatcher(VertexFormat.Mode mode, ResourceLocation key) {
		this.mode					= mode;
		this.program				= ComputeShaderProgramLoader.getProgram(key);
		this.viewMatrixUniform		= this.program				.getUniform("viewMatrix");
		this.projectMatrixUniform	= this.program				.getUniform("projectMatrix");
		this.polygonCountUniform	= this.program				.getUniform("polygonCount");
		this.vertexOffsetUniform	= this.program				.getUniform("vertexOffset");
		this.varyingOffsetUniform	= this.program				.getUniform("varyingOffset");
	}

	@Override
	public int dispatch(AcceleratedBufferBuilder builder) {
		var shadowState		= ShadowRenderingState	.areShadowsCurrentlyBeingRendered	();
		var vertexCount		= builder				.getTotalVertexCount				();
		var polygonCount	= vertexCount / mode	.primitiveLength;

		viewMatrixUniform	.uploadMatrix4f		(shadowState ? ShadowRenderer.MODELVIEW		: RenderSystem.getModelViewMatrix	());
		projectMatrixUniform.uploadMatrix4f		(shadowState ? ShadowRenderer.PROJECTION	: RenderSystem.getProjectionMatrix	());

		polygonCountUniform	.uploadUnsignedInt	(polygonCount);
		vertexOffsetUniform	.uploadUnsignedInt	((int) (builder.getVertexCountOffset	()));
		varyingOffsetUniform.uploadUnsignedInt	((int) (builder.getVaryingCountOffset	()));

		program.useProgram();

		program.dispatch(
				(polygonCount + GROUP_SIZE - 1) / GROUP_SIZE,
				DISPATCH_COUNT_Y_Z,
				DISPATCH_COUNT_Y_Z
		);

		return program.getBarrierFlags();
	}

	@Override
	public boolean shouldCull() {
		return 			OrientationCullingFeature.shouldCull()
				&& (	IrisCompatFeature.isShadowCullingEnabled() || !ShadowRenderingState.areShadowsCurrentlyBeingRendered());
	}
}
