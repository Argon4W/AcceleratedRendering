package com.github.argon4w.acceleratedrendering.core.meshes.data.cache;

public enum MeshDataCacheType {

	IGNORED,
	MERGED;

	public IMeshDataCache create() {
		return create(this);
	}

	public static IMeshDataCache create(MeshDataCacheType type) {
		return switch (type) {
			case IGNORED	-> new IgnoreMeshDataCache();
			case MERGED		-> new SimpleMeshDataCache();
		};
	}
}
