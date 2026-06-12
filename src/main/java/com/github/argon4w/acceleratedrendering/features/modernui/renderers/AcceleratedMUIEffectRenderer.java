package com.github.argon4w.acceleratedrendering.features.modernui.renderers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedMUIEffectRenderer implements IAcceleratedRenderer<AcceleratedMUIEffectRenderer.Context> {

	public static final AcceleratedMUIEffectRenderer INSTANCE = new AcceleratedMUIEffectRenderer();

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
		var baseline	= context		.baseline		();
		var start		= context		.start			();
		var end			= context		.end			();

		extension.beginTransform(transform, normal);

		vertexConsumer.vertex(start,	baseline + 0.75F,	0.01F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), 0.0F, 1.0F, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(end,		baseline + 0.75F,	0.01F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), 1.0F, 1.0F, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(end,		baseline,			0.01F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), 1.0F, 0.0F, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(start,	baseline,			0.01F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), 0.0F, 0.0F, overlay, light, 0.0F, 0.0F, 0.0F);

		extension.endTransform();
	}

	public static Context context(
			float baseline,
			float start,
			float end
	) {
		return new Context(
				baseline,
				start,
				end
		);
	}

	public record Context(
			float baseline,
			float start,
			float end
	) {

	}
}
