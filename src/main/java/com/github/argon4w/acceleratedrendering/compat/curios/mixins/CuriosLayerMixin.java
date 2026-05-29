package com.github.argon4w.acceleratedrendering.compat.curios.mixins;

import com.github.argon4w.acceleratedrendering.compat.curios.CuriosCompatFeature;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.filter.FilterFeature;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.client.render.CuriosLayer;

@Pseudo
@Mixin(CuriosLayer.class)
public class CuriosLayerMixin {

	@Inject(
			method	= "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			at		= @At("HEAD"),
			remap	= false
	)
	public void startRenderCuriosLayer(
			PoseStack			matrixStack,
			MultiBufferSource	renderTypeBuffer,
			int					light,
			LivingEntity		livingEntity,
			float				limbSwing,
			float				limbSwingAmount,
			float				partialTicks,
			float				ageInTicks,
			float				netHeadYaw,
			float				headPitch,
			CallbackInfo		ci
	) {
		if (			CoreFeature			.isLoaded				()
				&&		CuriosCompatFeature	.isEnabled				()
				&&	!	CuriosCompatFeature	.shouldAccelerateCurios	()
		) {
			AcceleratedEntityRenderingFeature	.useVanillaPipeline();
			AcceleratedItemRenderingFeature		.useVanillaPipeline();
			AcceleratedTextRenderingFeature		.useVanillaPipeline();
		}
	}

	@Inject(
			method	= "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
			at		= @At("RETURN"),
			remap	= false
	)
	public void stopRenderCuriosLayer(
			PoseStack			matrixStack,
			MultiBufferSource	renderTypeBuffer,
			int					light,
			LivingEntity		livingEntity,
			float				limbSwing,
			float				limbSwingAmount,
			float				partialTicks,
			float				ageInTicks,
			float				netHeadYaw,
			float				headPitch,
			CallbackInfo		ci
	) {
		if (			CoreFeature			.isLoaded				()
				&&		CuriosCompatFeature	.isEnabled				()
				&&	!	CuriosCompatFeature	.shouldAccelerateCurios	()
		) {
			AcceleratedEntityRenderingFeature	.resetPipeline();
			AcceleratedItemRenderingFeature		.resetPipeline();
			AcceleratedTextRenderingFeature		.resetPipeline();
		}
	}

	@WrapOperation(
			method	= "lambda$render$0",
			at		= @At(
					value	= "INVOKE",
					target	= "Ltop/theillusivec4/curios/api/client/ICurioRenderer;render(Lnet/minecraft/world/item/ItemStack;Ltop/theillusivec4/curios/api/SlotContext;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/RenderLayerParent;Lnet/minecraft/client/renderer/MultiBufferSource;IFFFFFF)V"
			),
			remap	= false
	)
	public void filterCuriosItem(
			ICurioRenderer			instance,
			ItemStack				itemStack,
			SlotContext				slotContext,
			PoseStack				poseStack,
			RenderLayerParent<?, ?>	renderLayerParent,
			MultiBufferSource		bufferSource,
			int						light,
			float					limbSwing,
			float					limbSwingAmount,
			float					partialTicks,
			float					ageInTicks,
			float					netHeadYaw,
			float					headPitch,
			Operation<Void>			original
	) {
		var pass =	!	CoreFeature			.isLoaded				()
				||	!	FilterFeature		.isEnabled				()
				||	!	CuriosCompatFeature	.isEnabled				()
				||	!	CuriosCompatFeature	.shouldFilterCuriosItems()
				||		CuriosCompatFeature	.testCuriosItem			(itemStack);

		if (!pass) {
			AcceleratedEntityRenderingFeature	.useVanillaPipeline();
			AcceleratedItemRenderingFeature		.useVanillaPipeline();
			AcceleratedTextRenderingFeature		.useVanillaPipeline();
		}

		original.call(
				instance,
				itemStack,
				slotContext,
				poseStack,
				renderLayerParent,
				bufferSource,
				light,
				limbSwing,
				limbSwingAmount,
				partialTicks,
				ageInTicks,
				netHeadYaw,
				headPitch
		);

		if (!pass) {
			AcceleratedEntityRenderingFeature	.resetPipeline();
			AcceleratedItemRenderingFeature		.resetPipeline();
			AcceleratedTextRenderingFeature		.resetPipeline();
		}
	}
}
