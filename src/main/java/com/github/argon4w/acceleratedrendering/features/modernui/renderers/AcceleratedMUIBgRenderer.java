package com.github.argon4w.acceleratedrendering.features.modernui.renderers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedMUIBgRenderer implements IAcceleratedRenderer<AcceleratedMUIBgRenderer.Context> {

	public static final AcceleratedMUIBgRenderer INSTANCE = new AcceleratedMUIBgRenderer();

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
		var extension			= vertexConsumer.getAccelerated		();
		var backgroundX			= context		.backgroundX		();
		var backgroundTop		= context		.backgroundTop		();
		var backgroundAdvance	= context		.backgroundAdvance	();

		extension.beginTransform(transform, normal);

		var red		= FastColor.ARGB32.red	(color) / 255.0F;
		var green	= FastColor.ARGB32.green(color) / 255.0F;
		var blue	= FastColor.ARGB32.blue	(color) / 255.0F;
		var alpha	= FastColor.ARGB32.alpha(color) / 255.0F;

		vertexConsumer.vertex(backgroundX - 1.0F,						backgroundTop + 9.0F, 0.01F, red, green, blue, alpha, 0.0F, 1.0F, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(backgroundX + backgroundAdvance + 1.0F,	backgroundTop + 9.0F, 0.01F, red, green, blue, alpha, 1.0F, 1.0F, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(backgroundX + backgroundAdvance + 1.0F,	backgroundTop - 1.0F, 0.01F, red, green, blue, alpha, 1.0F, 0.0F, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(backgroundX - 1.0F,						backgroundTop - 1.0F, 0.01F, red, green, blue, alpha, 0.0F, 0.0F, overlay, light, 0.0F, 0.0F, 0.0F);

		extension.endTransform();
	}

	public static Context context(
			float backgroundX,
			float backgroundTop,
			float backgroundAdvance
	) {
		return new Context(
				backgroundX,
				backgroundTop,
				backgroundAdvance
		);
	}

	public record Context(
			float backgroundX,
			float backgroundTop,
			float backgroundAdvance
	) {

	}
}
