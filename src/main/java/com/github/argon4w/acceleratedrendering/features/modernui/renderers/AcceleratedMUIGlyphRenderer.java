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
public class AcceleratedMUIGlyphRenderer implements IAcceleratedRenderer<AcceleratedMUIGlyphRenderer.Context> {

	public static final AcceleratedMUIGlyphRenderer INSTANCE = new AcceleratedMUIGlyphRenderer();

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
		var upSkew		= context			.upSkew			();
		var downSkew	= context			.downSkew		();

		extension.beginTransform(transform, normal);

		vertexConsumer.vertex(glyphX			+ upSkew,	glyphY,				0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u1, glyph.v1, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(glyphX			+ downSkew,	glyphY + height,	0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u1, glyph.v2, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(glyphX + width	+ downSkew,	glyphY + height,	0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u2, glyph.v2, overlay, light, 0.0F, 0.0F, 0.0F);
		vertexConsumer.vertex(glyphX + width	+ upSkew,	glyphY,				0.0F, FastColor.ABGR32.red(color), FastColor.ABGR32.green(color), FastColor.ABGR32.blue(color), FastColor.ABGR32.alpha(color), glyph.u2, glyph.v1, overlay, light, 0.0F, 0.0F, 0.0F);

		extension.endTransform();
	}

	public static Context context(
			GLBakedGlyph	glyph,
			float			glyphX,
			float			glyphY,
			float			width,
			float			height,
			float			upSkew,
			float			downSkew
	) {
		return new Context(
				glyph,
				glyphX,
				glyphY,
				width,
				height,
				upSkew,
				downSkew
		);
	}

	public record Context(
			GLBakedGlyph	glyph,
			float			glyphX,
			float			glyphY,
			float			width,
			float			height,
			float			upSkew,
			float			downSkew
	) {

	}
}
