package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;

public interface IMeshDataCache {

	void	set		(VertexLayout layout, MeshData data, IMesh mesh);
	IMesh	get		(VertexLayout layout, MeshData data);
	int		count	(VertexLayout layout, MeshData data);
}
