package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;

public class MeshDataCaches {

	public static final IMeshDataCache SERVER = CoreFeature.createMeshDataCache();
	public static final IMeshDataCache CLIENT = CoreFeature.createMeshDataCache();
}
