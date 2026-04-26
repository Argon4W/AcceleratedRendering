package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreBuffersProvider;
import com.github.argon4w.acceleratedrendering.core.buffers.AcceleratedBufferSources;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.batchedentityrendering.impl.RenderBuffersExt;
import net.irisshaders.iris.pathways.HandRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CoreBuffersProvider.class)
public class CoreBuffersProviderMixin {

	@WrapOperation(
			method	= "lambda$static$1",
			at		= @At(
					value	= "FIELD",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/CoreBuffers;CORE:Lcom/github/argon4w/acceleratedrendering/core/buffers/AcceleratedBufferSources;",
					opcode	= Opcodes.GETSTATIC
			)
	)
	private static AcceleratedBufferSources redirectHandBuffers(Operation<AcceleratedBufferSources> original) {
		return HandRenderer.INSTANCE.isActive() ? IrisCompatBuffers.HAND : original.call();
	}

	@WrapMethod(
			method	= "bindAcceleratedBufferSources",
			remap	= false
	)
	private static void bindAcceleratedBufferSourcesForIris(RenderBuffers renderBuffers, Operation<Void> original) {
		var extension = (RenderBuffersExt) renderBuffers;

		extension	.beginLevelRendering();
		original	.call				(renderBuffers);
		extension	.endLevelRendering	();
		original	.call				(renderBuffers);
	}
}
