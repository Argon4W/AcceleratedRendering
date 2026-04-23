package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;

public class IgnoreMeshDataCache implements IMeshDataCache {

	@Override
	public void set(
			VertexLayout	layout,
			MeshData		data,
			IMesh			mesh
	) {

	}

	@Override
	public IMesh get(VertexLayout layout, MeshData meshData) {
		return null;
	}

	@Override
	public int count(VertexLayout layout, MeshData data) {
		return 0;
	}
}
