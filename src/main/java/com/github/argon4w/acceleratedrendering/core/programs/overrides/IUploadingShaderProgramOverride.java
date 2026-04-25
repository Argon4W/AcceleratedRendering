package com.github.argon4w.acceleratedrendering.core.programs.overrides;

public interface IUploadingShaderProgramOverride extends IShaderProgramOverride {

	void	uploadMeshInfo		(long	meshInfoAddress,	int	meshInfoIndex);
	int		dispatchUploading	(int	meshCount,			int	meshSize, long meshOffset);
	long	getMeshInfoSize		();
}
