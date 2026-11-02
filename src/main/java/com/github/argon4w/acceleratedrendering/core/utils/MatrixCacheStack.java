package com.github.argon4w.acceleratedrendering.core.utils;

import lombok.Getter;
import org.joml.Matrix4f;

import java.util.Arrays;

public class MatrixCacheStack {

	private static Matrix4f	[]	MATRICES_DATA;
	private static int		[]	FRAMES_DATA;
	private static int			MATRICES_SIZE;
	private static int			FRAMES_SIZE;
	private static int			MATRICES_POINTER;
	private static int			FRAME_INDEX;

	static {
		MATRICES_SIZE		= 16;
		FRAMES_SIZE			= 4;
		MATRICES_POINTER	= 0;
		FRAME_INDEX			= -1;

		MATRICES_DATA	= new Matrix4f	[MATRICES_SIZE];
		FRAMES_DATA		= new int		[FRAMES_SIZE];

		for (var i = 0; i < MATRICES_SIZE; i++) {
			MATRICES_DATA[i] = new Matrix4f();
		}
	}

	public static MatrixArrayView acquire(int count) {
		var result = new MatrixArrayView(MATRICES_POINTER, count);

		FRAME_INDEX ++;

		if (FRAMES_SIZE <= FRAME_INDEX) {
			FRAMES_SIZE = FRAME_INDEX * 2;
			FRAMES_DATA = Arrays.copyOf(FRAMES_DATA, FRAMES_SIZE);
		}

		if (MATRICES_SIZE < MATRICES_POINTER + count) {
			var oldSize = MATRICES_SIZE;
			var newSize = MATRICES_POINTER + count * 2;

			MATRICES_SIZE = newSize;
			MATRICES_DATA = Arrays.copyOf(MATRICES_DATA, newSize);

			for (var i = oldSize; i < newSize; i ++) {
				MATRICES_DATA[i] = new Matrix4f();
			}
		}

		FRAMES_DATA[FRAME_INDEX] = MATRICES_POINTER;

		MATRICES_POINTER += count;

		return result;
	}

	public static void release() {
		if (FRAME_INDEX < 0) {
			throw new IllegalStateException("No MatrixArrayView is currently in use.");
		}

		MATRICES_POINTER = FRAMES_DATA[FRAME_INDEX --];
	}

	@Getter
	public static class MatrixArrayView implements AutoCloseable {

		private final int offset;
		private final int length;

		private MatrixArrayView(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}

		public Matrix4f get(int index) {
			return MATRICES_DATA[offset + index];
		}

		@Override
		public void close() {
			release();
		}
	}
}
