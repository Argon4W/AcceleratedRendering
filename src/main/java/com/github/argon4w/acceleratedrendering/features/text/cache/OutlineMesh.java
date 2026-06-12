package com.github.argon4w.acceleratedrendering.features.text.cache;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public record OutlineMesh(ComponentMesh outline, ComponentMesh main) {

	public void render(
			Font				mcFont,
			MultiBufferSource	bufferSource,
			Matrix4f			transform,
			float				positionX,
			float				positionY,
			int					packedLight,
			int					outlineColor,
			int					color
	) {
		outline.render(
				mcFont,
				Font.DisplayMode.NORMAL,
				bufferSource,
				transform,
				positionX,
				positionY,
				packedLight,
				outlineColor
		);

		main.render(
				mcFont,
				Font.DisplayMode.POLYGON_OFFSET,
				bufferSource,
				transform,
				positionX,
				positionY,
				packedLight,
				color
		);
	}
}
