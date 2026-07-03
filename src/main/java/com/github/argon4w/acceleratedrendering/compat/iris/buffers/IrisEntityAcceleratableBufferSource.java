package com.github.argon4w.acceleratedrendering.compat.iris.buffers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratableBufferSource;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.function.Supplier;

@ExtensionMethod(BufferSourceExtension.class)
public record IrisEntityAcceleratableBufferSource(MultiBufferSource wrapped, MultiBufferSource original) implements MultiBufferSource, IAcceleratableBufferSource {

	@Override
	public Supplier<IAcceleratedBufferSource> getBoundAcceleratedBufferSource() {
		return original.getAcceleratable().getBoundAcceleratedBufferSource();
	}

	@Override
	public boolean isBufferSourceAcceleratable() {
		return original.getAcceleratable().isBufferSourceAcceleratable();
	}

	@Override
	public void bindAcceleratedBufferSource(Supplier<IAcceleratedBufferSource> bufferSource) {
		original.getAcceleratable().bindAcceleratedBufferSource(bufferSource);
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		return wrapped.getBuffer(renderType);
	}
}
