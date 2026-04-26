package com.github.argon4w.acceleratedrendering.core.programs.culling;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderPrograms;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;

public class PassThroughCullingProgramDispatcher implements ICullingProgramDispatcher {

	public	static	final		PassThroughCullingProgramDispatcher	QUAD				= new PassThroughCullingProgramDispatcher(VertexFormat.Mode.QUADS,		ComputeShaderPrograms.CORE_PASS_THROUGH_QUAD_CULLING_KEY);
	public	static	final		PassThroughCullingProgramDispatcher	TRIANGLE			= new PassThroughCullingProgramDispatcher(VertexFormat.Mode.TRIANGLES,	ComputeShaderPrograms.CORE_PASS_THROUGH_TRIANGLE_CULLING_KEY);
	private static	final		int									GROUP_SIZE			= 128;
	private static	final		int									DISPATCH_COUNT_Y_Z	= 1;

	private final VertexFormat.Mode	mode;
	private final ComputeProgram	program;
	private final Uniform			polygonCountUniform;
	private final Uniform			vertexOffsetUniform;

	public PassThroughCullingProgramDispatcher(VertexFormat.Mode mode, ResourceLocation key) {
		this.mode					= mode;
		this.program				= ComputeShaderProgramLoader.getProgram(key);
		this.polygonCountUniform	= program					.getUniform("polygonCount");
		this.vertexOffsetUniform	= program					.getUniform("vertexOffset");
	}

	@Override
	public int dispatch(AcceleratedBufferBuilder builder) {
		var vertexCount		= builder			.getTotalVertexCount();
		var polygonCount	= vertexCount / mode.primitiveLength;

		polygonCountUniform.uploadUnsignedInt(polygonCount);
		vertexOffsetUniform.uploadUnsignedInt((int) (builder.getVertexCountOffset()));

		program.useProgram	();
		program.dispatch	(
				(polygonCount + GROUP_SIZE - 1) / GROUP_SIZE,
				DISPATCH_COUNT_Y_Z,
				DISPATCH_COUNT_Y_Z
		);

		return program.getBarrierFlags();
	}

	@Override
	public boolean shouldCull() {
		return false;
	}
}
