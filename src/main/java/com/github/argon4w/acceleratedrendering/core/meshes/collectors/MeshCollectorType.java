package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;

public enum MeshCollectorType {

	CULLED,
	SIMPLE;

	public IMeshCollector create(IAcceleratedVertexConsumer consumer) {
		return create(this, consumer);
	}

	public static IMeshCollector create(MeshCollectorType collectorType, IAcceleratedVertexConsumer consumer) {
		return switch (collectorType) {
			case CULLED -> new CulledMeshCollector(consumer);
			case SIMPLE -> new SimpleMeshCollector(consumer.getLayout());
		};
	}
}
