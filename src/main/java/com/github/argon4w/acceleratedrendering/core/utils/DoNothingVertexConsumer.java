package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;

public class DoNothingVertexConsumer implements VertexConsumer {

	public static final DoNothingVertexConsumer INSTANCE = new DoNothingVertexConsumer();

	@Override
	public VertexConsumer vertex(
			double x,
			double y,
			double z
	) {
		return this;
	}

	@Override
	public VertexConsumer color(
			int red,
			int green,
			int blue,
			int alpha
	) {
		return this;
	}

	@Override
	public VertexConsumer normal(
			float normalX,
			float normalY,
			float normalZ
	) {
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v) {
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v) {
		return this;
	}

	@Override
	public void endVertex() {

	}

	@Override
	public void defaultColor(
			int red,
			int green,
			int blue,
			int alpha
	) {

	}

	@Override
	public void unsetDefaultColor() {

	}
}
