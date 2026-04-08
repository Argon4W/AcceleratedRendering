package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class PoseStackExtension {

	public static void setPose(
			PoseStack	in,
			Matrix4f	transform,
			Matrix3f	normal
	) {
		var last = in.last();

		last.pose	().set(transform);
		last.normal	().set(normal);
	}
}
