package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.core.CoreBuffersProvider;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.irisshaders.batchedentityrendering.impl.RenderBuffersExt;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CoreBuffersProvider.class)
public class CoreBuffersProviderMixin {

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
