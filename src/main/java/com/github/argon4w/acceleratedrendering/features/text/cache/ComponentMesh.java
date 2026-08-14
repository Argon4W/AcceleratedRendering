package com.github.argon4w.acceleratedrendering.features.text.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.utils.MatrixCacheStack;
import com.github.argon4w.acceleratedrendering.features.text.extensions.BakedGlyphExtension;
import com.github.argon4w.acceleratedrendering.features.text.key.ISequenceKey;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedSequenceEffectRenderer;
import com.github.argon4w.acceleratedrendering.features.text.renderers.AcceleratedStyledSequenceRenderer;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
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

	private final SequenceSet	[]	sequenceSets;
	private final Obfuscated	[]	obfuscatedGlyphs;
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

		for (int index = 0, size = obfuscatedGlyphs.length; index < size; index ++) {
			var obfuscated	= obfuscatedGlyphs[index];

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

		for (int index1 = 0, size1 = sequenceSets.length; index1 < size1; index1 ++) {
			var sequenceSet		= sequenceSets					[index1];
			var sequenceSize	= sequenceSet.getSequenceSize	();
			var glyphRenderType	= sequenceSet.getGlyphRenderType();
			var whiteRenderType	= sequenceSet.getWhiteRenderType();

			try (var view = MatrixCacheStack.acquire(sequenceSize)) {
				for (var index2 = 0; index2 < sequenceSize; index2 ++) {
					var matrix = view.get(index2);

					matrix.set		(transform);
					matrix.translate(
							positionX + sequenceSet.getSequenceOffset(index2),
							positionY,
							0.0f
					);
				}

				var builderGlyph	= bufferSource.getBuffer		(glyphRenderType);
				var extensionGlyph	= builderGlyph.getAccelerated	();

				for (var index2 = 0; index2 < sequenceSize; index2 ++) {
					var matrix		= view			.get			(index2);
					var sequenceKey	= sequenceSet	.getSequenceKey	(index2);
					var hasColor	= sequenceKey	.hasColor		();
					var textColor	= sequenceKey	.getColor		();

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

					if (extensionGlyph.isAccelerated()) {
						extensionGlyph.doRender(
								AcceleratedStyledSequenceRenderer.INSTANCE,
								sequenceKey,
								matrix,
								NORMAL,
								packedLight,
								OverlayTexture.NO_OVERLAY,
								color
						);
					} else {
						AcceleratedStyledSequenceRenderer.INSTANCE.buildSequenceMesh(
								builderGlyph,
								sequenceKey,
								matrix,
								color,
								packedLight
						);
					}
				}

				var builderWhite	= (VertexConsumer)				null;
				var extensionWhite	= (IAcceleratedVertexConsumer)	null;

				for (var index2 = 0; index2 < sequenceSize; index2 ++) {
					var effectKey = sequenceSet.getEffectKey(index2);

					if (		!effectKey.isUnderlined		()
							&&	!effectKey.isStrikethrough	()
					) {
						continue;
					}

					var matrix		= view		.get		(index2);
					var hasColor	= effectKey	.hasColor	();
					var textColor	= effectKey	.getColor	();

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

					if (builderWhite == null) {
						builderWhite	= bufferSource.getBuffer		(whiteRenderType);
						extensionWhite	= builderWhite.getAccelerated	();
					}

					if (extensionWhite.isAccelerated()) {
						extensionWhite.doRender(
								AcceleratedSequenceEffectRenderer.INSTANCE,
								effectKey,
								matrix,
								NORMAL,
								packedLight,
								OverlayTexture.NO_OVERLAY,
								color
						);
					} else {
						AcceleratedSequenceEffectRenderer.INSTANCE.buildSequenceMesh(
								builderWhite,
								effectKey,
								matrix,
								color,
								packedLight
						);
					}
				}
			}
		}

		return advance;
	}

	@AllArgsConstructor
	private static class SequenceSet {

		@Getter private final RenderType		glyphRenderType;
		@Getter private final RenderType		whiteRenderType;
		@Getter private	final int				sequenceSize;
		private			final float			[]	sequenceOffsets;
		private			final ISequenceKey	[]	sequenceKeys;
		private			final ISequenceKey	[]	effectKeys;

		public float getSequenceOffset(int index) {
			return sequenceOffsets[index];
		}

		public ISequenceKey getSequenceKey(int index) {
			return sequenceKeys[index];
		}

		public ISequenceKey getEffectKey(int index) {
			return effectKeys[index];
		}
	}

	private record Obfuscated(
			GlyphInfo	glyphInfo,
			Style		style,
			float		offset
	) {

	}

	public static class Builder {

		private final	Map				<Key, BuildingSequenceSet>	sequenceSetsByKey;
		private final	ObjectArrayList	<BuildingSequenceSet>		sequenceSetsByIdx;
		private final	ObjectArrayList	<Obfuscated>				obfuscatedGlyphs;
		private			float										advance;

		public Builder() {
			this.sequenceSetsByKey	= new Object2ObjectOpenHashMap	<>();
			this.sequenceSetsByIdx	= new ObjectArrayList			<>();
			this.obfuscatedGlyphs	= new ObjectArrayList			<>();
			this.advance			= 0.0f;
		}

		public void addSequence(
				ISequenceKey	sequenceKey,
				RenderType		glyphRenderType,
				RenderType		whiteRenderType,
				float			offset
		) {
			var key = new Key(
					glyphRenderType,
					whiteRenderType
			);

			var sequence = this.sequenceSetsByKey.get(key);

			if (sequence == null) {
				sequence = new BuildingSequenceSet(
						glyphRenderType,
						whiteRenderType
				);

				sequenceSetsByKey.put(key,	sequence);
				sequenceSetsByIdx.add(		sequence);
			}

			sequence.sequences.add(new BuildingSequenceSet.Sequence(
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
			var size = sequenceSetsByIdx.size();

			var sequenceSets = new SequenceSet[size];

			for (var index1 = 0; index1 < size; index1 ++) {
				var buildingSequenceSet	= sequenceSetsByIdx		.get				(index1);
				var glyphRenderType		= buildingSequenceSet	.getGlyphRenderType	();
				var whiteRenderType		= buildingSequenceSet	.getWhiteRenderType	();
				var sequences			= buildingSequenceSet	.getSequences		();

				var sequenceSize	= sequences.size	();
				var sequenceOffsets	= new float			[sequenceSize];
				var sequenceKeys	= new ISequenceKey	[sequenceSize];
				var effectKeys		= new ISequenceKey	[sequenceSize];

				for (var index2 = 0; index2 < sequenceSize; index2 ++) {
					var sequence = sequences.get(index2);

					sequenceOffsets	[index2] = sequence.sequenceOffset	();
					sequenceKeys	[index2] = sequence.sequenceKey		();
					effectKeys		[index2] = sequence.effectKey		();
				}

				sequenceSets[index1] = new SequenceSet(
						glyphRenderType,
						whiteRenderType,
						sequenceSize,
						sequenceOffsets,
						sequenceKeys,
						effectKeys
				);
			}

			return new ComponentMesh(
					sequenceSets,
					this.obfuscatedGlyphs.toArray(Obfuscated[]::new),
					this.advance,
					shadow
			);
		}

		private record Key(
				RenderType sequence,
				RenderType effect
		) {

		}

		@Getter
		private static class BuildingSequenceSet {

			private final List<Sequence>	sequences;
			private final RenderType		glyphRenderType;
			private final RenderType		whiteRenderType;

			private BuildingSequenceSet(RenderType glyphRenderType, RenderType whiteRenderType) {
				this.sequences			= new ObjectArrayList<>();
				this.glyphRenderType	= glyphRenderType;
				this.whiteRenderType	= whiteRenderType;
			}

			public record Sequence(
					float			sequenceOffset,
					ISequenceKey	sequenceKey,
					ISequenceKey	effectKey
			) {

			}
		}
	}
}
