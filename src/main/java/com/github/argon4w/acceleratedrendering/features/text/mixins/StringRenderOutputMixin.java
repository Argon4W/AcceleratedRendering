package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.features.text.cache.ComponentMesh;
import com.github.argon4w.acceleratedrendering.features.text.IAcceleratedStringRenderOutput;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedSequenceEffectRenderer;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedStyledSequenceRenderer;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.extensions.BakedGlyphExtension;
import com.github.argon4w.acceleratedrendering.features.text.key.SimpleSequenceKey;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ExtensionMethod({
		VertexConsumerExtension	.class,
		BufferSourceExtension	.class,
		BakedGlyphExtension		.class
})
@Mixin(Font.StringRenderOutput.class)
public class StringRenderOutputMixin implements IAcceleratedStringRenderOutput {

	@Shadow							float						x;
	@Shadow							float						y;
	@Shadow @Final					Font						this$0;
	@Shadow @Final					MultiBufferSource			bufferSource;

	@Shadow @Final @Mutable private	Font.DisplayMode			mode;
	@Shadow @Final private			Matrix4f					pose;
	@Shadow @Final private			boolean						dropShadow;
	@Shadow @Final private			float						dimFactor;
	@Shadow @Final private			float						r;
	@Shadow @Final private			float						g;
	@Shadow @Final private			float						b;
	@Shadow @Final private			float						a;
	@Shadow @Final private			int							packedLightCoords;

	@Unique private static final	Matrix4f					SCRATCH	= new Matrix4f().identity		();
	@Unique private static final	Matrix3f					NORMAL	= new Matrix3f().identity		();
	@Unique private static final	SimpleSequenceKey.Mutable	MUTABLE	= new SimpleSequenceKey.Mutable	();

	@Unique	private					ComponentMesh.Builder		mesh		= null;
	@Unique private					RenderType					type		= null;
	@Unique private					Style						style		= null;
	@Unique private					boolean						accelerated	= false;
	@Unique private					boolean						outline		= false;
	@Unique private					int							color		= 0;
	@Unique private					float						advance		= 0.0f;

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	public void onInit(
			Font				this$0,
			MultiBufferSource	bufferSource,
			float				positionX,
			float				positionY,
			int					color,
			boolean				shadow,
			Matrix4f			pose,
			Font.DisplayMode	mode,
			int					light,
			CallbackInfo		ci
	) {
		if (			CoreFeature						.isLoaded						()
				&&		bufferSource.getAcceleratable()	.isBufferSourceAcceleratable	()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			this.accelerated	= true;
			this.advance		= 0.0f;
		}
	}

	@Inject(
			method		= "accept",
			at			= @At("HEAD"),
			cancellable	= true
	)
	public void onAccept(
			int								position,
			Style							style,
			int								codePoint,
			CallbackInfoReturnable<Boolean>	cir
	) {
		if (accelerated) {
			cir.setReturnValue(true);

			var italic	= style		.isItalic		();
			var bold	= style		.isBold			();
			var font	= style		.getFont		();
			var fontSet	= this$0	.getFontSet		(font);
			var info	= fontSet	.getGlyphInfo	(codePoint, this$0.filterFishyGlyphs);
			var glyph	= fontSet	.getGlyph		(codePoint);
			var type	= glyph		.renderType		(mode);
			var advance	= info		.getAdvance		(bold);

			if (this.style == null) {
				setup(style, type);
			} else {
				if (		!this.type	.equals(type)
						||	!this.style	.equals(style)
				) {
					flush(fontSet);
					setup(style, type);
				}
			}

			if (style.isObfuscated() && codePoint != 32) {
				if (mesh != null) {
					mesh.addAdvance		(advance);
					mesh.addObfuscated	(
							info,
							this.style,
							this.advance + MUTABLE.getAdvance()
					);
				}

				glyph = fontSet.getRandomGlyph(info);

				var boldOffset		= bold			? info.getBoldOffset	() : 0.0f;
				var shadowOffset	= dropShadow	? info.getShadowOffset	() : 0.0f;

				var extension1 = glyph							.getAccelerated();
				var extension2 = bufferSource.getBuffer(type)	.getAccelerated();

				if (extension2.isAccelerated()) {
					var renderer = extension1.getRenderer(italic);

					SCRATCH.set			(pose);
					SCRATCH.translate	(
							this.x + shadowOffset + MUTABLE.getAdvance(),
							this.y + shadowOffset,
							0.0f
					);

					extension2.doRender(
							renderer,
							null,
							SCRATCH,
							NORMAL,
							packedLightCoords,
							OverlayTexture.NO_OVERLAY,
							color
					);

					if (bold) {
						SCRATCH.translate(
								boldOffset,
								0.0f,
								0.0f
						);

						extension2.doRender(
								renderer,
								null,
								SCRATCH,
								NORMAL,
								packedLightCoords,
								OverlayTexture.NO_OVERLAY,
								color
						);
					}
				} else {
					throw new IllegalStateException("Someone uses incorrect render type in the baked glyph.");
				}

				MUTABLE.addHidden	(codePoint);
				MUTABLE.addAdvance	(advance);
			} else {
				MUTABLE.addCodePoint	(codePoint);
				MUTABLE.addAdvance		(advance);
			}
		}
	}

	@Inject(
			method	= "finish",
			at		= @At("HEAD")
	)
	public void onFinish(
			int								backgroundColor,
			float							x,
			CallbackInfoReturnable<Float>	cir
	) {
		flush();
	}

	@Unique
	private void setup(Style style, RenderType type) {
		this.style	= style;
		this.type	= type;

		var textColor = style.getColor();

		if (textColor != null) {
			this.color = textColor.getValue();
			this.color = FastColor.ARGB32.color(
					(int) (a * 255.0f),
					(int) (FastColor.ARGB32.red		(this.color) * dimFactor),
					(int) (FastColor.ARGB32.green	(this.color) * dimFactor),
					(int) (FastColor.ARGB32.blue	(this.color) * dimFactor)
			);
		} else {
			this.color = FastColor.ARGB32.color(
					(int) (a * 255.0f),
					(int) (r * 255.0f),
					(int) (g * 255.0f),
					(int) (b * 255.0f)
			);
		}

		MUTABLE.reset		();
		MUTABLE.setStyle	(
				this.style,
				this.dropShadow,
				this.outline
		);
	}

	@Unique
	@Override
	public void flush() {
		if (			this.accelerated
				&& 		this.style	!= null
				&&		this.type	!= null
				&&	!	MUTABLE.isEmpty()
		) {
			flush(this$0.getFontSet(style.getFont()));
		}

		this.style		= null;
		this.type		= null;
		this.outline	= false;
		this.color		= 0;
		this.advance	= 0.0f;

		MUTABLE.reset();
	}

	@Unique
	private void flush(FontSet fontSet) {
		var extension1 = bufferSource.getBuffer(type).getAccelerated();

		if (extension1.isAccelerated()) {
			if (mesh != null) {
				mesh.addAdvance	(MUTABLE.getAdvance());
				mesh.addSequence(
						MUTABLE.bake(),
						this.type,
						this.advance
				);
			}

			SCRATCH.set			(pose);
			SCRATCH.translate	(
					this.x,
					this.y,
					0.0f
			);

			extension1.doRender(
					AcceleratedStyledSequenceRenderer.INSTANCE,
					MUTABLE,
					SCRATCH,
					NORMAL,
					packedLightCoords,
					OverlayTexture.NO_OVERLAY,
					color
			);
		} else {
			throw new IllegalStateException("Someone uses incorrect render type in the baked glyph.");
		}

		if (		style.isStrikethrough	()
				||	style.isUnderlined		()
		) {
			var extension2 = bufferSource.getBuffer(fontSet.whiteGlyph().renderType(mode)).getAccelerated();

			if (extension2.isAccelerated()) {
				extension2.doRender(
						AcceleratedSequenceEffectRenderer.INSTANCE,
						MUTABLE,
						SCRATCH,
						NORMAL,
						packedLightCoords,
						OverlayTexture.NO_OVERLAY,
						color
				);
			} else {
				throw new IllegalStateException("Someone uses incorrect render type in the baked glyph.");
			}
		}

		this.x			+= MUTABLE.getAdvance();
		this.advance	+= MUTABLE.getAdvance();
	}

	@Override
	public void setPosition(float positionX, float positionY) {
		this.x = positionX;
		this.y = positionY;
	}

	@Override
	public void setAccelerated(boolean accelerated) {
		this.accelerated = accelerated;
	}

	@Override
	public void setOutline(boolean outline) {
		this.outline = outline;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
	}

	@Override
	public void setMode(Font.DisplayMode mode) {
		this.mode = mode;
	}

	@Override
	public void beginMesh() {
		mesh = new ComponentMesh.Builder();
	}

	@Override
	public ComponentMesh bake() {
		return mesh == null ? null : mesh.build(dropShadow);
	}
}
