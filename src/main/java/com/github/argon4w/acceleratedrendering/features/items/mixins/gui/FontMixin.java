package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.ComponentStringDrawContext;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.FormattedStringDrawContext;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.Outline8StringDrawContext;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.RawStringDrawContext;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Font.class)
public abstract class FontMixin {

	@Shadow public abstract boolean isBidirectional();

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
	public int renderGuiStringFast1(
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
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new RawStringDrawContext(
				new Matrix4f(transform),
				(Font) (Object) this,
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				displayMode,
				backgroundColor,
				packedLight,
				isBidirectional()
		));

		return 0;
	}

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I")
	public int renderGuiStringFast2(
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

		GuiBatchingController.INSTANCE.submitString(new RawStringDrawContext(
				new Matrix4f(transform),
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
		));

		return 0;
	}

	@WrapMethod(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
	public int renderGuiStringFast3(
			Component			text,
			float				textX,
			float				textY,
			int					textColor,
			boolean				dropShadow,
			Matrix4f			transform,
			MultiBufferSource	bufferSource,
			Font.DisplayMode	displayMode,
			int					backgroundColor,
			int					packedLight,
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
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new ComponentStringDrawContext(
				new Matrix4f(transform),
				(Font) (Object) this,
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				displayMode,
				backgroundColor,
				packedLight
		));

		return 0;
	}

	@WrapMethod(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
	public int renderGuiStringFast4(
			FormattedCharSequence	text,
			float					textX,
			float					textY,
			int						textColor,
			boolean					dropShadow,
			Matrix4f				transform,
			MultiBufferSource		bufferSource,
			Font.DisplayMode		displayMode,
			int						backgroundColor,
			int						packedLight,
			Operation<Integer>		original
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
					packedLight
			);
		}

		GuiBatchingController.INSTANCE.submitString(new FormattedStringDrawContext(
				new Matrix4f(transform),
				(Font) (Object) this,
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				displayMode,
				backgroundColor,
				packedLight
		));

		return 0;
	}

	@WrapMethod(method = "drawInBatch8xOutline")
	public void renderGuiStringFast5(
			FormattedCharSequence	text,
			float					textX,
			float					textY,
			int						textColor,
			int						backgroundColor,
			Matrix4f				transform,
			MultiBufferSource		bufferSource,
			int						packedLight,
			Operation<Integer>		original
	) {
		if (!CoreFeature.isGuiBatching()) {
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

		GuiBatchingController.INSTANCE.submitString(new Outline8StringDrawContext(
				new Matrix4f(transform),
				(Font) (Object) this,
				text,
				textX,
				textY,
				textColor,
				backgroundColor,
				packedLight
		));
	}
}
