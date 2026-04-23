package com.github.argon4w.acceleratedrendering.compat.sodium.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAccelerationHolder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.jellysquid.mods.sodium.client.render.vertex.buffer.ExtendedBufferBuilder;
import me.jellysquid.mods.sodium.client.render.vertex.buffer.SodiumBufferBuilder;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;

import java.util.function.Supplier;

@Pseudo
@Mixin(SodiumBufferBuilder.class)
public class SodiumBufferBuilderMixin implements IAcceleratedVertexConsumer, IAccelerationHolder {

	@Shadow(remap = false) @Final private ExtendedBufferBuilder builder;

	@Unique
	@Override
	public VertexConsumer initAcceleration(RenderType renderType, Supplier<IAcceleratedBufferSource> bufferSource) {
		return ((IAccelerationHolder) builder).initAcceleration(renderType, bufferSource);
	}

	@Unique
	@Override
	public AcceleratedBufferBuilder getAccelerated() {
		return ((IAccelerationHolder) builder).getAccelerated();
	}

	@Unique
	@Override
	public boolean isAccelerated() {
		return ((IAcceleratedVertexConsumer) builder).isAccelerated();
	}

	@Unique
	@Override
	public <T> void doRender(
			IAcceleratedRenderer<T> renderer,
			T						context,
			Matrix4f				transform,
			Matrix3f				normal,
			int						light,
			int						overlay,
			int						color
	) {
		((IAcceleratedVertexConsumer) builder).doRender(
				renderer,
				context,
				transform,
				normal,
				light,
				overlay,
				color
		);
	}
}
