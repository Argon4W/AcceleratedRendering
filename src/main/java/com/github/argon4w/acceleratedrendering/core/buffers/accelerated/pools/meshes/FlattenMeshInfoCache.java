package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes;

import com.github.argon4w.acceleratedrendering.core.utils.FastColorUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Arrays;

public class FlattenMeshInfoCache implements IMeshInfoCache {

	public static final VarHandle	HANDLE				= MethodHandles.arrayElementVarHandle(int[].class).withInvokeExactBehavior();
	public static final int			MESH_INFO_SIZE		= 5;
	public static final int			COLOR_OFFSET		= 0;
	public static final int			LIGHT_OFFSET		= 1;
	public static final int			OVERLAY_OFFSET		= 2;
	public static final int			SHARING_OFFSET		= 3;
	public static final int			SHOULD_CULL_OFFSET	= 4;

	private				int[]		cache;
	private				int			size;
	private				int			count;

	public FlattenMeshInfoCache() {
		this.size	= 128;
		this.cache	= new int[this.size * MESH_INFO_SIZE];
		this.count	= 0;
	}

	@Override
	public void reset() {
		count = 0;
	}

	@Override
	public void delete() {

	}

	@Override
	public void setup(
			int color,
			int light,
			int overlay,
			int sharing,
			int shouldCull
	) {
		if (count >= size) {
			size	= size * 2;
			cache	= Arrays.copyOf(cache, size * MESH_INFO_SIZE);
		}

		var infoIndex = count * MESH_INFO_SIZE;

		HANDLE.set(cache, infoIndex + COLOR_OFFSET,			color);
		HANDLE.set(cache, infoIndex + LIGHT_OFFSET,			light);
		HANDLE.set(cache, infoIndex + OVERLAY_OFFSET,		overlay);
		HANDLE.set(cache, infoIndex + SHARING_OFFSET,		sharing);
		HANDLE.set(cache, infoIndex + SHOULD_CULL_OFFSET,	shouldCull);

		count ++;
	}

	@Override
	public int getMeshCount() {
		return count;
	}

	@Override
	public int getSharing(int i) {
		return (int) HANDLE.get(cache, i * MESH_INFO_SIZE + SHARING_OFFSET);
	}

	@Override
	public int getShouldCull(int i) {
		return (int) HANDLE.get(cache, i * MESH_INFO_SIZE + SHOULD_CULL_OFFSET);
	}

	@Override
	public int getColor(int i) {
		return FastColorUtils.convert((int) HANDLE.get(cache, i * MESH_INFO_SIZE + COLOR_OFFSET));
	}

	@Override
	public int getLight(int i) {
		return (int) HANDLE.get(cache, i * MESH_INFO_SIZE + LIGHT_OFFSET);
	}

	@Override
	public int getOverlay(int i) {
		return (int) HANDLE.get(cache, i * MESH_INFO_SIZE + OVERLAY_OFFSET);
	}
}
