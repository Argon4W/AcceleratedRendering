package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import it.unimi.dsi.fastutil.objects.*;

public class SimpleMeshDataCache implements IMeshDataCache {

	private final Object2ReferenceMap<VertexLayout, Object2ReferenceMap	<MeshData, IMesh>>	meshes;
	private final Object2ReferenceMap<VertexLayout, Object2IntMap		<MeshData>>			counts;

	public SimpleMeshDataCache() {
		this.meshes = new Object2ReferenceOpenHashMap<>();
		this.counts = new Object2ReferenceOpenHashMap<>();

		this.meshes.defaultReturnValue(Object2ReferenceMaps	.emptyMap());
		this.counts.defaultReturnValue(Object2IntMaps		.emptyMap());
	}

	@Override
	public void set(
			VertexLayout	layout,
			MeshData		data,
			IMesh			mesh
	) {
		var meshCache	= meshes.getOrDefault(layout, null);
		var countCache	= counts.getOrDefault(layout, null);

		if (meshCache == null) {
			meshCache	= new Object2ReferenceOpenHashMap	<>();
			countCache	= new Object2IntOpenHashMap			<>();

			meshes.put(layout, meshCache);
			counts.put(layout, countCache);
		}

		meshCache	.putIfAbsent(data, mesh);
		countCache	.put		(data, countCache.getInt(data) + 1);
	}

	@Override
	public IMesh get(VertexLayout layout, MeshData meshData) {
		return meshes.get(layout).get(meshData);
	}

	@Override
	public int count(VertexLayout layout, MeshData data) {
		return counts.get(layout).getInt(data);
	}
}
