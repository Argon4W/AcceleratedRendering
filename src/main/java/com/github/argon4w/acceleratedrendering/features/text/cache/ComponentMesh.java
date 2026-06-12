package com.github.argon4w.acceleratedrendering.features.text.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.features.text.extensions.BakedGlyphExtension;
import com.github.argon4w.acceleratedrendering.features.text.key.ISequenceKey;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedSequenceEffectRenderer;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedStyledSequenceRenderer;
import com.mojang.blaze3d.font.GlyphInfo;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.objects.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ExtensionMethod({
		VertexConsumerExtension	.class,
		BakedGlyphExtension		.class
})
public class ComponentMesh {

	public static final Matrix4f SCRATCH	= new Matrix4f().identity();
	public static final Matrix3f NORMAL		= new Matrix3f().identity();

	private final Map<RenderType, Sequences>	sequences;
	private final List<Obfuscated>				obfuscatedGlyphs;
	private final float							advance;
	private final boolean						shadow;

	public float render(
			Font				mcFont,
			Font.DisplayMode	mode,
			MultiBufferSource	bufferSource,
			Matrix4f			transform,
			float				positionX,
			float				positionY,
			int					packedLight,
			int					color
	) {
		var dimFactor = shadow ? 0.25f : 1.0f;

		var defaultColor = FastColor.ARGB32.color(
						FastColor.ARGB32.alpha	(color),
				(int) (	FastColor.ARGB32.red	(color) * dimFactor),
				(int) (	FastColor.ARGB32.green	(color) * dimFactor),
				(int) (	FastColor.ARGB32.blue	(color) * dimFactor)
		);

		for (int index = 0, size = obfuscatedGlyphs.size(); index < size; index ++) {
			var obfuscated	= obfuscatedGlyphs.get(index);

			var glyphInfo	= obfuscated.glyphInfo	();
			var style		= obfuscated.style		();
			var offset		= obfuscated.offset		();
			var font		= style		.getFont	();
			var bold		= style		.isBold		();
			var italic		= style		.isItalic	();
			var fontSet		= mcFont	.getFontSet	(font);

			var glyph = fontSet.getRandomGlyph(glyphInfo);

			var boldOffset		= bold		? glyphInfo.getBoldOffset	() : 0.0f;
			var shadowOffset	= shadow	? glyphInfo.getShadowOffset	() : 0.0f;

			var extension1 = glyph											.getAccelerated();
			var extension2 = bufferSource.getBuffer(glyph.renderType(mode))	.getAccelerated();

			if (extension2.isAccelerated()) {
				var renderer = extension1.getRenderer(italic);

				SCRATCH.set			(transform);
				SCRATCH.translate	(
						positionX + shadowOffset + offset,
						positionX + shadowOffset,
						0.0f
				);

				extension2.doRender(
						renderer,
						null,
						SCRATCH,
						NORMAL,
						packedLight,
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
							packedLight,
							OverlayTexture.NO_OVERLAY,
							color
					);
				}
			} else {
				throw new IllegalStateException("Someone uses incorrect render type in the baked glyph.");
			}
		}

		for (var sequences : sequences.values()) {
			var extension1 = bufferSource.getBuffer(sequences.getRenderType()).getAccelerated();

			if (extension1.isAccelerated()) {
				for (int index = 0, size = sequences.getSequenceKeys().size(); index < size; index ++) {
					var sequenceOffset	= sequences		.getSequenceOffsets	().getFloat	(index);
					var sequenceKey 	= sequences		.getSequenceKeys	().get		(index);
					var hasColor		= sequenceKey	.hasColor			();
					var textColor		= sequenceKey	.getColor			();

					if (hasColor) {
						color = FastColor.ARGB32.color(
										FastColor.ARGB32.alpha	(color),
								(int) (	FastColor.ARGB32.red	(textColor) * dimFactor),
								(int) (	FastColor.ARGB32.green	(textColor) * dimFactor),
								(int) (	FastColor.ARGB32.blue	(textColor) * dimFactor)
						);
					} else {
						color = defaultColor;
					}

					SCRATCH.set			(transform);
					SCRATCH.translate	(
							positionX + sequenceOffset,
							positionY,
							0.0f
					);

					extension1.doRender(
							AcceleratedStyledSequenceRenderer.INSTANCE,
							sequenceKey,
							SCRATCH,
							NORMAL,
							packedLight,
							OverlayTexture.NO_OVERLAY,
							color
					);

					if (		sequenceKey.isStrikethrough	()
							||	sequenceKey.isUnderlined	()
					) {
						var extension2 = bufferSource.getBuffer(mcFont.getFontSet(sequenceKey.getFont()).whiteGlyph().renderType(mode)).getAccelerated();

						if (extension2.isAccelerated()) {
							extension2.doRender(
									AcceleratedSequenceEffectRenderer.INSTANCE,
									sequenceKey,
									SCRATCH,
									NORMAL,
									packedLight,
									OverlayTexture.NO_OVERLAY,
									color
							);
						} else {
							throw new IllegalStateException("Someone uses incorrect render type in the baked glyph.");
						}
					}
				}
			} else {
				throw new IllegalStateException("Someone uses incorrect render type in the baked glyph.");
			}
		}

		return advance;
	}

	@Getter
	private static class Sequences {

		private final FloatList					sequenceOffsets;
		private final ObjectList<ISequenceKey>	sequenceKeys;
		private final ObjectList<ISequenceKey>	effectKeys;
		private final RenderType				renderType;

		public Sequences(RenderType renderType) {
			this.sequenceOffsets	= new FloatArrayList	();
			this.sequenceKeys		= new ObjectArrayList<>	();
			this.effectKeys			= new ObjectArrayList<>	();
			this.renderType			= renderType;
		}
	}

	private record Obfuscated(
			GlyphInfo	glyphInfo,
			Style		style,
			float		offset
	) {

	}

	public static class Builder {

		private final	Map	<RenderType, Sequences>	sequences;
		private final	List<Obfuscated>			obfuscatedGlyphs;
		private			float						advance;

		public Builder() {
			this.sequences			= new Object2ObjectArrayMap	<>();
			this.obfuscatedGlyphs	= new ObjectArrayList		<>();
			this.advance			= 0.0f;
		}

		public void addSequence(
				ISequenceKey	sequenceKey,
				RenderType		renderType,
				float			offset
		) {
			var sequence = this.sequences.get(renderType);

			if (sequence == null) {
				sequence = new Sequences(renderType);

				sequences.put(renderType, sequence);
			}

			sequence.effectKeys		.add(AcceleratedSequenceEffectRenderer.INSTANCE.getIndexKey(sequenceKey));
			sequence.sequenceKeys	.add(AcceleratedStyledSequenceRenderer.INSTANCE.getIndexKey(sequenceKey));
			sequence.sequenceOffsets.add(offset);
		}

		public void addMesh(ComponentMesh that) {
			for (var entry : that.sequences.entrySet()) {
				var thatRenderType	= entry				.getKey();
				var thatSequence	= entry				.getValue();
				var thisSequence	= this.sequences	.get(thatRenderType);

				if (thisSequence == null) {
					thisSequence = new Sequences(thatRenderType);

					sequences.put(thatRenderType, thisSequence);
				}

				thisSequence.effectKeys		.addAll(thatSequence.effectKeys);
				thisSequence.sequenceKeys	.addAll(thatSequence.sequenceKeys);
				thisSequence.sequenceOffsets.addAll(thatSequence.sequenceOffsets);
			}

			this.obfuscatedGlyphs.addAll(that.obfuscatedGlyphs);
		}

		public void addObfuscated(
				GlyphInfo	glyphInfo,
				Style		style,
				float		offset
		) {
			obfuscatedGlyphs.add(new Obfuscated(
					glyphInfo,
					style,
					offset
			));
		}

		public void addAdvance(float advance) {
			this.advance += advance;
		}

		public ComponentMesh build(boolean shadow) {
			return new ComponentMesh(
					this.sequences,
					this.obfuscatedGlyphs,
					this.advance,
					shadow
			);
		}
	}
}
