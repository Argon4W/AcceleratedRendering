package com.github.argon4w.acceleratedrendering.core.programs.culling;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import net.minecraft.client.renderer.RenderType;

public class EmptyCullingProgramSelector implements ICullingProgramSelector {

	public static final EmptyCullingProgramSelector INSTANCE = new EmptyCullingProgramSelector();

	@Override
	public ICullingProgramDispatcher select(RenderType renderType) {
		return EmptyCullingProgramDispatcher.INSTANCE;
	}

	public static class EmptyCullingProgramDispatcher implements ICullingProgramDispatcher {

		public static EmptyCullingProgramDispatcher INSTANCE = new EmptyCullingProgramDispatcher();

		@Override
		public int dispatch(AcceleratedBufferBuilder builder) {
			return 0;
		}

		@Override
		public boolean shouldCull() {
			return false;
		}
	}
}
