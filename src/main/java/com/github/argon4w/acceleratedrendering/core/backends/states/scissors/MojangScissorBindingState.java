package com.github.argon4w.acceleratedrendering.core.backends.states.scissors;

import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import static org.lwjgl.opengl.GL46.*;

public class MojangScissorBindingState implements IBindingState {

	private boolean recorded;
	private boolean	enabled;
	private int		scissorX;
	private int		scissorY;
	private int		scissorWidth;
	private int		scissorHeight;

	public MojangScissorBindingState() {
		this.enabled		= false;
		this.scissorX		= 0;
		this.scissorY		= 0;
		this.scissorWidth	= 0;
		this.scissorHeight	= 0;
	}

	@Override
	public void capture(GuiGraphics graphics) {
		var rect = graphics.scissorStack.stack.peekLast();

		if (rect != null) {
			var window	= Minecraft.getInstance()	.getWindow	();
			var height	= window					.getHeight	();
			var scale	= window					.getGuiScale();

			scissorY		= (int) (height - rect.bottom()	* scale);
			scissorX		= (int) (rect.left	()			* scale);
			scissorWidth	= (int) (rect.width	()			* scale);
			scissorHeight	= (int) (rect.height()			* scale);
			enabled			= true;
		} else {
			enabled = false;
		}

		recorded = true;
	}

	@Override
	public void restore() {
		if (!recorded) {
			return;
		}

		if (enabled) {
			glEnable	(GL_SCISSOR_TEST);
			glScissor	(
					scissorX,
					scissorY,
					scissorWidth,
					scissorHeight
			);
		} else {
			glDisable(GL_SCISSOR_TEST);
		}

		recorded = false;
	}

	@Override
	public void delete() {
		enabled		= false;
		recorded	= false;
	}
}
