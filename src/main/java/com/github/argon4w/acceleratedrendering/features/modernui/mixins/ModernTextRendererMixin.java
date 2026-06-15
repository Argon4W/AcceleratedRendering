package com.github.argon4w.acceleratedrendering.features.modernui.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.modernui.contexts.MUIOutlineDrawContext;
import com.github.argon4w.acceleratedrendering.features.modernui.contexts.MUIStringDrawContext;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import icyllis.modernui.mc.text.ModernTextRenderer;
import icyllis.modernui.mc.text.TextLayout;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(ModernTextRenderer.class)
public class ModernTextRendererMixin {

	@WrapOperation(
			method	= "drawText(Licyllis/modernui/mc/text/TextLayout;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
			at		= @At(
					value	= "INVOKE",
					target	= "Licyllis/modernui/mc/text/TextLayout;drawText(Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;FFIIIIZIZFII)F"
			),
			remap	= false
	)
	public float renderTextFast(
			TextLayout			instance,
			Matrix4f			matrix,
			MultiBufferSource	source,
			float				textX,
			float				textTop,
			int					red,
			int					green,
			int					blue,
			int					alpha,
			boolean				isShadow,
			int					preferredMode,
			boolean				polygonOffset,
			float				uniformScale,
			int					backgroundColor,
			int					packedLight,
			Operation<Float>	original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
				||	!	ModsFeature.isEnabled				()
				||	!	ModsFeature.shouldAccelerateModernUI()
		) {
			return original.call(
					instance,
					matrix,
					source,
					textX,
					textTop,
					red,
					green,
					blue,
					alpha,
					isShadow,
					preferredMode,
					polygonOffset,
					uniformScale,
					backgroundColor,
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new MUIStringDrawContext(
				new Matrix4f(matrix),
				instance,
				textX,
				textTop,
				uniformScale,
				FastColor.ARGB32.color(
						alpha,
						red,
						green,
						blue
				),
				backgroundColor,
				preferredMode,
				packedLight,
				isShadow,
				polygonOffset
		));

		return ((TextLayoutAccessor) instance).getTotalAdvance();
	}

	@WrapMethod(
			method	= "drawText8xOutline",
			remap	= false
	)
	public void renderOutlineFast(
			FormattedCharSequence	text,
			float					textX,
			float					textY,
			int						textColor,
			int						backgroundColor,
			Matrix4f				transform,
			MultiBufferSource		bufferSource,
			int						packedLight,
			Operation<Void>			original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
				||	!	ModsFeature.isEnabled				()
				||	!	ModsFeature.shouldAccelerateModernUI()
		) {
			original.call(
					text,
					textX,
					textY,
					textColor,
					backgroundColor,
					transform,
					bufferSource,
					packedLight
			);

			return;
		}

		GuiBatchingController.INSTANCE.submitString(new MUIOutlineDrawContext(
				new Matrix4f(transform),
				(ModernTextRenderer) (Object) this,
				text,
				textX,
				textY,
				textColor,
				backgroundColor,
				packedLight
		));
	}
}
