package com.github.argon4w.acceleratedrendering.core.meshes.collectors;

import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface IMeshCollector extends VertexConsumer {

	MeshData			getData			();
	ByteBufferBuilder	getBuffer		();
	VertexLayout		getLayout		();
	int					getVertexCount	();
	void				flush			();
}
