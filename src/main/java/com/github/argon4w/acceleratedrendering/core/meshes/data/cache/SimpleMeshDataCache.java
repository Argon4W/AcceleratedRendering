package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import it.unimi.dsi.fastutil.objects.*;

public class SimpleMeshDataCache implements IMeshDataCache {

	private final Object2ReferenceMap<VertexLayout, VertexLayoutCache> persistentVertexLayoutCaches;
	private final Object2ReferenceMap<VertexLayout, VertexLayoutCache> reloadAwareVertexLayoutCaches;

	public SimpleMeshDataCache() {
		this.persistentVertexLayoutCaches	= new Object2ReferenceOpenHashMap<>();
		this.reloadAwareVertexLayoutCaches	= new Object2ReferenceOpenHashMap<>();
	}

	@Override
	public void set(
			boolean			reloadAware,
			VertexLayout	layout,
			MeshData		data,
			IMesh			mesh
	) {
		var vertexLayoutCaches = reloadAware
				? this.reloadAwareVertexLayoutCaches
				: this.persistentVertexLayoutCaches;

		var vertexLayoutCache = vertexLayoutCaches.get(layout);

		if (vertexLayoutCache == null) {
			vertexLayoutCache = new VertexLayoutCache(layout, reloadAware);
		}

		vertexLayoutCache.meshes.put(data, mesh);
	}

	@Override
	public IMesh get(
			boolean 		reloadAware,
			VertexLayout 	layout,
			MeshData 		data
	) {
		var cache = (reloadAware
				? this.reloadAwareVertexLayoutCaches
				: this.persistentVertexLayoutCaches).get(layout);

		return cache == null ? null : cache.meshes.get(data);
	}

	@Override
	public void reload() {
		reloadAwareVertexLayoutCaches.clear();
	}

	public class VertexLayoutCache {

		private final Object2ReferenceMap<MeshData, IMesh> meshes;

		private VertexLayoutCache(VertexLayout vertexLayout, boolean reloadAware) {
			this.meshes = new Object2ReferenceOpenHashMap<>();

			(reloadAware
					? SimpleMeshDataCache.this.reloadAwareVertexLayoutCaches
					: SimpleMeshDataCache.this.persistentVertexLayoutCaches).put(vertexLayout, this);
		}
	}
}
