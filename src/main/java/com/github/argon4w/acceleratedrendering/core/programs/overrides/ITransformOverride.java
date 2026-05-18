package com.github.argon4w.acceleratedrendering.core.programs.overrides;

public non-sealed interface ITransformOverride extends IProgramOverride {

	void	uploadVarying		(long	varyingAddress,	int offset);
	int		dispatchTransform	(int	vertexCount,	int vertexOffset, int varyingOffset);
	long	getVaryingSize		();
}
