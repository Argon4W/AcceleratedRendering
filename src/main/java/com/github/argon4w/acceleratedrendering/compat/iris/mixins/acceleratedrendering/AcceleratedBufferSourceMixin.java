package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSource;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AcceleratedBufferSource.class)
public class AcceleratedBufferSourceMixin {

	@ModifyArg(
			method	= "getBuffer",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/builders/AcceleratedBufferBuilder;<init>(Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/pools/StagingBufferPool$StagingBuffer;Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/pools/StagingBufferPool$StagingBuffer;Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/draw/pools/IElementPool$IElementSegment;Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/AcceleratedRingBuffers$Buffers;Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/layers/functions/ILayerFunction;Lnet/minecraft/client/renderer/RenderType;)V"
			),
			index	= 5,
			remap	= false
	)
	public RenderType unwrapIrisRenderType(RenderType renderType) {
		return renderType instanceof WrappableRenderType wrapped ? wrapped.unwrap() : renderType;
	}

	@Inject(
			method	= "drawBuffers",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/AcceleratedRingBuffers$Buffers;bindDrawBuffers()V",
					shift	= At.Shift.BEFORE
			),
			remap	= false
	)
	private void beforeBindDrawBuffers(CallbackInfo ci) {
		if (!ImmediateState.isRenderingLevel) {
			ImmediateState.renderWithExtendedVertexFormat = false;
		}
	}

	@Inject(
			method	= "drawBuffers",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/accelerated/AcceleratedRingBuffers$Buffers;bindDrawBuffers()V",
					shift	= At.Shift.AFTER
			),
			remap	= false
	)
	private void afterBindDrawBuffers(CallbackInfo ci) {
		if (!ImmediateState.isRenderingLevel) {
			ImmediateState.renderWithExtendedVertexFormat = true;
		}
	}
}
