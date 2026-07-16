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
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedSequenceEffectRenderer implements IAcceleratedRenderer<ISequenceKey> {

	public static final AcceleratedSequenceEffectRenderer	INSTANCE	= new AcceleratedSequenceEffectRenderer	();
	private	static final Matrix4f							IDENTITY	= new Matrix4f							().identity();
	private	static final Vector3f							SCRATCH		= new Vector3f							();

	private final Map	<ISequenceKey, Sequence>	sequencesByKey;
	private final List	<Sequence>					sequencesByIdx;

	public AcceleratedSequenceEffectRenderer() {
		this.sequencesByKey = new Object2ObjectOpenHashMap	<>();
		this.sequencesByIdx = new ObjectArrayList			<>();
	}

	public ISequenceKey getIndexKey(ISequenceKey key) {
		return key instanceof IndexKey ? key : new IndexKey(key, getSequence(key).getIndex());
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
		var extension	= vertexConsumer.getAccelerated	();
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

		var meshCollector	= new SimpleMeshCollector	(extension.getLayout());
		var meshBuilder		= extension.decorate		(meshCollector);

		buildSequenceMesh(
				meshBuilder,
				sequenceKey,
				IDENTITY,
				0xFF_FF_FF_FF,
				0
		);

		var data	= meshCollector	.getData	();
		var buffer	= meshCollector	.getBuffer	();
		mesh		= merges		.get		(data);

		if (mesh != null) {
			buffer.discard	();
			buffer.close	();
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

	public void buildSequenceMesh(
			VertexConsumer	meshBuilder,
			ISequenceKey	sequenceKey,
			Matrix4f		transform,
			int				color,
			int				light
	) {
		var advance	= 0.0f;
		var mcFont	= Minecraft		.getInstance	().font;
		var texts	= sequenceKey	.getTexts		();
		var font	= sequenceKey	.getFont		();
		var shadow	= sequenceKey	.isShadow		();
		var outline	= sequenceKey	.isOutline		();
		var bold	= sequenceKey	.isBold			();
		var fontSet	= mcFont		.getFontSet		(font);
		var filter	= mcFont		.filterFishyGlyphs;

		for (int text : texts) {
			var codePoint = text & 0x1FFFFF;

			var whiteGlyph		= fontSet	.whiteGlyph		();
			var glyphInfo		= fontSet	.getGlyphInfo	(codePoint, filter);
			var glyphAdvance	= glyphInfo	.getAdvance		(bold);
			var shadowOffset	= glyphInfo	.getShadowOffset();
			var positionX		= advance;

			advance += glyphAdvance;

			var effectOffset = shadow ? 1.0f : 0.0f;

			if (sequenceKey.isStrikethrough()) {
				buildEffectMesh(
						meshBuilder,
						whiteGlyph,
						transform,
						outline,
						glyphAdvance,
						4.5f,
						positionX	+ effectOffset,
						0			+ effectOffset,
						shadowOffset,
						color,
						light
				);
			}

			if (sequenceKey.isUnderlined()) {
				buildEffectMesh(
						meshBuilder,
						whiteGlyph,
						transform,
						outline,
						glyphAdvance,
						9.0f,
						positionX	+ effectOffset,
						0			+ effectOffset,
						shadowOffset,
						color,
						light
				);
			}
		}
	}

	private void buildEffectMesh(
			VertexConsumer	meshBuilder,
			BakedGlyph		bakedGlyph,
			Matrix4f		transform,
			boolean			outline,
			float			advance,
			float			position,
			float			offsetX,
			float			offsetY,
			float			shadowOffset,
			int				color,
			int				light
	) {
		var positions = new Vector2f[] {
				new Vector2f(-1.0f,		position),
				new Vector2f(advance,	position),
				new Vector2f(advance,	position - 1.0f),
				new Vector2f(-1.0f,		position - 1.0f),
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
					if (		outlineOffsetX !=0
							||	outlineOffsetY !=0
					) {
						bakeQuad(
								meshBuilder,
								transform,
								positions,
								texCoords,
								offsetX + shadowOffset * outlineOffsetX,
								offsetY + shadowOffset * outlineOffsetY,
								color,
								light
						);
					}
				}
			}
		} else {
			bakeQuad(
					meshBuilder,
					transform,
					positions,
					texCoords,
					offsetX,
					offsetY,
					color,
					light
			);
		}
	}

	public void bakeQuad(
			VertexConsumer	meshBuilder,
			Matrix4f		transform,
			Vector2f[]		positions,
			Vector2f[]		texCoords,
			float			offsetX,
			float			offsetY,
			int				color,
			int				light
	) {
		for (var i = 0; i < 4; i ++) {
			var texCoord = texCoords[i];

			transform.transformPosition(
					positions[i].x() + offsetX,
					positions[i].y() + offsetY,
					0.01f,
					SCRATCH
			);

			meshBuilder.addVertex(
					SCRATCH.x(),
					SCRATCH.y(),
					SCRATCH.z(),
					color,
					texCoord.x,
					texCoord.y,
					0,
					light,
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
			this.meshes = new Object2ObjectArrayMap		<>	();
			this.merges = new Object2ObjectOpenHashMap	<>	();
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
