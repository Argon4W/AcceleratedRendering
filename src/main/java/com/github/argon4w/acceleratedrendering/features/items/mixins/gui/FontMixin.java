package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.FontAdvanceEstimator;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Font.class)
public abstract class FontMixin {

	@Shadow public abstract boolean isBidirectional();

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
	public int renderGuiStringFast1(
			String				textString,
			float				textX,
			float				textY,
			int					textColor,
			boolean				textShadow,
			Matrix4f			transform,
			MultiBufferSource	bufferSource,
			Font.DisplayMode	displayMode,
			int					backgroundColor,
			int					packedLight,
			Operation<Integer>	original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textString,
					textX,
					textY,
					textColor,
					textShadow,
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
				textString,
				textX,
				textY,
				textColor,
				textShadow,
				displayMode,
				backgroundColor,
				packedLight,
				isBidirectional()
		));

		return FontAdvanceEstimator.INSTANCE.getAdvance(
				Style.EMPTY,
				textString,
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;IIZ)I")
	public int renderGuiStringFast2(
			String				textString,
			float				textX,
			float				textY,
			int					textColor,
			boolean				textShadow,
			Matrix4f			transform,
			MultiBufferSource	bufferSource,
			Font.DisplayMode	displayMode,
			int					backgroundColor,
			int					packedLight,
			boolean				bidirectional,
			Operation<Integer>	original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textString,
					textX,
					textY,
					textColor,
					textShadow,
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
				textString,
				textX,
				textY,
				textColor,
				textShadow,
				displayMode,
				backgroundColor,
				packedLight,
				bidirectional
		));

		return FontAdvanceEstimator.INSTANCE.getAdvance(
				Style.EMPTY,
				textString,
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch(Lnet/minecraft/network/chat/Component;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
	public int renderGuiStringFast3(
			Component			textComponent,
			float				textX,
			float				textY,
			int					textColor,
			boolean				textShadow,
			Matrix4f			transform,
			MultiBufferSource	bufferSource,
			Font.DisplayMode	displayMode,
			int					backgroundColor,
			int					packedLight,
			Operation<Integer>	original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textComponent,
					textX,
					textY,
					textColor,
					textShadow,
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
				textComponent,
				textX,
				textY,
				textColor,
				textShadow,
				displayMode,
				backgroundColor,
				packedLight
		));

		return FontAdvanceEstimator.INSTANCE.getAdvance(
				textComponent.getVisualOrderText(),
				textShadow,
				textX
		);
	}

	@WrapMethod(method = "drawInBatch(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)I")
	public int renderGuiStringFast4(
			FormattedCharSequence	textSequence,
			float					textX,
			float					textY,
			int						textColor,
			boolean					textShadow,
			Matrix4f				transform,
			MultiBufferSource		bufferSource,
			Font.DisplayMode		displayMode,
			int						backgroundColor,
			int						packedLight,
			Operation<Integer>		original
	) {
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
		) {
			return original.call(
					textSequence,
					textX,
					textY,
					textColor,
					textShadow,
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
				textSequence,
				textX,
				textY,
				textColor,
				textShadow,
				displayMode,
				backgroundColor,
				packedLight
		));

		return FontAdvanceEstimator.INSTANCE.getAdvance(
				textSequence,
				textShadow,
				textX
		);
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
		if (		!	CoreFeature.isLoaded				()
				||	!	CoreFeature.isGuiBatching			()
				||		CoreFeature.shouldByPassGuiBatching	()
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
