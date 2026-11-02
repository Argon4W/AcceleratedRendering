package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.github.argon4w.acceleratedrendering.core.utils.RenderTypeUtils;
import net.irisshaders.batchedentityrendering.impl.BlendingStateHolder;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.irisshaders.batchedentityrendering.impl.WrappableRenderType;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderTypeUtils.class)
public class RenderTypeUtilsMixin {

	@ModifyVariable(
			method		= "getTextureLocation",
			at			= @At("HEAD"),
			ordinal		= 0,
			argsOnly	= true
	)
	private static RenderType unwrapIrisRenderType1(RenderType renderType) {
		return renderType instanceof WrappableRenderType wrapped ? wrapped.unwrap() : renderType;
	}

	@ModifyVariable(
			method		= "isCulled",
			at			= @At("HEAD"),
			ordinal		= 0,
			argsOnly	= true
	)
	private static RenderType unwrapIrisRenderType2(RenderType renderType) {
		return renderType instanceof WrappableRenderType wrapped ? wrapped.unwrap() : renderType;
	}

	@ModifyVariable(
			method		= "isDynamic",
			at			= @At("HEAD"),
			ordinal		= 0,
			argsOnly	= true
	)
	private static RenderType unwrapIrisRenderType3(RenderType renderType) {
		return renderType instanceof WrappableRenderType wrapped ? wrapped.unwrap() : renderType;
	}

	@ModifyVariable(
			method		= "hasDepth",
			at			= @At("HEAD"),
			ordinal		= 0,
			argsOnly	= true
	)
	private static RenderType unwrapIrisRenderType4(RenderType renderType) {
		return renderType instanceof WrappableRenderType wrapped ? wrapped.unwrap() : renderType;
	}

	@ModifyVariable(
			method		= "withDepth",
			at			= @At("HEAD"),
			ordinal		= 0,
			argsOnly	= true
	)
	private static RenderType unwrapIrisRenderType5(RenderType renderType) {
		return renderType instanceof WrappableRenderType wrapped ? wrapped.unwrap() : renderType;
	}

	@Inject(
			method		= "getDrawType",
			at			= @At("HEAD"),
			cancellable	= true
	)
	private static void getIrisRenderTypeDrawType(RenderType renderType, CallbackInfoReturnable<LayerDrawType> cir) {
		var holder = (BlendingStateHolder) renderType;

		cir.setReturnValue(	holder.getTransparencyType() == TransparencyType.GENERAL_TRANSPARENT
				||			holder.getTransparencyType() == TransparencyType.DECAL
				? LayerDrawType.TRANSLUCENT
				: LayerDrawType.OPAQUE
		);
	}

	@Inject(
			method		= "isTranslucent",
			at			= @At("HEAD"),
			cancellable	= true
	)
	private static void checkIrisTransparency(RenderType renderType, CallbackInfoReturnable<Boolean> cir) {
		var holder = (BlendingStateHolder) renderType;

		cir.setReturnValue(	holder.getTransparencyType() == TransparencyType.GENERAL_TRANSPARENT
				||			holder.getTransparencyType() == TransparencyType.DECAL
		);
	}
}
