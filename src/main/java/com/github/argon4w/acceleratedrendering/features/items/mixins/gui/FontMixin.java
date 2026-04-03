package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Font.class)
public class FontMixin {

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I")
	public int renderGuiStringFast(
			String				text,
			float				textX,
			float				textY,
			int					textColor,
			boolean				dropShadow,
			Matrix4f			transform,
			MultiBufferSource	bufferSource,
			Font.DisplayMode	displayMode,
			int					backgroundColor,
			int					packedLight,
			boolean				bidirectional,
			Operation<Integer>	original
	) {
		if (!CoreFeature.isGuiBatching()) {
			return original.call(
					text,
					textX,
					textY,
					textColor,
					dropShadow,
					transform,
					bufferSource,
					displayMode,
					backgroundColor,
					packedLight,
					bidirectional
			);
		}

		GuiBatchingController.INSTANCE.recordString(
				transform,
				(Font) (Object) this,
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				displayMode,
				backgroundColor,
				packedLight,
				bidirectional
		);
		return 0;
	}
}
