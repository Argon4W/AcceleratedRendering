package com.github.argon4w.acceleratedrendering.features.items.gui;

import com.github.argon4w.acceleratedrendering.core.CoreBuffers;
import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.CoreStates;
import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.github.argon4w.acceleratedrendering.core.utils.PoseStackExtension;
import com.github.argon4w.acceleratedrendering.core.utils.RenderTypeUtils;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.*;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.string.IStringDrawContext;
import com.github.argon4w.acceleratedrendering.features.items.gui.renderer.AcceleratedBlitRenderer;
import com.github.argon4w.acceleratedrendering.features.items.gui.renderer.AcceleratedFillRenderer;
import com.github.argon4w.acceleratedrendering.features.items.gui.renderer.AcceleratedGradientRenderer;
import com.mojang.blaze3d.platform.Lighting;
import it.unimi.dsi.fastutil.floats.Float2ReferenceAVLTreeMap;
import it.unimi.dsi.fastutil.floats.Float2ReferenceSortedMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ItemDecoratorHandler;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.List;


@ExtensionMethod({
		VertexConsumerExtension	.class,
		BufferSourceExtension	.class,
		PoseStackExtension		.class,
})
public class GuiBatchingController {

	public static	final	GuiBatchingController								INSTANCE = new GuiBatchingController();

	private			final	IBindingState										scissorDraw;
	private			final	IBindingState										scissorFlush;
	private			final	List<BlitDrawContext>								blitDrawContexts;
	private			final	List<IStringDrawContext>							stringDrawContexts;
	private			final	List<DecoratorDrawContext>							decoratorDrawContexts;
	private			final	List<FillDrawContext>								fillDrawContexts;
	private			final	List<HighlightDrawContext>							highlightDrawContexts;
	private			final	List<GradientDrawContext>							gradientDrawContexts;
	private			final	List<ItemRenderContext>								flatItemDrawContexts;
	private			final	List<ItemRenderContext>								blockItemDrawContexts;
	private			final	Float2ReferenceSortedMap<List<IGuiElementContext>>	depthLayers;

	private GuiBatchingController() {
		this.scissorDraw			= CoreFeature.createScissorState	();
		this.scissorFlush			= CoreFeature.createScissorState	();
		this.blitDrawContexts		= new ReferenceArrayList		<>	();
		this.stringDrawContexts		= new ReferenceArrayList		<>	();
		this.decoratorDrawContexts	= new ReferenceArrayList		<>	();
		this.fillDrawContexts		= new ReferenceArrayList		<>	();
		this.highlightDrawContexts	= new ReferenceArrayList		<>	();
		this.gradientDrawContexts	= new ReferenceArrayList		<>	();
		this.flatItemDrawContexts	= new ReferenceArrayList		<>	();
		this.blockItemDrawContexts	= new ReferenceArrayList		<>	();
		this.depthLayers			= new Float2ReferenceAVLTreeMap	<>	();
	}

	public void startBatching(GuiGraphics graphics) {
		if (		graphics.bufferSource().getAcceleratable()	.isBufferSourceAcceleratable	()
				&&	AcceleratedItemRenderingFeature				.isEnabled						()
				&&	AcceleratedItemRenderingFeature				.shouldUseAcceleratedPipeline	()
				&&	AcceleratedItemRenderingFeature				.shouldAccelerateInGui			()
				&&	AcceleratedItemRenderingFeature				.shouldUseGuiItemBatching		()
				&&	CoreFeature									.isLoaded						()
		) {
			CoreFeature.setGuiBatching	();
			scissorDraw.record			(graphics);
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public void flushBatching(GuiGraphics graphics) {
		if (CoreFeature.isGuiBatching()) {
			var itemRenderer	= Minecraft.getInstance()	.getItemRenderer();
			var bufferSource	= graphics					.bufferSource	();
			var poseStack		= graphics					.pose			();

			CoreFeature.resetGuiBatching();
			CoreFeature.setRenderingGui	();

			for (float layer : depthLayers.keySet()) {
				var nextLayer	= depthLayers.tailMap	(layer);
				var elements	= depthLayers.get		(layer);
				var depth		= 0.0f;
				var step		= 0.1f;

				if (!nextLayer.isEmpty()) {
					step = 1.0f / nextLayer.firstEntry().getValue().size();
				}

				for (var element : elements) {
					element.transform().translateLocal(
							0.0f,
							0.0f,
							depth
					);

					depth += step;
				}
			}

			for (float layer : depthLayers.keySet()) {
				var elements = depthLayers.get(layer);

				if (elements.isEmpty()) {
					depthLayers.remove(layer);
				} else {
					elements.clear();
				}
			}

			for (var context : blitDrawContexts) {
				var extension = graphics.bufferSource().getBuffer(GuiRenderTypes.blit(context.atlasLocation())).getAccelerated();

				if (extension.isAccelerated()) {
					extension.doRender(
							AcceleratedBlitRenderer.INSTANCE,
							context,
							context.transform	(),
							context.normal		(),
							context.blitLight	(),
							context.blitOverlay	(),
							context.blitColor	()
					);
				}
			}

			for (var context : fillDrawContexts) {
				var extension = graphics.bufferSource().getBuffer(context.renderType()).getAccelerated();

				if (extension.isAccelerated()) {
					extension.doRender(
							AcceleratedFillRenderer.INSTANCE,
							context,
							context.transform	(),
							context.normal		(),
							context.light		(),
							context.overlay		(),
							context.color		()
					);
				}
			}

			for (var context : gradientDrawContexts) {
				var extension = graphics.bufferSource().getBuffer(context.renderType()).getAccelerated();

				if (extension.isAccelerated()) {
					extension.doRender(
							AcceleratedGradientRenderer.INSTANCE,
							context,
							context.transform	(),
							context.normal		(),
							context.light		(),
							context.overlay		(),
							-1
					);
				}
			}

			for (var context : stringDrawContexts) {
				context.drawString(graphics.bufferSource());
			}

			scissorFlush.record	(graphics);
			scissorDraw	.restore();


			Lighting	.setupForFlatItems					();
			CoreFeature	.forceSetDefaultLayer				(1);
			CoreFeature	.forceSetDefaultLayerBeforeFunction	(Lighting::setupForFlatItems);
			CoreFeature	.forceSetDefaultLayerAfterFunction	(Lighting::setupFor3DItems);

			for (var context : flatItemDrawContexts) {
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

			graphics	.flush							();
			Lighting	.setupFor3DItems				();
			CoreFeature	.resetDefaultLayer				();
			CoreFeature	.resetDefaultLayerBeforeFunction();
			CoreFeature	.resetDefaultLayerAfterFunction	();

			for (var context : blockItemDrawContexts) {
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

			CoreFeature.resetRenderingGui	();
			flushBatching					();

			for (var context : decoratorDrawContexts) {
				poseStack.pushPose	();
				poseStack.setPose	(context.transform(), context.normal());

				context.handler().render(
						graphics,
						context.font	(),
						context.stack	(),
						context.xOffset	(),
						context.yOffset	()
				);

				graphics.pose().popPose();
			}

			for (var context : highlightDrawContexts) {
				poseStack.pushPose	();
				poseStack.setPose	(context.transform(), context.normal());

				AbstractContainerScreen.renderSlotHighlight(
						graphics,
						context.highlightX	(),
						context.highlightY	(),
						context.blitOffset	(),
						context.color		()
				);

				graphics.pose().popPose();
			}

			blitDrawContexts		.clear	();
			stringDrawContexts		.clear	();
			decoratorDrawContexts	.clear	();
			fillDrawContexts		.clear	();
			highlightDrawContexts	.clear	();
			gradientDrawContexts	.clear	();
			flatItemDrawContexts	.clear	();
			blockItemDrawContexts	.clear	();
			scissorFlush			.restore();
		}
	}

	public void flushBatching() {
		CoreStates						.recordBuffers	();
		CoreBuffers.POS					.prepareBuffers	();
		CoreBuffers.POS_COLOR			.prepareBuffers	();
		CoreBuffers.POS_TEX_COLOR		.prepareBuffers	();
		CoreBuffers.POS_COLOR_TEX_LIGHT	.prepareBuffers	();
		CoreBuffers.ENTITY				.prepareBuffers	();
		CoreBuffers.BLOCK				.prepareBuffers	();
		CoreBuffers.POS_TEX				.prepareBuffers	();
		CoreStates						.restoreBuffers	();

		CoreBuffers.POS					.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_COLOR			.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_TEX_COLOR		.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_COLOR_TEX_LIGHT	.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.ENTITY				.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.BLOCK				.drawBuffers	(LayerDrawType.ALL);
		CoreBuffers.POS_TEX				.drawBuffers	(LayerDrawType.ALL);

		CoreBuffers.POS					.clearBuffers	();
		CoreBuffers.POS_COLOR			.clearBuffers	();
		CoreBuffers.POS_TEX_COLOR		.clearBuffers	();
		CoreBuffers.POS_COLOR_TEX_LIGHT	.clearBuffers	();
		CoreBuffers.ENTITY				.clearBuffers	();
		CoreBuffers.BLOCK				.clearBuffers	();
		CoreBuffers.POS_TEX				.clearBuffers	();
	}

	public void submitBlit(
			Matrix4f			transform,
			Matrix3f			normal,
			ResourceLocation	atlasLocation,
			int					minX,
			int					maxX,
			int					minY,
			int					maxY,
			int					blitOffset,
			int					blitColor,
			float				minU,
			float				maxU,
			float				minV,
			float				maxV
	) {
		var layer = getLayer(getGlobalDepth(
				transform.m22(),
				transform.m32(),
				blitOffset
		));

		var context = new BlitDrawContext(
				new Matrix4f(transform),
				new Matrix3f(normal),
				atlasLocation,
				minX,
				maxX,
				minY,
				maxY,
				blitOffset,
				blitColor,
				0,
				0,
				minU,
				maxU,
				minV,
				maxV
		);

		blitDrawContexts.add(context);
		layer			.add(context);
	}

	public void submitItem(
			Matrix4f			transform,
			Matrix3f			normal,
			ItemStack			itemStack,
			ItemDisplayContext	displayContext,
			boolean				leftHand,
			int					combinedLight,
			int					combinedOverlay,
			BakedModel			bakedModel,
			boolean				blockLight
	) {
		var layer = getLayer(getGlobalDepth(
				transform.m22(),
				transform.m32(),
				0.0f
		));

		var context = new ItemRenderContext(
				new Matrix4f(transform),
				new Matrix3f(normal),
				itemStack,
				displayContext,
				leftHand,
				combinedLight,
				combinedOverlay,
				bakedModel
		);

		var contexts = blockLight ? blockItemDrawContexts : flatItemDrawContexts;

		contexts.add(context);
		layer	.add(context);
	}

	public void submitFill(
			Matrix4f	transform,
			Matrix3f	normal,
			RenderType	renderType,
			int			minX,
			int			minY,
			int			maxX,
			int			maxY,
			int			blitOffset,
			int			color
	) {
		if (RenderTypeUtils.hasDepth(renderType)) {
			var layer = getLayer(getGlobalDepth(
					transform.m22(),
					transform.m32(),
					blitOffset
			));

			var context = new FillDrawContext(
					new Matrix4f(transform),
					new Matrix3f(normal),
					renderType,
					minX,
					minY,
					maxX,
					maxY,
					blitOffset,
					color,
					0,
					0
			);

			fillDrawContexts.add(context);
			layer			.add(context);
		} else {
			var depth = depthLayers.lastFloatKey();
			var layer = depthLayers.get			(depth);

			var context = new FillDrawContext(
					new Matrix4f				(transform).translate(0.0f, 0.0f, depth),
					new Matrix3f				(normal),
					RenderTypeUtils.withDepth	(renderType),
					minX,
					minY,
					maxX,
					maxY,
					0,
					color,
					0,
					0
			);

			fillDrawContexts.add(context);
			layer			.add(context);
		}
	}

	public void submitGradient(
			Matrix4f	transform,
			Matrix3f	normal,
			RenderType	renderType,
			int			minX,
			int			minY,
			int			maxX,
			int			maxY,
			int			blitOffset,
			int			colorFrom,
			int			colorTo
	) {
		if (RenderTypeUtils.hasDepth(renderType)) {
			var layer = getLayer(getGlobalDepth(
					transform.m22(),
					transform.m32(),
					blitOffset
			));

			var context = new GradientDrawContext(
					new Matrix4f(transform),
					new Matrix3f(normal),
					renderType,
					minX,
					minY,
					maxX,
					maxY,
					blitOffset,
					colorFrom,
					colorTo,
					0,
					0
			);

			gradientDrawContexts.add(context);
			layer				.add(context);
		} else {
			var depth = depthLayers.lastFloatKey();
			var layer = depthLayers.get			(depth);

			var context = new GradientDrawContext(
					new Matrix4f				(transform).translate(0.0f, 0.0f, depth),
					new Matrix3f				(normal),
					RenderTypeUtils.withDepth	(renderType),
					minX,
					minY,
					maxX,
					maxY,
					0,
					colorFrom,
					colorTo,
					0,
					0
			);

			gradientDrawContexts.add(context);
			layer				.add(context);
		}
	}

	@SuppressWarnings("UnstableApiUsage")
	public void submitCustomDecorator(
			Matrix4f				transform,
			Matrix3f				normal,
			ItemDecoratorHandler	handler,
			Font					font,
			ItemStack				itemStack,
			int						xOffset,
			int						yOffset
	) {
		var layer = getLayer(getGlobalDepth(
				transform.m22(),
				transform.m32(),
				100.0f
		));

		var context = new DecoratorDrawContext(
				new Matrix4f(transform),
				new Matrix3f(normal),
				handler,
				font,
				itemStack,
				xOffset,
				yOffset
		);

		decoratorDrawContexts	.add(context);
		layer					.add(context);
	}

	public void submitHighlight(
			Matrix4f	transform,
			Matrix3f	normal,
			int			highlightX,
			int			highlightY,
			int			blitOffset,
			int			color
	) {
		var layer = getLayer(getGlobalDepth(
				transform.m22(),
				transform.m32(),
				blitOffset
		));

		var context = new HighlightDrawContext(
				new Matrix4f(transform),
				new Matrix3f(normal),
				highlightX,
				highlightY,
				blitOffset,
				color
		);

		highlightDrawContexts	.add(context);
		layer					.add(context);
	}

	public void submitString(IStringDrawContext context) {
		var layer = getLayer(getGlobalDepth(
				context.transform().m22(),
				context.transform().m32(),
				0.0f
		));

		stringDrawContexts	.add(context);
		layer				.add(context);
	}

	private List<IGuiElementContext> getLayer(float depth) {
		var layer = depthLayers.get(depth);

		if (layer == null) {
			layer = new ReferenceArrayList<>();
			depthLayers.put(depth, layer);
		}

		return layer;
	}

	public static float getGlobalDepth(
			float m22,
			float m32,
			float localDepth
	) {
		return m22 * localDepth + m32;
	}

	public void delete() {
		scissorDraw	.delete();
		scissorFlush.delete();
	}
}
