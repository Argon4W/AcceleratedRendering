package com.github.argon4w.acceleratedrendering.core.programs.overrides;

public sealed interface IProgramOverride permits IUploadingOverride, ITransformOverride {

	void useProgram		();
	void setupProgram	();
}
