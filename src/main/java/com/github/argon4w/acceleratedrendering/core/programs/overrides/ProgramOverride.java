package com.github.argon4w.acceleratedrendering.core.programs.overrides;

public record ProgramOverride(
		int					overrideId,
		ITransformOverride	transform,
		IUploadingOverride	uploading
) {

	public void uploadVarying(long varyingAddress, int offset) {
		transform.uploadVarying(varyingAddress, offset);
	}

	public void uploadMeshInfo(long meshInfoAddress, int meshInfoIndex) {
		uploading.uploadMeshInfo(meshInfoAddress, meshInfoIndex);
	}

	public long getVaryingSize() {
		return transform.getVaryingSize();
	}

	public long getMeshInfoSize() {
		return uploading.getMeshInfoSize();
	}
}
