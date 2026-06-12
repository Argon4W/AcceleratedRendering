package com.github.argon4w.acceleratedrendering.core.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

public class FastUtils {

	public static boolean equals(IntArrayList a, IntArrayList b) {
		return Arrays.equals(
				a.elements(), 0, a.size(),
				b.elements(), 0, b.size()
		);
	}

	public static int hashCode(IntArrayList list) {
		var array	= list.elements();
		var result	= 0;

		for (int index = 0, size = list.size(); index < size; index ++) {
			result = 31 * result + array[index];
		}

		return result;
	}
}
