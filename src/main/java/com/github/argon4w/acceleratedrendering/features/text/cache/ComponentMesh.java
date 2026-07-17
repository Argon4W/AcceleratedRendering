package com.github.argon4w.acceleratedrendering.features.text.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.features.text.extensions.BakedGlyphExtension;
import com.github.argon4w.acceleratedrendering.features.text.key.ISequenceKey;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedSequenceEffectRenderer;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedStyledSequenceRenderer;
import com.mojang.blaze3d.font.GlyphInfo;
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

	private final List<SequenceSet>	sequenceSets;
	private final List<Obfuscated>	obfuscatedGlyphs;
	private final float				advance;
	private final boolean			shadow;

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

			var buffer = bufferSource.getBuffer(glyph.renderType(mode));

			var boldOffset		= bold		? glyphInfo.getBoldOffset	() : 0.0f;
			var shadowOffset	= shadow	? glyphInfo.getShadowOffset	() : 0.0f;

			var extension1 = glyph	.getAccelerated();
			var extension2 = buffer	.getAccelerated();

			if (extension2.isAccelerated()) {
				var renderer = extension1.getRenderer(italic);

				SCRATCH.set			(transform);
				SCRATCH.translate	(
						positionX + shadowOffset + offset,
						positionY + shadowOffset,
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
				mcFont.renderChar(
						glyph,
						bold,
						italic,
						boldOffset,
						positionX + shadowOffset + offset,
						positionY + shadowOffset,
						transform,
						buffer,
						FastColor.ARGB32.red	(color),
						FastColor.ARGB32.green	(color),
						FastColor.ARGB32.blue	(color),
						FastColor.ARGB32.alpha	(color),
						packedLight
				);
			}
		}

		for (int index1 = 0, size1 = sequenceSets.size(); index1 < size1; index1 ++) {
			var sequenceSet	= sequenceSets	.get			(index1);
			var renderType	= sequenceSet	.getRenderType	();
			var effectType	= sequenceSet	.getEffectType	();

			var extensionRenderType = (IAcceleratedVertexConsumer) null;
			var extensionEffectType = (IAcceleratedVertexConsumer) null;

			for (int index2 = 0, size2 = sequenceSet.getSequences().size(); index2 < size2; index2 ++) {
				var sequence		= sequenceSet.getSequences().get			(index2);
				var sequenceOffset	= sequence					.sequenceOffset	();
				var sequenceKey 	= sequence					.sequenceKey	();
				var effectKey		= sequence					.effectKey		();
				var hasColor		= sequenceKey				.hasColor		();
				var textColor		= sequenceKey				.getColor		();

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

				if (extensionRenderType == null) {
					extensionRenderType = bufferSource.getBuffer(renderType).getAccelerated();
				}

				if (extensionRenderType.isAccelerated()) {
					extensionRenderType.doRender(
							AcceleratedStyledSequenceRenderer.INSTANCE,
							sequenceKey,
							SCRATCH,
							NORMAL,
							packedLight,
							OverlayTexture.NO_OVERLAY,
							color
					);
				} else {
					AcceleratedStyledSequenceRenderer.INSTANCE.buildSequenceMesh(
							bufferSource.getBuffer(renderType),
							sequenceKey,
							SCRATCH,
							color,
							packedLight
					);
				}

				if (		effectKey.isUnderlined		()
						||	effectKey.isStrikethrough	()
				) {
					if (extensionEffectType == null) {
						extensionEffectType = bufferSource.getBuffer(effectType).getAccelerated();
					}

					if (extensionEffectType.isAccelerated()) {
						extensionEffectType.doRender(
								AcceleratedSequenceEffectRenderer.INSTANCE,
								effectKey,
								SCRATCH,
								NORMAL,
								packedLight,
								OverlayTexture.NO_OVERLAY,
								color
						);
					} else {
						AcceleratedSequenceEffectRenderer.INSTANCE.buildSequenceMesh(
								bufferSource.getBuffer(effectType),
								effectKey,
								SCRATCH,
								color,
								packedLight
						);
					}
				}
			}
		}

		return advance;
	}

	@Getter
	private static class SequenceSet {

		private final ObjectList<Sequence>		sequences;
		private final RenderType				renderType;
		private final RenderType				effectType;

		public SequenceSet(RenderType renderType, RenderType effectType) {
			this.sequences	= new ObjectArrayList<>();
			this.renderType	= renderType;
			this.effectType	= effectType;
		}

		public record Sequence(
				float			sequenceOffset,
				ISequenceKey	sequenceKey,
				ISequenceKey	effectKey
		) {

		}
	}

	private record Obfuscated(
			GlyphInfo	glyphInfo,
			Style		style,
			float		offset
	) {

	}

	public static class Builder {

		private final	Map	<Key, SequenceSet>	sequenceSetsByKey;
		private final	List<SequenceSet>		sequenceSetsByIdx;
		private final	List<Obfuscated>		obfuscatedGlyphs;
		private			float					advance;

		public Builder() {
			this.sequenceSetsByKey	= new Object2ObjectOpenHashMap	<>();
			this.sequenceSetsByIdx	= new ObjectArrayList			<>();
			this.obfuscatedGlyphs	= new ObjectArrayList			<>();
			this.advance			= 0.0f;
		}

		public void addSequence(
				ISequenceKey	sequenceKey,
				RenderType		renderType,
				RenderType		effectType,
				float			offset
		) {
			var key = new Key(
					renderType,
					effectType
			);

			var sequence = this.sequenceSetsByKey.get(key);

			if (sequence == null) {
				sequence = new SequenceSet(renderType, effectType);

				sequenceSetsByKey.put(key,	sequence);
				sequenceSetsByIdx.add(		sequence);
			}

			sequence.sequences.add(new SequenceSet.Sequence(
					offset,
					AcceleratedStyledSequenceRenderer.INSTANCE.getIndexKey(sequenceKey),
					AcceleratedSequenceEffectRenderer.INSTANCE.getIndexKey(sequenceKey)
			));
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
					this.sequenceSetsByIdx,
					this.obfuscatedGlyphs,
					this.advance,
					shadow
			);
		}

		private record Key(
				RenderType sequence,
				RenderType effect
		) {

		}
	}
}
