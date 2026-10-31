package com.github.argon4w.acceleratedrendering.core.backends.states.viewports;

import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;

public class OpenGLViewportBindingState implements IBindingState {

	private final IntBuffer bindingViewport;

	public OpenGLViewportBindingState() {
		this.bindingViewport = MemoryUtil.memAllocInt(4);
	}

	@Override
	public void capture(GuiGraphics graphics) {
		glGetIntegerv(GL_VIEWPORT, bindingViewport);
	}

	@Override
	public void restore() {
		glViewport(
				bindingViewport.get(0),
				bindingViewport.get(1),
				bindingViewport.get(2),
				bindingViewport.get(3)
		);
	}

	@Override
	public void delete() {
		MemoryUtil.memFree(bindingViewport);
	}
}
