package com.github.argon4w.acceleratedrendering.features.items.gui.contexts;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public record ItemRenderContext(
		Matrix4f			transform,
		Matrix3f			normal,
		ItemStack			itemStack,
		ItemDisplayContext	displayContext,
		boolean				leftHand,
		int					combinedLight,
		int					combinedOverlay,
		BakedModel			bakedModel
) implements IGuiElementContext {

	@Override
	public float depth() {
		return 0.0f;
	}
}
