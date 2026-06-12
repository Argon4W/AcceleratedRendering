package com.github.argon4w.acceleratedrendering.core.mixins.buffers;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.EmptyAcceleratedBufferSources;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAccelerationHolder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements IAccelerationHolder, IAcceleratedVertexConsumer {

	@Unique private IAcceleratedBufferSource	bufferSources = EmptyAcceleratedBufferSources.INSTANCE;
	@Unique private RenderType					renderType;
	@Unique private AcceleratedBufferBuilder	acceleration;
	@Unique private boolean						init = false;


	@Inject(
			method = "begin",
			at = @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/BufferBuilder;switchFormat(Lcom/mojang/blaze3d/vertex/VertexFormat;)V",
					shift	= At.Shift.AFTER
			)
	)
	public void onBegin(
			VertexFormat.Mode	mode,
			VertexFormat		format,
			CallbackInfo		ci
	) {
		this.acceleration	= null;
		this.init			= false;
	}

	@Unique
	@Override
	public VertexConsumer initAcceleration(RenderType renderType, Supplier<IAcceleratedBufferSource> bufferSource) {
		if (CoreFeature.isLoaded() && !init) {
			this.bufferSources	= bufferSource.get();
			this.renderType		= renderType;
			this.acceleration	= null;
			this.init			= true;
		}

		return (VertexConsumer) this;
	}

	@Unique
	@Override
	public boolean isAccelerated() {
		return bufferSources != EmptyAcceleratedBufferSources.INSTANCE && getAccelerated() != null;
	}

	@Unique
	@Override
	public <T> void doRender(
			IAcceleratedRenderer<T>	renderer,
			T						context,
			Matrix4f				transform,
			Matrix3f				normal,
			int						light,
			int						overlay,
			int						color
	) {
		getAccelerated().doRender(
				renderer,
				context,
				transform,
				normal,
				light,
				overlay,
				color
		);
	}

	@Unique
	@Override
	public AcceleratedBufferBuilder getAccelerated() {
		if (		acceleration == null
				||	acceleration.isOutdated()
		) {
			acceleration = bufferSources.getBuffer(
					renderType,
					CoreFeature.getDefaultLayerBeforeFunction	(),
					CoreFeature.getDefaultLayerAfterFunction	(),
					CoreFeature.getDefaultLayer					()
			);
		}

		return acceleration;
	}
}
