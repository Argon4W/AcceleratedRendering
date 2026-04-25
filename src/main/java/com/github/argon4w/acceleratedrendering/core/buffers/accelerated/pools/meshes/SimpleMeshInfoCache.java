package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes;

import com.github.argon4w.acceleratedrendering.core.utils.FastColorUtils;
import com.github.argon4w.acceleratedrendering.core.utils.SimpleCachedArray;

import java.util.function.IntFunction;

public class SimpleMeshInfoCache implements IMeshInfoCache, IntFunction<SimpleMeshInfo> {

	private final SimpleCachedArray<SimpleMeshInfo> meshInfos;

	public SimpleMeshInfoCache() {
		this.meshInfos = new SimpleCachedArray<>(128, this);
	}

	@Override
	public void setup(
			int color,
			int light,
			int overlay,
			int sharing,
			int shouldCull
	) {
		meshInfos.get().setupMeshInfo(
				color,
				light,
				overlay,
				sharing,
				shouldCull
		);
	}

	@Override
	public void reset() {
		meshInfos.reset();
	}

	@Override
	public void delete() {
		meshInfos.delete();
	}

	@Override
	public int getMeshCount() {
		return meshInfos.getCursor();
	}

	@Override
	public int getSharing(int i) {
		return meshInfos.at(i).getSharing();
	}

	@Override
	public int getShouldCull(int i) {
		return meshInfos.at(i).getShouldCull();
	}

	@Override
	public int getColor(int i) {
		return FastColorUtils.convert(meshInfos.at(i).getColor());
	}

	@Override
	public int getLight(int i) {
		return meshInfos.at(i).getLight();
	}

	@Override
	public int getOverlay(int i) {
		return meshInfos.at(i).getOverlay();
	}

	@Override
	public SimpleMeshInfo apply(int value) {
		return new SimpleMeshInfo();
	}
}
