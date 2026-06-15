package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.core.meshes.ClientMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.data.cache.MeshDataCaches;
import com.github.argon4w.acceleratedrendering.core.utils.TextureUtils;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class CoreReloads implements ResourceManagerReloadListener {

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		TextureUtils					.reload();
		ServerMesh.Builder	.INSTANCE	.reload();
		ClientMesh.Builder	.INSTANCE	.reload();
		MeshDataCaches		.SERVER		.reload();
		MeshDataCaches		.CLIENT		.reload();
	}
}
