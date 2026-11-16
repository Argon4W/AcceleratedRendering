package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;

public record TooltipDrawContext(
		Matrix4f						transform,
		Matrix3f						normal,
		Font							font,
		List<ClientTooltipComponent>	components,
		int								mouseX,
		int								mouseY,
		ClientTooltipPositioner			tooltipPositioner
) implements IGuiElementContext {

	@Override
	public float depth() {
		return 400;
	}

	@Override
	public float thickness() {
		return GuiBatchingController.DELTA;
	}
}
