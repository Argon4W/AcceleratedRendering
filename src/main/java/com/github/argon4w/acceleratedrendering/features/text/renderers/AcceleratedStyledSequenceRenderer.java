package com.github.argon4w.acceleratedrendering.features.text.renderers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.SimpleMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.key.ISequenceKey;
import com.github.argon4w.acceleratedrendering.features.text.key.IndexKey;
import com.github.argon4w.acceleratedrendering.features.text.key.SimpleSequenceKey;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.List;
import java.util.Map;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedStyledSequenceRenderer implements IAcceleratedRenderer<ISequenceKey> {

	public static final AcceleratedStyledSequenceRenderer INSTANCE = new AcceleratedStyledSequenceRenderer();

	private final Map	<ISequenceKey, Sequence>	sequencesByKey;
	private final List	<Sequence>					sequencesByIdx;

	public AcceleratedStyledSequenceRenderer() {
		this.sequencesByKey = new Object2ObjectOpenHashMap	<>();
		this.sequencesByIdx = new ObjectArrayList			<>();
	}

	public ISequenceKey getIndexKey(ISequenceKey key) {
		return new IndexKey(key, getSequence(key).getIndex());
	}

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			ISequenceKey	sequenceKey,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var sequence	= getSequence					(sequenceKey);
		var extension 	= vertexConsumer.getAccelerated	();
		var meshes		= sequence		.getMeshes		();
		var merges		= sequence		.getMerges		();
		var mesh		= meshes		.get			(extension);

		extension.beginTransform(transform, normal);

		if (mesh != null) {
			mesh.write(
					extension,
					color,
					light,
					overlay
			);

			extension.endTransform();
			return;
		}

		var advance			= 0.0f;
		var meshCollector	= new SimpleMeshCollector	(extension.getLayout());
		var meshBuilder		= extension.decorate		(meshCollector);

		var mcFont	= Minecraft		.getInstance	().font;
		var texts	= sequenceKey	.getTexts		();
		var font	= sequenceKey	.getFont		();
		var shadow	= sequenceKey	.isShadow		();
		var outline	= sequenceKey	.isOutline		();
		var italic	= sequenceKey	.isItalic		();
		var bold	= sequenceKey	.isBold			();
		var fontSet	= mcFont		.getFontSet		(font);
		var filter	= mcFont		.filterFishyGlyphs;

		for (int text : texts) {
			var codePoint = text & 0x1FFFFF;

			var glyphInfo		= fontSet	.getGlyphInfo	(codePoint, filter);
			var bakedGlyph		= fontSet	.getGlyph		(codePoint);
			var glyphAdvance	= glyphInfo	.getAdvance		(bold);
			var boldOffset		= glyphInfo	.getBoldOffset	();
			var shadowOffset	= glyphInfo	.getShadowOffset();
			var positionX		= advance;

			advance += glyphAdvance;

			if (bakedGlyph instanceof EmptyGlyph) {
				continue;
			}

			if (!SimpleSequenceKey.isHidden(text)) {
				var glyphBold		= bold		? boldOffset	: 0.0f;
				var glyphShadow		= shadow	? shadowOffset	: 0.0f;

				buildCodepointMesh(
						meshBuilder,
						bakedGlyph,
						italic,
						outline,
						positionX	+ glyphShadow,
						0			+ glyphShadow,
						shadowOffset
				);

				if (bold) {
					buildCodepointMesh(
							meshBuilder,
							bakedGlyph,
							italic,
							outline,
							positionX	+ glyphShadow + glyphBold,
							0			+ glyphShadow,
							shadowOffset
					);
				}
			}
		}

		var data	= meshCollector	.getData	();
		var buffer	= meshCollector	.getBuffer	();
		mesh		= merges		.get		(data);

		if (mesh != null) {
			buffer.close();
		} else {
			var builder = AcceleratedEntityRenderingFeature
					.getMeshType()
					.getBuilder	();

			mesh = builder.build(
					meshCollector,
					false,
					true,
					0
			);
		}

		meshes	.put	(extension, mesh);
		merges	.put	(data,		mesh);
		mesh	.write	(
				extension,
				color,
				light,
				overlay
		);
	}

	private void buildCodepointMesh(
			VertexConsumer	meshBuilder,
			BakedGlyph		bakedGlyph,
			boolean			italic,
			boolean			outline,
			float			offsetX,
			float			offsetY,
			float			shadowOffset
	) {
		var offsetUp			= bakedGlyph.up		- 3.0f;
		var offsetDown			= bakedGlyph.down	- 3.0f;
		var italicOffsetUp		= italic ? 1.0f - 0.25f * offsetUp		: 0.0f;
		var italicOffsetDown	= italic ? 1.0f - 0.25f * offsetDown	: 0.0f;

		var positions = new Vector2f[] {
				new Vector2f(bakedGlyph.left	+ italicOffsetUp,	offsetUp),
				new Vector2f(bakedGlyph.left	+ italicOffsetDown,	offsetDown),
				new Vector2f(bakedGlyph.right	+ italicOffsetDown,	offsetDown),
				new Vector2f(bakedGlyph.right	+ italicOffsetUp,	offsetUp)
		};

		var texCoords = new Vector2f[] {
				new Vector2f(bakedGlyph.u0, bakedGlyph.v0),
				new Vector2f(bakedGlyph.u0, bakedGlyph.v1),
				new Vector2f(bakedGlyph.u1, bakedGlyph.v1),
				new Vector2f(bakedGlyph.u1, bakedGlyph.v0),
		};

		if (outline) {
			for		(var outlineOffsetX = -1; outlineOffsetX <= 1; outlineOffsetX ++) {
				for	(var outlineOffsetY = -1; outlineOffsetY <= 1; outlineOffsetY ++) {
					if (		outlineOffsetX != 0
							||	outlineOffsetY != 0
					) {
						bakeQuad(
								meshBuilder,
								positions,
								texCoords,
								offsetX + shadowOffset * outlineOffsetX,
								offsetY + shadowOffset * outlineOffsetY
						);
					}
				}
			}
		} else {
			bakeQuad(
					meshBuilder,
					positions,
					texCoords,
					offsetX,
					offsetY
			);
		}
	}

	public void bakeQuad(
			VertexConsumer	meshBuilder,
			Vector2f[]		positions,
			Vector2f[]		texCoords,
			float			offsetX,
			float			offsetY
	) {
		for (var i = 0; i < 4; i ++) {
			var positionX	= positions[i].x() + offsetX;
			var positionY	= positions[i].y() + offsetY;
			var texCoord	= texCoords[i];

			meshBuilder.vertex(
					positionX,
					positionY,
					0.0f,
					1.0f,
					1.0f,
					1.0f,
					1.0f,
					texCoord.x,
					texCoord.y,
					0,
					0,
					0.0f,
					0.0f,
					0.0f
			);
		}
	}

	private Sequence getSequence(ISequenceKey key) {
		var sequence = key instanceof IndexKey idx
				? sequencesByIdx.get(idx.id())
				: sequencesByKey.get(key);

		if (sequence == null) {
			sequence = new Sequence(key.bake());
		}

		return sequence;
	}

	@Getter
	private class Sequence {

		private final Map<IBufferGraph,	IMesh>	meshes;
		private final Map<MeshData,		IMesh>	merges;
		private final int						index;

		public Sequence(ISequenceKey key) {
			this.meshes	= new Object2ObjectArrayMap		<>	();
			this.merges	= new Object2ObjectOpenHashMap	<>	();
			this.index	= sequencesByIdx.size				();

			sequencesByKey.put(key,	this);
			sequencesByIdx.add(		this);
		}
	}

	public void reload() {
		sequencesByKey.clear();
		sequencesByIdx.clear();
	}
}
