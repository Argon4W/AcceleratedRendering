package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class CullerUtils {

	public static boolean shouldCull(Vertex[] vertices, NativeImage texture) {
		if (texture == null) {
			return false;
		}

		if (vertices.length == 4) {
			var vertex0 = new Vector3f(vertices[0].getPosition());
			var vector1 = new Vector3f(vertices[1].getPosition()).sub(vertex0);
			var vector2 = new Vector3f(vertices[2].getPosition()).sub(vertex0);
			var vector3 = new Vector3f(vertices[3].getPosition()).sub(vertex0);

			var length1 = vector1.cross(vector2).length();
			var length2 = vector1.cross(vector3).length();

			if (length1 == 0 && length2 == 0) {
				return true;
			}
		}

		var minU = 1.0f;
		var minV = 1.0f;
		var maxU = 0.0f;
		var maxV = 0.0f;

		for (int index = 0, size = vertices.length; index < size; index ++) {
			var vertex = vertices[index];

			var texCoord	= vertex	.getTexCoord();
			var texCoordU	= texCoord	.x			();
			var texCoordV	= texCoord	.y			();

			minU = Math.min(minU, texCoordU);
			minV = Math.min(minV, texCoordV);
			maxU = Math.max(maxU, texCoordU);
			maxV = Math.max(maxV, texCoordV);
		}

		var width	= texture.getWidth	();
		var height	= texture.getHeight	();

		var minX	= Mth.floor	(minU * texture.getWidth	());
		var minY	= Mth.floor	(minV * texture.getHeight	());
		var maxX	= Mth.ceil	(maxU * texture.getWidth	());
		var maxY	= Mth.ceil	(maxV * texture.getHeight	());

		for (		var x = minX; x <= maxX; x ++) {
			for (	var y = minY; y <= maxY; y ++) {
				var clampedX = x % width;
				var clampedY = y % height;

				clampedX = clampedX < 0 ? width		+ clampedX : clampedX;
				clampedY = clampedY < 0 ? height	+ clampedY : clampedY;

				if (FastColor.ABGR32.alpha(texture.getPixelRGBA(clampedX, clampedY)) != 0) {
					return false;
				}
			}
		}

		return true;
	}
}
