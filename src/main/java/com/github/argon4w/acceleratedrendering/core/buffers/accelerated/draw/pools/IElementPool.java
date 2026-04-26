package com.github.argon4w.acceleratedrendering.core.buffers.accelerated.draw.pools;

import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;

public interface IElementPool {

	void			reset		();
	void			delete		();
	void			prepare		();
	void			bindBuffer	();
	boolean			isResized	();
	IServerBuffer	getBuffer	();
	IElementSegment get			();

	interface IElementSegment {

		long	getCount	();
		void	setup		();
		void	count		(int count);
	}
}
