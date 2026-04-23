package com.github.argon4w.acceleratedrendering.features.items.gui.renderer;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.BlitDrawContext;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedBlitRenderer implements IAcceleratedRenderer<BlitDrawContext> {

	public static final AcceleratedBlitRenderer INSTANCE = new AcceleratedBlitRenderer();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			BlitDrawContext	context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension = vertexConsumer.getAccelerated();

		extension.beginTransform(transform, normal);

		var minU		= context.minU		();
		var minV		= context.minV		();
		var maxU		= context.maxU		();
		var maxV		= context.maxV		();
		var minX		= context.minX		();
		var minY		= context.minY		();
		var maxX		= context.maxX		();
		var maxY		= context.maxY		();
		var blitOffset	= context.blitOffset();

		vertexConsumer.vertex(minX, minY, blitOffset).color(color).uv(minU, minV);
		vertexConsumer.vertex(minX, maxY, blitOffset).color(color).uv(minU, maxV);
		vertexConsumer.vertex(maxX, maxY, blitOffset).color(color).uv(maxU, maxV);
		vertexConsumer.vertex(maxX, minY, blitOffset).color(color).uv(maxU, minV);

		extension.endTransform();
	}
}
