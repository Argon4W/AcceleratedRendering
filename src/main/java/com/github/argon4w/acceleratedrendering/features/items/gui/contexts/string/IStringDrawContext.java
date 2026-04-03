package com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string;

import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.IGuiElementContext;
import net.minecraft.client.renderer.MultiBufferSource;

public interface IStringDrawContext extends IGuiElementContext {

	void drawString(MultiBufferSource buffer);
}
