package com.github.argon4w.acceleratedrendering.core.backends.states.scissors;

import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL46.*;

public class OpenGLScissorBindingState implements IBindingState {

	private final	IntBuffer	bindingScissor;

	private			boolean		enabled;
	private			boolean		recorded;

	public OpenGLScissorBindingState() {
		this.bindingScissor	= MemoryUtil.memAllocInt(4);
		this.enabled		= false;
		this.recorded		= false;
	}

	@Override
	public void capture(GuiGraphics graphics) {
		glGetIntegerv				(GL_SCISSOR_BOX, bindingScissor);
		enabled		= glIsEnabled	(GL_SCISSOR_TEST);
		recorded	= true;
	}

	@Override
	public void restore() {
		if (!recorded) {
			return;
		}

		if (enabled) {
			glEnable	(GL_SCISSOR_TEST);
			glScissor	(
					bindingScissor.get(0),
					bindingScissor.get(1),
					bindingScissor.get(2),
					bindingScissor.get(3)
			);
		} else {
			glDisable(GL_SCISSOR_TEST);
		}

		recorded = false;
	}

	@Override
	public void delete() {
		MemoryUtil.memFree(bindingScissor);
		recorded = false;
	}
}
