package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ItemDecoratorHandler;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@SuppressWarnings("UnstableApiUsage")
public record DecoratorDrawContext(
		Matrix4f				transform,
		Matrix3f				normal,
		ItemDecoratorHandler	handler,
		Font					font,
		ItemStack				stack,
		int						xOffset,
		int						yOffset
) implements IGuiElementContext {

	@Override
	public float depth() {
		return 100.0f;
	}
}
