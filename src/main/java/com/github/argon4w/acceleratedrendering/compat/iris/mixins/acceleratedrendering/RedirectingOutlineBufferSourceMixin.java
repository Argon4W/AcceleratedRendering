package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.IRenderTypeExtension;
import com.github.argon4w.acceleratedrendering.compat.iris.IrisCompatFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.redirecting.RedirectingOutlineBufferSource;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RedirectingOutlineBufferSource.class)
public class RedirectingOutlineBufferSourceMixin {

    @WrapOperation(method = "getBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;toString()Ljava/lang/String;"))
    public String unwrapIrisRenderType(RenderType instance, Operation<String> original) {
        if (!IrisCompatFeature.isEnabled()) {
            return original.call(instance);
        }

        if (IrisCompatFeature.isFastIrisRenderTypeCheckEnabled()) {
            return original.call(((IRenderTypeExtension) instance).getOrUnwrap());
        }

        if (!(instance instanceof WrappableRenderType wrappable)) {
            return original.call(instance);
        }

        return original.call(wrappable.unwrap());
    }
}
