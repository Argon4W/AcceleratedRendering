package com.github.argon4w.acceleratedrendering.features.text.extensions;

import com.github.argon4w.acceleratedrendering.features.text.IAcceleratedStringRenderOutput;
import net.minecraft.client.gui.Font;

public class StringRenderOutputExtension {

	public static IAcceleratedStringRenderOutput getAccelerated(Font.StringRenderOutput in) {
		return (IAcceleratedStringRenderOutput) in;
	}
}
