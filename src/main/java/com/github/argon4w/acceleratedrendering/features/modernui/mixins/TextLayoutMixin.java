package com.github.argon4w.acceleratedrendering.features.modernui.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.utils.DoNothingVertexConsumer;
import com.github.argon4w.acceleratedrendering.features.modernui.renderers.AcceleratedMUIBgRenderer;
import com.github.argon4w.acceleratedrendering.features.modernui.renderers.AcceleratedMUIGlyphRenderer;
import com.github.argon4w.acceleratedrendering.features.modernui.renderers.AcceleratedMUIOutlineRenderer;
import com.github.argon4w.acceleratedrendering.features.mods.ModsFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icyllis.modernui.mc.text.GLBakedGlyph;
import icyllis.modernui.mc.text.TextLayout;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(TextLayout				.class)
public class TextLayoutMixin {

	@Shadow(remap = false) @Final private float mTotalAdvance;

	@Unique private static final Matrix3f NORMAL = new Matrix3f().identity();

	@WrapOperation(
			method	= "drawText",
			at		= {
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 1
					),
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 2
					),
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 3
					)
			}
	)
	public VertexConsumer preventVanillaGlyphVertex(
			VertexConsumer						instance,
			Matrix4f							pose,
			float								positionX,
			float								positionY,
			float								positionZ,
			Operation<VertexConsumer>			original,
			@Share("skipGlyph") LocalBooleanRef	skipGlyph
	) {
		return skipGlyph.get() ? DoNothingVertexConsumer.INSTANCE : original.call(
				instance,
				pose,
				positionX,
				positionY,
				positionZ
		);
	}

	@WrapOperation(
			method	= "drawText",
			at		= {
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 5
					),
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 6
					),
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 7
					)
			}
	)
	public VertexConsumer preventVanillaBgVertex(
			VertexConsumer						instance,
			Matrix4f							pose,
			float								positionX,
			float								positionY,
			float								positionZ,
			Operation<VertexConsumer>			original,
			@Share("skipBg") LocalBooleanRef	skipBg
	) {
		return skipBg.get() ? DoNothingVertexConsumer.INSTANCE : original.call(
				instance,
				pose,
				positionX,
				positionY,
				positionZ
		);
	}

	@WrapOperation(
			method	= "drawTextOutline",
			at		= {
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 1
					),
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 2
					),
					@At(
							value	= "INVOKE",
							target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
							ordinal	= 3
					)
			}
	)
	public VertexConsumer preventVanillaOutlineVertex(
			VertexConsumer							instance,
			Matrix4f								pose,
			float									positionX,
			float									positionY,
			float									positionZ,
			Operation<VertexConsumer>				original,
			@Share("skipOutline") LocalBooleanRef	skipOutline
	) {
		return skipOutline.get() ? DoNothingVertexConsumer.INSTANCE : original.call(
				instance,
				pose,
				positionX,
				positionY,
				positionZ
		);
	}

	@WrapOperation(
			method	= "drawText",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
					ordinal	= 0
			)
	)
	public VertexConsumer drawMUIGlyph(
			VertexConsumer									instance,
			Matrix4f										pose,
			float											positionX,
			float											positionY,
			float											positionZ,
			Operation<VertexConsumer>						original,
			@Share("skipGlyph")				LocalBooleanRef	skipGlyph,
			@Local(name = "glyph")			GLBakedGlyph	glyph,
			@Local(name = "rx")				float			glyphX,
			@Local(name = "ry")				float			glyphY,
			@Local(name = "w")				float			width,
			@Local(name = "h")				float			height,
			@Local(name = "upSkew")			float			upSkew,
			@Local(name = "downSkew")		float			downSkew,
			@Local(name = "r")				int				red,
			@Local(name = "g")				int				green,
			@Local(name = "b")				int				blue,
			@Local(name = "a")				int				alpha,
			@Local(name = "packedLight")	int				packedLight
	) {
		var extension = instance.getAccelerated();

		if (			CoreFeature						.isLoaded						()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&		ModsFeature						.isEnabled						()
				&&		ModsFeature						.shouldAccelerateModernUI		()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
				&&		extension						.isAccelerated					()
		) {
			skipGlyph.set(true);

			extension.doRender(
					AcceleratedMUIGlyphRenderer.INSTANCE,
					AcceleratedMUIGlyphRenderer.context(
							glyph,
							glyphX,
							glyphY,
							width,
							height,
							upSkew,
							downSkew
					),
					pose,
					NORMAL,
					packedLight,
					OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(
							alpha,
							red,
							green,
							blue
					)
			);

			return DoNothingVertexConsumer.INSTANCE;
		}

		return original.call(
				instance,
				pose,
				positionX,
				positionY,
				positionZ
		);
	}

	@WrapOperation(
			method	= "drawText",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
					ordinal	= 4
			)
	)
	public VertexConsumer drawMUIBg(
			VertexConsumer									instance,
			Matrix4f										pose,
			float											positionX,
			float											positionY,
			float											positionZ,
			Operation<VertexConsumer>						original,
			@Share("skipBg")				LocalBooleanRef	skipBg,
			@Local(name = "x")				float			backgroundX,
			@Local(name = "top")			float			backgroundTop,
			@Local(name = "bgColor")		int				backgroundColor,
			@Local(name = "packedLight")	int				packedLight
	) {
		var extension = instance.getAccelerated();

		if (			CoreFeature						.isLoaded						()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&		ModsFeature						.isEnabled						()
				&&		ModsFeature						.shouldAccelerateModernUI		()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
				&&		extension						.isAccelerated					()
		) {
			skipBg.set(true);

			extension.doRender(
					AcceleratedMUIBgRenderer.INSTANCE,
					AcceleratedMUIBgRenderer.context(
							backgroundX,
							backgroundTop,
							mTotalAdvance
					),
					pose,
					NORMAL,
					packedLight,
					OverlayTexture.NO_OVERLAY,
					backgroundColor
			);

			return DoNothingVertexConsumer.INSTANCE;
		}

		return original.call(
				instance,
				pose,
				positionX,
				positionY,
				positionZ
		);
	}

	@WrapOperation(
			method	= "drawTextOutline",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/mojang/blaze3d/vertex/VertexConsumer;vertex(Lorg/joml/Matrix4f;FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
					ordinal	= 0
			)
	)
	public VertexConsumer drawMUIOutline(
			VertexConsumer									instance,
			Matrix4f										pose,
			float											positionX,
			float											positionY,
			float											positionZ,
			Operation<VertexConsumer>						original,
			@Share("skipOutline") 			LocalBooleanRef	skipOutline,
			@Local(name = "glyph")			GLBakedGlyph	glyph,
			@Local(name = "rx")				float			glyphX,
			@Local(name = "ry")				float			glyphY,
			@Local(name = "w")				float			width,
			@Local(name = "h")				float			height,
			@Local(name = "sBloat")			float			sBloat,
			@Local(name = "r")				int				red,
			@Local(name = "g")				int				green,
			@Local(name = "b")				int				blue,
			@Local(name = "a")				int				alpha,
			@Local(name = "packedLight")	int				packedLight
	) {
		var extension = instance.getAccelerated();

		if (			CoreFeature						.isLoaded						()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&		ModsFeature						.isEnabled						()
				&&		ModsFeature						.shouldAccelerateModernUI		()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
				&&		extension						.isAccelerated					()
		) {
			skipOutline.set(true);

			extension.doRender(
					AcceleratedMUIOutlineRenderer.INSTANCE,
					AcceleratedMUIOutlineRenderer.context(
							glyph,
							glyphX,
							glyphY,
							width,
							height,
							sBloat
					),
					pose,
					NORMAL,
					packedLight,
					OverlayTexture.NO_OVERLAY,
					FastColor.ARGB32.color(
							alpha,
							red,
							green,
							blue
					)
			);

			return DoNothingVertexConsumer.INSTANCE;
		}

		return original.call(
				instance,
				pose,
				positionX,
				positionY,
				positionZ
		);
	}
}
