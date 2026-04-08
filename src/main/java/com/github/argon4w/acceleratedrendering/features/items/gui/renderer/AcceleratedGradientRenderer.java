package com.github.argon4w.acceleratedrendering.features.items.gui.renderer;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.FillDrawContext;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.GradientDrawContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedGradientRenderer implements IAcceleratedRenderer<GradientDrawContext> {

	public static final AcceleratedGradientRenderer INSTANCE = new AcceleratedGradientRenderer();

	@Override
	public void render(
			VertexConsumer		vertexConsumer,
			GradientDrawContext	context,
			Matrix4f			transform,
			Matrix3f			normal,
			int					light,
			int					overlay,
			int					color
	) {
		var extension = vertexConsumer.getAccelerated();

		extension.beginTransform(transform, normal);

		var blitOffset	= context.blitOffset();
		var minX		= context.minX		();
		var minY		= context.minY		();
		var maxX		= context.maxX		();
		var maxY		= context.maxY		();
		var colorFrom	= context.colorFrom	();
		var colorTo		= context.colorTo	();

		vertexConsumer.addVertex(minX, minY, blitOffset).setColor(colorFrom);
		vertexConsumer.addVertex(minX, maxY, blitOffset).setColor(colorTo);
		vertexConsumer.addVertex(maxX, maxY, blitOffset).setColor(colorTo);
		vertexConsumer.addVertex(maxX, minY, blitOffset).setColor(colorFrom);

		extension.endTransform();
	}
}
