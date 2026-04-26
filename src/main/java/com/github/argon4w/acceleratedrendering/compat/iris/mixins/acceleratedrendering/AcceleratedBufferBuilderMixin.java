package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisAcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools.IElementPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.functions.ILayerFunction;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.indirect.IndirectElementBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.StagingBufferPool;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AcceleratedBufferBuilder.class)
public class AcceleratedBufferBuilderMixin implements IIrisAcceleratedBufferBuilder {

	@Shadow @Final private	VertexLayout		layout;
	@Shadow private			long				vertexAddress;

	@Unique private			IMemoryInterface	entityIdOffset;
	@Unique private			IMemoryInterface	entityOffset;

	@Inject(
			method	= "<init>",
			at		= @At("TAIL")
	)
	public void constructor(
			StagingBufferPool			.StagingBuffer		vertexBuffer,
			StagingBufferPool			.StagingBuffer		varyingBuffer,
			IElementPool				.IElementSegment	elementSegment,
			AcceleratedRingBuffers		.Buffers			buffer,
			ILayerFunction									layerFunction,
			RenderType										renderType,
			CallbackInfo									ci
	) {
		entityIdOffset	= layout.getElement(IrisVertexFormats.ENTITY_ID_ELEMENT);
		entityOffset	= layout.getElement(IrisVertexFormats.ENTITY_ELEMENT);
	}

	@Inject(
			method	= "addVertex(FFFIFFIIFFF)V",
			at		= @At("TAIL")
	)
	public void addIrisVertex(
			float								pX,
			float								pY,
			float								pZ,
			int									pColor,
			float								pU,
			float								pV,
			int									pPackedOverlay,
			int									pPackedLight,
			float								pNormalX,
			float								pNormalY,
			float								pNormalZ,
			CallbackInfo						ci,
			@Local(name = "vertexAddress") long	vertexAddress
	) {
		addIrisData(vertexAddress);
	}

	@Inject(
			method	= "addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
			at		= @At("TAIL")
	)
	public void addIrisVertex(
			float									pX,
			float									pY,
			float 									pZ,
			CallbackInfoReturnable<VertexConsumer>	cir
	) {
		addIrisData(vertexAddress);
	}

	@Inject(
			method	= {
					"addServerMesh",
					"addClientMesh"
			},
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/memory/IMemoryInterface;putInt(JI)V",
					ordinal	= 2,
					shift	= At.Shift.AFTER
			)
	)
	public void addIrisMesh(CallbackInfo ci, @Local(name = "vertexAddress") long vertexAddress) {
		addIrisData(vertexAddress);
	}

	@Unique
	private void addIrisData(long vertexAddress) {
		entityOffset	.putShort(vertexAddress + 0L, (short) -1);
		entityOffset	.putShort(vertexAddress + 2L, (short) -1);
		entityIdOffset	.putShort(vertexAddress + 0L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedEntity		());
		entityIdOffset	.putShort(vertexAddress + 2L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedBlockEntity	());
		entityIdOffset	.putShort(vertexAddress + 4L, (short) CapturedRenderingState.INSTANCE.getCurrentRenderedItem		());
	}

	@Unique
	@Override
	public IMemoryInterface getEntityIdOffset() {
		return entityIdOffset;
	}

	@Unique
	@Override
	public IMemoryInterface getEntityOffset() {
		return entityOffset;
	}
}
