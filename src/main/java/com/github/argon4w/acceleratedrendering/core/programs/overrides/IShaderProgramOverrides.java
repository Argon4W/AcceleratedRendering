package com.github.argon4w.acceleratedrendering.core.programs.overrides;

import net.minecraft.client.renderer.RenderType;

public interface IShaderProgramOverrides {

	ProgramOverride	getOverride	(RenderType	renderType);
	ProgramOverride	getOverride	(int		overrideId);
	int				getCount	();
}
