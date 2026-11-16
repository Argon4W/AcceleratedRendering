package com.github.argon4w.acceleratedrendering.core.backends.states.viewports;

import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.GuiGraphics;

public class MojangViewportBindingState implements IBindingState {

	private int viewportX;
	private int viewportY;
	private int viewportWidth;
	private int viewportHeight;

	public MojangViewportBindingState() {
		this.viewportX		= 0;
		this.viewportY		= 0;
		this.viewportWidth	= 0;
		this.viewportHeight	= 0;
	}

	@Override
	public void capture(GuiGraphics graphics) {
		viewportX		= GlStateManager.Viewport.x		();
		viewportY		= GlStateManager.Viewport.y		();
		viewportWidth	= GlStateManager.Viewport.width	();
		viewportHeight	= GlStateManager.Viewport.height();
	}

	@Override
	public void restore() {
		if (		viewportX		!= GlStateManager.Viewport.x		()
				||	viewportY		!= GlStateManager.Viewport.y		()
				||	viewportWidth	!= GlStateManager.Viewport.width	()
				||	viewportHeight	!= GlStateManager.Viewport.height	()
		) {
			GlStateManager._viewport(
					viewportX,
					viewportY,
					viewportWidth,
					viewportHeight
			);
		}
	}

	@Override
	public void delete() {

	}
}
