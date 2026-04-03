package com.github.argon4w.acceleratedrendering.features.items.mixins.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ItemDecoratorHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

	@Shadow @Final private PoseStack pose;

	@WrapMethod(method	= "fill(IIIII)V")
	public void renderDecorationRectanglesFast(
			int				minX,
			int				minY,
			int				maxX,
			int				maxY,
			int				color,
			Operation<Void>	original
	) {
		if (!CoreFeature.isGuiBatching()) {
			original.call(
					minX,
					minY,
					maxX,
					maxY,
					color
			);
			return;
		}

		var last = pose.last();

		GuiBatchingController.INSTANCE.recordRectangle(
				last		.pose	(),
				last		.normal	(),
				RenderType	.gui	(),
				minX,
				minY,
				maxX,
				maxY,
				-1,
				color
		);
	}

	@WrapMethod(method	= "fill(Lnet/minecraft/client/renderer/RenderType;IIIIII)V")
	public void renderDecorationRectanglesFast(
			RenderType		renderType,
			int				minX,
			int				minY,
			int				maxX,
			int				maxY,
			int				blitOffset,
			int				color,
			Operation<Void>	original
	) {
		if (!CoreFeature.isGuiBatching()) {
			original.call(
					renderType,
					minX,
					minY,
					maxX,
					maxY,
					blitOffset,
					color
			);
			return;
		}

		var last = pose.last();

		GuiBatchingController.INSTANCE.recordRectangle(
				last.pose	(),
				last.normal	(),
				renderType,
				minX,
				minY,
				maxX,
				maxY,
				blitOffset,
				color
		);
	}

	@WrapMethod(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFF)V")
	public void renderBlitFast(
			ResourceLocation atlasLocation,
			int					minX,
			int					maxX,
			int					minY,
			int					maxY,
			int					blitOffset,
			float				minU,
			float				maxU,
			float				minV,
			float				maxV,
			Operation<Void>		original
	) {
		if (!CoreFeature.isGuiBatching()) {
			original.call(
					atlasLocation,
					minX,
					maxX,
					minY,
					maxY,
					blitOffset,
					minU,
					maxU,
					minV,
					maxV
			);
			return;
		}

		var last = pose.last();

		GuiBatchingController.INSTANCE.recordBlit(
				last.pose	(),
				last.normal	(),
				atlasLocation,
				minX,
				maxX,
				minY,
				maxY,
				blitOffset,
				-1,
				minU,
				maxU,
				minV,
				maxV
		);
	}

	@WrapMethod(method = "innerBlit(Lnet/minecraft/resources/ResourceLocation;IIIIIFFFFFFFF)V")
	public void renderBlitFast(
			ResourceLocation	atlasLocation,
			int					minX,
			int					maxX,
			int					minY,
			int					maxY,
			int					blitOffset,
			float				minU,
			float				maxU,
			float				minV,
			float				maxV,
			float				red,
			float				green,
			float				blue,
			float				alpha,
			Operation<Void>		original
	) {
		if (!CoreFeature.isGuiBatching()) {
			original.call(
					atlasLocation,
					minX,
					maxX,
					minY,
					maxY,
					blitOffset,
					minU,
					maxU,
					minV,
					maxV,
					red,
					green,
					blue,
					alpha
			);
			return;
		}

		var last = pose.last();

		GuiBatchingController.INSTANCE.recordBlit(
				last.pose	(),
				last.normal	(),
				atlasLocation,
				minX,
				maxX,
				minY,
				maxY,
				blitOffset,
				FastColor.ARGB32.color(
						(int) (alpha	* 255.0f),
						(int) (red		* 255.0f),
						(int) (green	* 255.0f),
						(int) (blue		* 255.0f)
				),
				minU,
				maxU,
				minV,
				maxV
		);
	}

	@SuppressWarnings	("UnstableApiUsage")
	@WrapOperation		(
			method	= "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/neoforged/neoforge/client/ItemDecoratorHandler;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V"
			)
	)
	public void renderDecorationCustomFast(
			ItemDecoratorHandler	instance,
			GuiGraphics				guiGraphics,
			Font					font,
			ItemStack				stack,
			int						xOffset,
			int						yOffset,
			Operation<Void>			original
	) {
		if (!CoreFeature.isGuiBatching()) {
			original.call(
					instance,
					guiGraphics,
					font,
					stack,
					xOffset,
					yOffset
			);
			return;
		}

		var last = pose.last();

		GuiBatchingController.INSTANCE.recordDecorator(
				last.pose	(),
				last.normal	(),
				instance,
				font,
				stack,
				xOffset,
				yOffset
		);
	}

	@WrapOperation(
			method	= "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V"
			)
	)
	public void renderItemFast(
			ItemRenderer		instance,
			ItemStack			itemStack,
			ItemDisplayContext	displayContext,
			boolean				leftHand,
			PoseStack			poseStack,
			MultiBufferSource	bufferSource,
			int					combinedLight,
			int					combinedOverlay,
			BakedModel			bakedModel,
			Operation<Void>		original
	) {
		if (		!AcceleratedItemRenderingFeature.isEnabled						()
				||	!AcceleratedItemRenderingFeature.shouldUseAcceleratedPipeline	()
				||	!AcceleratedItemRenderingFeature.shouldAccelerateInGui			()
				||	!CoreFeature					.isLoaded						()
		) {
			original.call(
					instance,
					itemStack,
					displayContext,
					leftHand,
					poseStack,
					bufferSource,
					combinedLight,
					combinedOverlay,
					bakedModel
			);
			return;
		}

		if (CoreFeature.isGuiBatching()) {
			var last = pose.last();

			GuiBatchingController.INSTANCE.recordItem(
					last.pose	(),
					last.normal	(),
					itemStack,
					displayContext,
					leftHand,
					combinedLight,
					combinedOverlay,
					bakedModel,
					bakedModel.usesBlockLight()
			);
			return;
		}

		CoreFeature.setRenderingGui();

		original.call(
				instance,
				itemStack,
				displayContext,
				leftHand,
				poseStack,
				bufferSource,
				combinedLight,
				combinedOverlay,
				bakedModel
		);

		CoreFeature						.resetRenderingGui	();
		GuiBatchingController.INSTANCE	.flushBatching		();
	}
}
