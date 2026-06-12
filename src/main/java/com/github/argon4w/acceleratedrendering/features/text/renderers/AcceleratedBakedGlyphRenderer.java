package com.github.argon4w.acceleratedrendering.features.text.renderers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.SimpleMeshCollector;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Map;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedBakedGlyphRenderer implements IAcceleratedRenderer<Void> {

	private final Map<IBufferGraph, IMesh>	meshes;
	private final BakedGlyph				bakedGlyph;
	private final boolean					italic;

	public AcceleratedBakedGlyphRenderer(BakedGlyph bakedGlyph, boolean italic) {
		this.meshes		= new Object2ObjectArrayMap<>();
		this.bakedGlyph	= bakedGlyph;
		this.italic		= italic;
	}

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Void			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
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

		var meshCollector		= new SimpleMeshCollector	(extension.getLayout());
		var meshBuilder			= extension.decorate		(meshCollector);

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

		for (var i = 0; i < 4; i ++) {
			var position = new Vector3f(positions[i], 0.0f);
			var texCoord = texCoords[i];

			meshBuilder
					.vertex	(
							position.x,
							position.y,
							position.z
					)
					.color	(-1)
					.uv		(texCoord.x, texCoord.y)
					.uv2	(0);
		}

		mesh = AcceleratedTextRenderingFeature
				.getMeshType()
				.getBuilder	()
				.build		(meshCollector);

		meshes	.put	(extension, mesh);
		mesh	.write	(
				extension,
				color,
				light,
				overlay
		);

		extension.endTransform();
	}
}
