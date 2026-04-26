package com.github.argon4w.acceleratedrendering.core.utils;

import lombok.Getter;

@Getter
public class MutableSize {

	protected boolean	resized;
	protected long		size;

	public MutableSize(long initialSize) {
		this.resized	= false;
		this.size		= initialSize;
	}

	public void expand(long bytes) {
		if (bytes <= 0) {
			return;
		}

		beforeExpand();

		onExpand(bytes);
		doExpand(size, bytes);

		resized	=	true;
		size	+=	bytes;

		afterExpand();
	}

	public void onExpand(long bytes) {

	}

	public void doExpand(long size, long bytes) {

	}

	public void beforeExpand() {

	}

	public void afterExpand() {

	}

	public void resize(long atLeast) {
		resizeTo(Long.highestOneBit(atLeast) << 1);
	}

	public void resizeTo(long newBufferSize) {
		expand(newBufferSize - size);
	}

	public void mark() {
		resized = false;
	}
}
