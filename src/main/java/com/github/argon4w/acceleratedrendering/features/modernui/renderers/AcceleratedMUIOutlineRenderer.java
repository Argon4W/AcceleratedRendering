package com.github.argon4w.acceleratedrendering.features.modernui.renderers;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icyllis.modernui.mc.text.GLBakedGlyph;
import lombok.experimental.ExtensionMethod;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedMUIOutlineRenderer implements IAcceleratedRenderer<AcceleratedMUIOutlineRenderer.Context> {

	public static final AcceleratedMUIOutlineRenderer INSTANCE = new AcceleratedMUIOutlineRenderer();

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
		var extension	= vertexConsumer	.getAccelerated	();
		var glyph		= context			.glyph			();
		var glyphX		= context			.glyphX			();
		var glyphY		= context			.glyphY			();
		var width		= context			.width			();
		var height		= context			.height			();
		var sBloat		= context			.sBloat			();

		var uBloat = (glyph.u2 - glyph.u1) / (float) glyph.width;
		var vBloat = (glyph.v2 - glyph.v1) / (float) glyph.height;

		extension.beginTransform(transform, normal);

		vertexConsumer.vertex(glyphX			- sBloat,	glyphY			- sBloat, 0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u1 - uBloat, glyph.v1 - vBloat, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(glyphX			- sBloat,	glyphY + height	+ sBloat, 0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u1 - uBloat, glyph.v2 + vBloat, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(glyphX + width	+ sBloat,	glyphY + height	+ sBloat, 0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u2 + uBloat, glyph.v2 + vBloat, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(glyphX + width	+ sBloat,	glyphY			- sBloat, 0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u2 + uBloat, glyph.v1 - vBloat, overlay, light, 0.0F, 0.0F, 0.0F);

		extension.endTransform();
	}

	public static Context context(
			GLBakedGlyph	glyph,
			float			glyphX,
			float			glyphY,
			float			width,
			float			height,
			float			sBloat
	) {
		return new Context(
				glyph,
				glyphX,
				glyphY,
				width,
				height,
				sBloat
		);
	}

	public record Context(
			GLBakedGlyph	glyph,
			float			glyphX,
			float			glyphY,
			float			width,
			float			height,
			float			sBloat
	) {

	}
}
