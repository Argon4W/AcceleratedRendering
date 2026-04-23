package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.core.utils.CullerUtils;
import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class CulledMeshCollector implements VertexConsumer, IMeshCollector {

	private final	int					polygonSize;
	private final	NativeImage			texture;
	private	final	VertexLayout		layout;
	private final	SimpleMeshCollector	meshCollector;
	private final	Vertex[]			polygon;

	private			int					vertexIndex;

	public CulledMeshCollector(IAcceleratedVertexConsumer vertexConsumer) {
		this.polygonSize	= vertexConsumer	.getPolygonSize	();
		this.texture		= vertexConsumer	.downloadTexture();
		this.layout			= vertexConsumer	.getLayout		();
		this.meshCollector	= new SimpleMeshCollector			(this.layout);
		this.polygon		= new Vertex						[this.polygonSize];

		this.vertexIndex	= -1;
	}

	@Override
	public void flush() {
		if (vertexIndex >= polygonSize - 1) {
			vertexIndex = -1;

			if (!CullerUtils.shouldCull(polygon, texture)) {
				for (var vertex : polygon) {
					var vertexPosition	= vertex.getPosition();
					var vertexColor		= vertex.getColor	();
					var vertexUV		= vertex.getUv		();
					var vertexNormal	= vertex.getNormal	();

					meshCollector.vertex(
							vertexPosition	.x,
							vertexPosition	.y,
							vertexPosition	.z,
							vertexColor		.x / 255.0f,
							vertexColor		.y / 255.0f,
							vertexColor		.z / 255.0f,
							vertexColor		.w / 255.0f,
							vertexUV		.x,
							vertexUV		.y,
							OverlayTexture	.NO_OVERLAY,
							vertex			.getPackedLight(),
							vertexNormal	.x,
							vertexNormal	.y,
							vertexNormal	.z
					);
				}
			}
		}
	}

	@Override
	public void endVertex() {
		flush();
	}

	@Override
	public void unsetDefaultColor() {
		meshCollector.unsetDefaultColor();
	}

	@Override
	public void defaultColor(
			int defaultR,
			int defaultG,
			int defaultB,
			int defaultA
	) {
		meshCollector.defaultColor(
				defaultR,
				defaultG,
				defaultB,
				defaultA
		);
	}

	@Override
	public VertexConsumer vertex(
			double pX,
			double pY,
			double pZ
	) {
		flush();
		polygon[++ vertexIndex]					= new Vertex();
		polygon[vertexIndex].getPosition().x	= (float) pX;
		polygon[vertexIndex].getPosition().y	= (float) pY;
		polygon[vertexIndex].getPosition().z	= (float) pZ;

		return this;
	}

	@Override
	public VertexConsumer color(
			int pRed,
			int pGreen,
			int pBlue,
			int pAlpha
	) {
		if (vertexIndex < 0) {
			throw new IllegalStateException("Vertex not building!");
		}

		polygon[vertexIndex].getColor().x = pRed;
		polygon[vertexIndex].getColor().y = pGreen;
		polygon[vertexIndex].getColor().z = pBlue;
		polygon[vertexIndex].getColor().w = pAlpha;

		return this;
	}

	@Override
	public VertexConsumer uv(float pU, float pV) {
		if (vertexIndex < 0) {
			throw new IllegalStateException("Vertex not building!");
		}

		polygon[vertexIndex].getUv().x = pU;
		polygon[vertexIndex].getUv().y = pV;

		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int pU, int pV) {
		return this;
	}

	@Override
	public VertexConsumer uv2(int pU, int pV) {
		if (vertexIndex < 0) {
			throw new IllegalStateException("Vertex not building!");
		}

		polygon[vertexIndex].getLight().x = pU;
		polygon[vertexIndex].getLight().y = pV;

		return this;
	}

	@Override
	public VertexConsumer normal(
			float pNormalX,
			float pNormalY,
			float pNormalZ
	) {
		if (vertexIndex < 0) {
			throw new IllegalStateException("Vertex not building!");
		}

		polygon[vertexIndex].getNormal().x = pNormalX;
		polygon[vertexIndex].getNormal().y = pNormalY;
		polygon[vertexIndex].getNormal().z = pNormalZ;
		return this;
	}

	@Override
	public MeshData getData() {
		return meshCollector.getData();
	}

	@Override
	public ByteBufferBuilder getBuffer() {
		return meshCollector.getBuffer();
	}

	@Override
	public VertexLayout getLayout() {
		return meshCollector.getLayout();
	}

	@Override
	public int getVertexCount() {
		return meshCollector.getVertexCount();
	}
}
