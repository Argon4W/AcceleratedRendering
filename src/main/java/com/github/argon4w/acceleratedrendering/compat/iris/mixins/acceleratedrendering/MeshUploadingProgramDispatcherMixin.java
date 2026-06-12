package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisAcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisMeshInfoCache;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.IMeshInfoCache;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes.MeshUploadingProgramDispatcher;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MeshUploadingProgramDispatcher.class)
public class MeshUploadingProgramDispatcherMixin {

	@Inject(
			method	= "dispatch",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/memory/IMemoryInterface;putIntAt(JII)V",
					ordinal	= 2,
					shift	= At.Shift.AFTER
			),
			remap	= false
	)
	public void addIrisData(
			Collection<AcceleratedBufferBuilder>						builders,
			AcceleratedRingBuffers.Buffers								buffers,
			CallbackInfo												ci,
			@Local(name = "meshInfos")		IMeshInfoCache				meshInfos,
			@Local(name = "builder")		AcceleratedBufferBuilder	builder,
			@Local(name = "offset")			int							offset,
			@Local(name = "i")				int							i,
			@Local(name = "vertexAddress")	long						vertexAddress
	) {
		((IIrisAcceleratedBufferBuilder) builder).getEntityIdOffset().putShortAt(vertexAddress + 0L, offset, ((IIrisMeshInfoCache) meshInfos).getRenderedEntity		(i));
		((IIrisAcceleratedBufferBuilder) builder).getEntityIdOffset().putShortAt(vertexAddress + 2L, offset, ((IIrisMeshInfoCache) meshInfos).getRenderedBlockEntity(i));
		((IIrisAcceleratedBufferBuilder) builder).getEntityIdOffset().putShortAt(vertexAddress + 4L, offset, ((IIrisMeshInfoCache) meshInfos).getRenderedItem		(i));
	}
}
