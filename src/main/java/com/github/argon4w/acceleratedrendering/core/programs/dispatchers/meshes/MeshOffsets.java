package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import lombok.Getter;

@Getter
public class MeshOffsets {

	public static final int OFFSETS_SIZE	= 2;
	public static final int VERTEX_OFFSET	= 0;
	public static final int VARYING_OFFSET	= 1;

	private final long[]	cache;
	private final int		count;

	public MeshOffsets(int count) {
		this.cache = new long[count * 2];
		this.count = count;
	}

	public void setupOffsets(AcceleratedBufferBuilder builder) {
		var index = builder.getIndex		();
		var count = builder.getVertexCount	();

		cache[index * OFFSETS_SIZE + VERTEX_OFFSET]		= builder.getVertexCountOffset	() + count;
		cache[index * OFFSETS_SIZE + VARYING_OFFSET]	= builder.getVaryingCountOffset	() + count;
	}

	public OffsetPair reserve(MeshUploader uploader) {
		var index = uploader.getIndex		();
		var count = uploader.getVertexCount	();

		var pair = new OffsetPair(
				cache[index * OFFSETS_SIZE + VERTEX_OFFSET],
				cache[index * OFFSETS_SIZE + VARYING_OFFSET]
		);

		cache[index * OFFSETS_SIZE + VERTEX_OFFSET]		+= count;
		cache[index * OFFSETS_SIZE + VARYING_OFFSET]	+= count;

		return pair;
	}

	public OffsetPair reserve(AcceleratedBufferBuilder builder, int count) {
		var index = builder.getIndex();

		var pair = new OffsetPair(
				cache[index * OFFSETS_SIZE + VERTEX_OFFSET],
				cache[index * OFFSETS_SIZE + VARYING_OFFSET]
		);

		cache[index * OFFSETS_SIZE + VERTEX_OFFSET]		+= count;
		cache[index * OFFSETS_SIZE + VARYING_OFFSET]	+= count;

		return pair;
	}

	public void clear() {
		for (var i = 0; i < count; i ++) {
			cache[i * OFFSETS_SIZE + VERTEX_OFFSET]		= 0L;
			cache[i * OFFSETS_SIZE + VARYING_OFFSET]	= 0L;
		}
	}

	public record OffsetPair(
			long vertexOffset,
			long varyingOffset
	) {

	}
}
