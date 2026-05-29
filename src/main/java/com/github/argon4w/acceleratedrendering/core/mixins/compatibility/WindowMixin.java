package com.github.argon4w.acceleratedrendering.core.mixins.compatibility;

import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Window.class)
public class WindowMixin {

	/* @ModifyConstant(
			method		= "<init>",
			constant	= @Constant(
					intValue	= 3,
					ordinal		= 0
			)
	)
	public int modifyGlMajorVersion(int value) {
		return 4;
	}

	@ModifyConstant(
			method		= "<init>",
			constant	= @Constant(
					intValue	= 2,
					ordinal		= 0
			)
	)
	public int modifyGlMinorVersion(int value) {
		return 6;
	} */
}
