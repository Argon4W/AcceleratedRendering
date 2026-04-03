package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

public interface IGuiElementContext {

	Matrix4f	transform	();
	Matrix3f	normal		();
	float		depth		();
}
