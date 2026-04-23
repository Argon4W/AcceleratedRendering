package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;

public interface IMeshCollector {

	MeshData			getData			();
	ByteBufferBuilder	getBuffer		();
	VertexLayout		getLayout		();
	int					getVertexCount	();
	void				flush			();
}
