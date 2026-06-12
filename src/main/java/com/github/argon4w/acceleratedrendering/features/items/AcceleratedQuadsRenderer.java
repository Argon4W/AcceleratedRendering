package com.github.argon4w.acceleratedrendering.features.items;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.features.items.colors.ILayerColors;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

@ExtensionMethod({
		VertexConsumerExtension	.class,
		BakedModelExtension		.class
})
public class AcceleratedQuadsRenderer implements IAcceleratedRenderer<AcceleratedQuadsRenderer.Context> {

	public static final AcceleratedQuadsRenderer INSTANCE = new AcceleratedQuadsRenderer();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Context			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
		var quads		= context		.quads			();
		var colors		= context		.colors			();

		extension.beginTransform(transform, normal);

		for (var quad : quads) {
			quad
					.getAccelerated	()
					.renderFast		(
							transform,
							normal,
							extension,
							light,
							overlay,
							colors.getColor(quad.getTintIndex())
					);
		}

		extension.endTransform();
	}

	public static Context context(List<BakedQuad> quads, ILayerColors colors) {
		return new Context(quads, colors);
	}

	public record Context(List<BakedQuad> quads, ILayerColors colors) {

	}
}
