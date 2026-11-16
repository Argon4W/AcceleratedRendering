package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;

public class IgnoreMeshDataCache implements IMeshDataCache {

	@Override
	public void set(
			boolean			reloadAware,
			VertexLayout	layout,
			MeshData		data,
			IMesh			mesh
	) {

	}

	@Override
	public IMesh get(
			boolean			reloadAware,
			VertexLayout	layout,
			MeshData		data
	) {
		return null;
	}

	@Override
	public void reload() {

	}
}
