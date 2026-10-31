package com.github.argon4w.acceleratedrendering.core.backends.states;

import net.minecraft.client.gui.GuiGraphics;

import static org.lwjgl.opengl.GL46.*;

public class FramebufferBindingState implements IBindingState {

	private int bindingFramebuffer;
	private int bindingReadFramebuffer;
	private int bindingDrawFramebuffer;

	public FramebufferBindingState() {
		this.bindingFramebuffer		= 0;
		this.bindingReadFramebuffer	= 0;
		this.bindingDrawFramebuffer	= 0;
	}

	@Override
	public void capture(GuiGraphics graphics) {
		bindingFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);

		if (bindingFramebuffer == 0) {
			bindingReadFramebuffer = glGetInteger(GL_READ_FRAMEBUFFER_BINDING);
			bindingDrawFramebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
		}
	}

	@Override
	public void restore() {
		if (bindingFramebuffer != 0) {
			glBindFramebuffer(GL_FRAMEBUFFER, bindingFramebuffer);
		} else {
			glBindFramebuffer(GL_READ_FRAMEBUFFER, bindingReadFramebuffer);
			glBindFramebuffer(GL_DRAW_FRAMEBUFFER, bindingDrawFramebuffer);
		}
	}

	@Override
	public void delete() {

	}
}
