package com.github.argon4w.acceleratedrendering.features.items.gui.renderer;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.FillDrawContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedFillRenderer implements IAcceleratedRenderer<FillDrawContext> {

	public static final AcceleratedFillRenderer INSTANCE = new AcceleratedFillRenderer();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			FillDrawContext	context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension = vertexConsumer.getAccelerated();

		extension.beginTransform(transform, normal);

		var blitOffset	= context.blitOffset();
		var minX		= context.minX		();
		var minY		= context.minY		();
		var maxX		= context.maxX		();
		var maxY		= context.maxY		();
		var swap		= 0;

		if (minX < maxX) {
			swap = minX;
			minX = maxX;
			maxX = swap;
		}

		if (minY < maxY) {
			swap = minY;
			minY = maxY;
			maxY = swap;
		}

		vertexConsumer.addVertex(minX, minY, blitOffset).setColor(color);
		vertexConsumer.addVertex(minX, maxY, blitOffset).setColor(color);
		vertexConsumer.addVertex(maxX, maxY, blitOffset).setColor(color);
		vertexConsumer.addVertex(maxX, minY, blitOffset).setColor(color);

		extension.endTransform();
	}
}
