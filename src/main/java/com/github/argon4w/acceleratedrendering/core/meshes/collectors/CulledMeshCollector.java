package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.utils.CullerUtils;
import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import com.mojang.blaze3d.platform.NativeImage;

public class CulledMeshCollector extends PolygonMeshCollector {

	private final NativeImage texture;

	public CulledMeshCollector(IAcceleratedVertexConsumer vertexConsumer) {
		super(vertexConsumer);

		this.texture = vertexConsumer.downloadTexture();
	}

	@Override
	protected void flushPolygon(IMeshCollector collector, Vertex[] polygon) {
		if (!CullerUtils.shouldCull(polygon, texture)) {
			for (int index = 0, size = polygon.length; index < size; index ++) {
				var vertex = polygon[index];

				var packedOverlay	= vertex.getPackedOverlay	();
				var packedLight		= vertex.getPackedLight		();
				var packedColor		= vertex.getPackedColor		();
				var position		= vertex.getPosition		();
				var texCoord		= vertex.getTexCoord		();
				var normal			= vertex.getNormal			();

				collector.addVertex(
						position.x(),
						position.y(),
						position.z(),
						packedColor,
						texCoord.x(),
						texCoord.y(),
						packedOverlay,
						packedLight,
						normal.x(),
						normal.y(),
						normal.z()
				);
			}
		}
	}
}
