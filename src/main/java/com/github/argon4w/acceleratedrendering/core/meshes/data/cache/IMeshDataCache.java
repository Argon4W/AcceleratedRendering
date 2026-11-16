package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;

public interface IMeshDataCache {

	void	reload	();
	void	set		(boolean reloadAware, VertexLayout layout, MeshData data, IMesh mesh);
	IMesh	get		(boolean reloadAware, VertexLayout layout, MeshData data);
}
