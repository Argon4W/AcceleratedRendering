package com.github.argon4w.acceleratedrendering.core.programs.overrides;

import net.minecraft.client.renderer.RenderType;

import java.util.Map;

public interface IShaderProgramOverrides {

	ProgramOverride	getOverride	(RenderType	renderType);
	ProgramOverride	getOverride	(int		overrideId);
	int				getCount	();
}
