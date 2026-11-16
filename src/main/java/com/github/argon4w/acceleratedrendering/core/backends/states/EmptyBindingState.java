package com.github.argon4w.acceleratedrendering.core.backends.states;

import net.minecraft.client.gui.GuiGraphics;

public class EmptyBindingState implements IBindingState {

	public static final IBindingState INSTANCE = new EmptyBindingState();

	@Override
	public void capture(GuiGraphics graphics) {

	}

	@Override
	public void restore() {

	}

	@Override
	public void delete() {

	}
}
