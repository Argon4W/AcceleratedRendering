package com.github.argon4w.acceleratedrendering.features.filter.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.utils.PoseStackExtension;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.filter.FilterFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.GuiBatchingController;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.ItemRenderContext;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@ExtensionMethod(PoseStackExtension		.class)
@Mixin			(GuiBatchingController	.class)
public class GuiBatchingControllerMixin {

	@Unique private final List<ItemRenderContext> filteredFlatItemDrawContexts	= new ReferenceArrayList<>();
	@Unique private final List<ItemRenderContext> filteredBlockItemDrawContexts	= new ReferenceArrayList<>();

	@WrapOperation(
			method = "submitItem",
			at = @At(
					value	= "INVOKE",
					target	= "Ljava/util/List;add(Ljava/lang/Object;)Z"
			)
	)
	public boolean filterItem(
			List<?>								drawContexts,
			Object								itemRenderContext,
			Operation<Boolean>					original,
			@Local(name = "blockLight") boolean	blockLight
	) {
		var pass =	!	CoreFeature		.isLoaded			()
				||	!	FilterFeature	.isEnabled			()
				||	!	FilterFeature	.shouldFilterItems	()
				||		FilterFeature	.testItem			(((ItemRenderContext) itemRenderContext).itemStack());

		if (!pass) {
			drawContexts = blockLight
					? filteredBlockItemDrawContexts
					: filteredFlatItemDrawContexts;
		}

		return original.call(drawContexts, itemRenderContext);
	}

	@Inject(
			method	= "flushBatching(Lnet/minecraft/client/gui/GuiGraphics;)F",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/features/items/gui/GuiBatchingController;flushBatching()V",
					shift	= At.Shift.AFTER
			)
	)
	public void flushFilteredItems(
			GuiGraphics														graphics,
			CallbackInfoReturnable<Float>									cir,
			@Local(name = "bufferSource")	MultiBufferSource.BufferSource	bufferSource,
			@Local(name = "itemRenderer")	ItemRenderer					itemRenderer,
			@Local(name = "poseStack")		PoseStack						poseStack
	) {
		AcceleratedEntityRenderingFeature	.useVanillaPipeline();
		AcceleratedItemRenderingFeature		.useVanillaPipeline();
		AcceleratedTextRenderingFeature		.useVanillaPipeline();

		Lighting.setupForFlatItems();

		for (var context : filteredFlatItemDrawContexts) {
			poseStack.pushPose	();
			poseStack.setPose	(context.transform(), context.normal());

			itemRenderer.render(
					context.itemStack		(),
					context.displayContext	(),
					context.leftHand		(),
					poseStack,
					bufferSource,
					context.combinedLight	(),
					context.combinedOverlay	(),
					context.bakedModel		()
			);

			poseStack.popPose();
		}

		graphics	.flush			();
		Lighting	.setupFor3DItems();

		for (var context : filteredBlockItemDrawContexts) {
			poseStack.pushPose	();
			poseStack.setPose	(context.transform(), context.normal());

			itemRenderer.render(
					context.itemStack		(),
					context.displayContext	(),
					context.leftHand		(),
					poseStack,
					bufferSource,
					context.combinedLight	(),
					context.combinedOverlay	(),
					context.bakedModel		()
			);

			poseStack.popPose();
		}

		graphics							.flush			();
		AcceleratedEntityRenderingFeature	.resetPipeline	();
		AcceleratedItemRenderingFeature		.resetPipeline	();
		AcceleratedTextRenderingFeature		.resetPipeline	();
	}

	@Inject(
			method	= "flushBatching(Lnet/minecraft/client/gui/GuiGraphics;)F",
			at		= @At(
					value	= "INVOKE",
					target	= "Ljava/util/List;clear()V",
					ordinal	= 0,
					shift	= At.Shift.BEFORE
			)
	)
	public void clearFilteredItems(GuiGraphics graphics, CallbackInfoReturnable<Float> cir) {
		filteredFlatItemDrawContexts	.clear();
		filteredBlockItemDrawContexts	.clear();
	}
}
