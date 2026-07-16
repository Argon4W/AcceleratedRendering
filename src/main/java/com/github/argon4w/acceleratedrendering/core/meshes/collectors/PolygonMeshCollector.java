package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;

public abstract class PolygonMeshCollector implements IMeshCollector {

	private final	SimpleMeshCollector	meshCollector;
	private final	Vertex[]			polygon;
	private			int					vertexIndex;

	public PolygonMeshCollector(IAcceleratedVertexConsumer vertexConsumer) {
		this.meshCollector	= new SimpleMeshCollector	(vertexConsumer.getLayout		());
		this.polygon		= new Vertex				[vertexConsumer.getPolygonSize	()];
		this.vertexIndex	= -1;
	}

	protected abstract void flushPolygon(IMeshCollector collector, Vertex[] polygon);

	@Override
	public void flush() {
		if (vertexIndex >= polygon.length - 1) {
			vertexIndex = -1;
			flushPolygon(meshCollector, polygon);
		}
	}

	@Override
	public VertexConsumer addVertex(
			float pX,
			float pY,
			float pZ
	) {
		flush();
		polygon[++	vertexIndex]					= new Vertex();
		polygon[	vertexIndex].getPosition().x	= pX;
		polygon[	vertexIndex].getPosition().y	= pY;
		polygon[	vertexIndex].getPosition().z	= pZ;

		return this;
	}

	@Override
	public VertexConsumer setColor(
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
	public VertexConsumer setUv(float pU, float pV) {
		if (vertexIndex < 0) {
			throw new IllegalStateException("Vertex not building!");
		}

		polygon[vertexIndex].getTexCoord().x = pU;
		polygon[vertexIndex].getTexCoord().y = pV;

		return this;
	}

	@Override
	public VertexConsumer setUv1(int pU, int pV) {
		return this;
	}

	@Override
	public VertexConsumer setUv2(int pU, int pV) {
		if (vertexIndex < 0) {
			throw new IllegalStateException("Vertex not building!");
		}

		polygon[vertexIndex].getLight().x = pU;
		polygon[vertexIndex].getLight().y = pV;

		return this;
	}

	@Override
	public VertexConsumer setNormal(
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
	public long getVertexCount() {
		return meshCollector.getVertexCount();
	}
}
