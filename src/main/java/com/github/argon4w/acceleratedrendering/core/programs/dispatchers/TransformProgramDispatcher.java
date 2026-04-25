package com.github.argon4w.acceleratedrendering.core.programs.dispatchers;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.Uniform;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool;
import com.github.argon4w.acceleratedrendering.core.programs.ComputeShaderProgramLoader;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ITransformShaderProgramOverride;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

import static org.lwjgl.opengl.GL46.*;

public class TransformProgramDispatcher {

	public	static	final	int								VERTEX_BUFFER_IN_INDEX		= 0;
	public	static	final	int								VARYING_BUFFER_IN_INDEX		= 3;
	private static	final	int								GROUP_SIZE					= 128;
	private static	final	int								DISPATCH_COUNT_Y_Z			= 1;

	private					ITransformShaderProgramOverride	lastOverride;
	private					int								lastBarriers;

	public TransformProgramDispatcher() {
		this.lastOverride = null;
		this.lastBarriers = GL_SHADER_STORAGE_BARRIER_BIT;
	}

	public void dispatch(Collection<AcceleratedBufferBuilder> builders) {
		glMemoryBarrier(lastBarriers);

		for (var builder : builders) {
			var currentOverride	= builder			.getTransformOverride	();
			var vertexCount		= builder			.getVertexCount			();
			var vertexBuffer	= builder			.getVertexBuffer		();
			var varyingBuffer	= builder			.getVaryingBuffer		();

			if (lastOverride != currentOverride) {
				lastOverride = currentOverride;
				lastOverride.useProgram		();
				lastOverride.setupProgram	();
			}

			if (vertexCount != 0) {
				vertexBuffer					.bindBase			(GL_SHADER_STORAGE_BUFFER, VERTEX_BUFFER_IN_INDEX);
				varyingBuffer					.bindBase			(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_IN_INDEX);
				lastBarriers |= currentOverride	.dispatchTransform	(
						vertexCount,
						(int) (vertexBuffer	.getOffset() / builder.getVertexSize	()),
						(int) (varyingBuffer.getOffset() / builder.getVaryingSize	())
				);
			}
		}

		resetOverride	();
		glUseProgram	(0);
	}

	public int dispatch(
			AcceleratedBufferBuilder		builder,
			StagingBufferPool.StagingBuffer	vertexBuffer,
			StagingBufferPool.StagingBuffer	varyingBuffer,
			long							vertexCount,
			long							vertexOffset,
			long							varyingOffset
	) {
		var currentOverride = builder.getTransformOverride();

		if (lastOverride != currentOverride) {
			lastOverride = currentOverride;
			lastOverride.useProgram		();
			lastOverride.setupProgram	();
		}

		vertexBuffer	.bindBase(GL_SHADER_STORAGE_BUFFER, VERTEX_BUFFER_IN_INDEX);
		varyingBuffer	.bindBase(GL_SHADER_STORAGE_BUFFER, VARYING_BUFFER_IN_INDEX);

		return currentOverride.dispatchTransform(
				(int) vertexCount,
				(int) vertexOffset,
				(int) varyingOffset
		);
	}

	public void resetOverride() {
		lastOverride = null;
	}

	public static class Default implements ITransformShaderProgramOverride {

		private final long				varyingSize;
		private final ComputeProgram	program;
		private final Uniform			vertexCountUniform;
		private final Uniform			vertexOffsetUniform;
		private final Uniform			varyingOffsetUniform;

		public Default(ResourceLocation key, long varyingSize) {
			this.varyingSize			= varyingSize;
			this.program				= ComputeShaderProgramLoader.getProgram(key);
			this.vertexCountUniform		= program					.getUniform("vertexCount");
			this.vertexOffsetUniform	= program					.getUniform("vertexOffset");
			this.varyingOffsetUniform	= program					.getUniform("varyingOffset");
		}

		@Override
		public long getVaryingSize() {
			return varyingSize;
		}

		@Override
		public void useProgram() {
			program.useProgram();
		}

		@Override
		public void setupProgram() {
			program.setup();
		}

		@Override
		public void uploadVarying(long varyingAddress, int offset) {

		}

		@Override
		public int dispatchTransform(
				int vertexCount,
				int vertexOffset,
				int varyingOffset
		) {
			vertexCountUniform		.uploadUnsignedInt	(vertexCount);
			vertexOffsetUniform		.uploadUnsignedInt	(vertexOffset);
			varyingOffsetUniform	.uploadUnsignedInt	(varyingOffset);
			program					.dispatch			(
					(vertexCount + GROUP_SIZE - 1) / GROUP_SIZE,
					DISPATCH_COUNT_Y_Z,
					DISPATCH_COUNT_Y_Z
			);

			return program.getBarrierFlags();
		}
	}
}
