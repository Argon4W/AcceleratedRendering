package com.github.argon4w.acceleratedrendering.features.items.gui;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.utils.RenderTypeUtils;
import com.github.argon4w.acceleratedrendering.features.items.AcceleratedItemRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.IAcceleratedGuiGraphics;
import com.github.argon4w.acceleratedrendering.features.items.gui.contexts.*;
import com.github.argon4w.acceleratedrendering.features.items.gui.renderer.AcceleratedBlitRenderer;
import com.github.argon4w.acceleratedrendering.features.items.gui.renderer.AcceleratedRectangleRenderer;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ItemDecoratorHandler;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Comparator;
import java.util.List;

@SuppressWarnings	("UnstableApiUsage")
@ExtensionMethod	(VertexConsumerExtension.class)
public class GuiBatchingController {

	public static	final	GuiBatchingController		INSTANCE = new GuiBatchingController();

	private			final	IBindingState				scissorDraw;
	private			final	IBindingState				scissorFlush;
	private			final	List<BlitDrawContext>		blitDrawContexts;
	private			final	List<StringDrawContext>		stringDrawContexts;
	private			final	List<DecoratorDrawContext>	decoratorDrawContexts;
	private			final	List<HighlightDrawContext>	highlightDrawContexts;
	private			final	List<RectangleDrawContext>	rectangleDrawContexts;

	private					float						currentDepth;

	private GuiBatchingController() {
		this.scissorDraw			= CoreFeature.createScissorState();
		this.scissorFlush			= CoreFeature.createScissorState();
		this.blitDrawContexts		= new ReferenceArrayList<>		();
		this.stringDrawContexts		= new ReferenceArrayList<>		();
		this.decoratorDrawContexts	= new ReferenceArrayList<>		();
		this.highlightDrawContexts	= new ReferenceArrayList<>		();
		this.rectangleDrawContexts	= new ReferenceArrayList<>		();

		this.currentDepth = -100.0f;
	}

	public void startBatching(GuiGraphics graphics) {
		if (		AcceleratedItemRenderingFeature	.isEnabled						()
				&&	AcceleratedItemRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	AcceleratedItemRenderingFeature	.shouldAccelerateInGui			()
				&&	AcceleratedItemRenderingFeature	.shouldUseGuiItemBatching		()
				&&	CoreFeature						.isLoaded						()
		) {
			CoreFeature.setGuiBatching	();
			scissorDraw.record			(graphics);
		}
	}

	public void flushBatching(GuiGraphics graphics) {
		if (CoreFeature.isGuiBatching()) {
			var poseStack = graphics.pose();

			CoreFeature.resetGuiBatching();
			CoreFeature.setRenderingGui	();

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
				} else {
					poseStack.pushPose	();
					poseStack.last		().pose		().set(context.transform());
					poseStack.last		().normal	().set(context.normal	());

					var blitColor = context.blitColor();

					graphics.innerBlit(
							context.atlasLocation	(),
							context.minX			(),
							context.maxX			(),
							context.minY			(),
							context.maxY			(),
							context.blitOffset		(),
							context.minU			(),
							context.maxU			(),
							context.minV			(),
							context.maxV			(),
							FastColor.ARGB32.red	(blitColor),
							FastColor.ARGB32.green	(blitColor),
							FastColor.ARGB32.blue	(blitColor),
							FastColor.ARGB32.alpha	(blitColor)
					);

					graphics.pose().popPose();
				}
			}

			rectangleDrawContexts.sort(Comparator.naturalOrder());

			for (var context : rectangleDrawContexts) {
				var extension = graphics.bufferSource().getBuffer(context.renderType()).getAccelerated();

				if (extension.isAccelerated()) {
					extension.doRender(
							AcceleratedRectangleRenderer.INSTANCE,
							context,
							context.transform	(),
							context.normal		(),
							context.light		(),
							context.overlay		(),
							context.color		()
					);
				} else {
					poseStack.pushPose	();
					poseStack.last		().pose		().set(context.transform());
					poseStack.last		().normal	().set(context.normal	());

					graphics.fill(
							context.renderType	(),
							context.minX		(),
							context.minY		(),
							context.maxX		(),
							context.maxY		(),
							context.color		()
					);

					graphics.pose().popPose();
				}
			}

			for (var context : stringDrawContexts) {
				context.font().drawInBatch(
						context	.text			(),
						context	.textX			(),
						context	.textY			(),
						context	.textColor		(),
						context	.dropShadow		(),
						context	.transform		(),
						graphics.bufferSource	(),
						context	.displayMode	(),
						context	.backgroundColor(),
						context	.packedLight	(),
						context	.bidirectional	()
				);
			}

			scissorFlush						.record				(graphics);
			scissorDraw							.restore			();
			CoreFeature							.resetRenderingGui	();
			((IAcceleratedGuiGraphics) graphics).flushItemBatching	();

			for (var context : decoratorDrawContexts) {
				poseStack.pushPose	();
				poseStack.last		().pose()	.set(context.transform	());
				poseStack.last		().normal()	.set(context.normal		());

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
				poseStack.last		().pose		().set(context.transform());
				poseStack.last		().normal	().set(context.normal	());

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
			highlightDrawContexts	.clear	();
			rectangleDrawContexts	.clear	();
			scissorFlush			.restore();

			currentDepth = -100.0f;
		}
	}

	public void recordBlit(
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
		updateDepth(
				transform.m22(),
				transform.m32(),
				blitOffset
		);

		blitDrawContexts.add(new BlitDrawContext(
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
		));
	}

	public void recordString(
			Matrix4f			transform,
			Font				font,
			String				text,
			float				textX,
			float 				textY,
			int					textColor,
			boolean				dropShadow,
			Font.DisplayMode	displayMode,
			int					backgroundColor,
			int					packedLight,
			boolean				bidirectional
	) {
		updateDepth(
				transform.m22(),
				transform.m32(),
				0.0f
		);

		stringDrawContexts.add(new StringDrawContext(
				new Matrix4f(transform),
				font,
				text,
				textX,
				textY,
				textColor,
				dropShadow,
				displayMode,
				backgroundColor,
				packedLight,
				bidirectional
		));
	}

	public void recordRectangle(
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
			updateDepth(
					transform.m22(),
					transform.m32(),
					blitOffset
			);

			rectangleDrawContexts.add(new RectangleDrawContext(
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
			));
		} else {
			rectangleDrawContexts.add(new RectangleDrawContext(
					new Matrix4f				(transform),
					new Matrix3f				(normal),
					RenderTypeUtils.withDepth	(renderType),
					minX,
					minY,
					maxX,
					maxY,
					getLocalDepthAssumeSafe(
							transform.m22(),
							transform.m32(),
							++ currentDepth
					),
					color,
					0,
					0
			));
		}
	}

	public void recordDecorator(
			Matrix4f				transform,
			Matrix3f				normal,
			ItemDecoratorHandler	handler,
			Font					font,
			ItemStack				itemStack,
			int						xOffset,
			int						yOffset
	) {
		updateDepth(
				transform.m22(),
				transform.m32(),
				100.0f
		);

		decoratorDrawContexts.add(new DecoratorDrawContext(
				new Matrix4f(transform),
				new Matrix3f(normal),
				handler,
				font,
				itemStack,
				xOffset,
				yOffset
		));
	}

	public void recordHighlight(
			Matrix4f	transform,
			Matrix3f	normal,
			int			highlightX,
			int			highlightY,
			int			blitOffset,
			int			color
	) {
		highlightDrawContexts.add(new HighlightDrawContext(
				new Matrix4f(transform),
				new Matrix3f(normal),
				highlightX,
				highlightY,
				blitOffset,
				color
		));
	}

	private void updateDepth(
			float m22,
			float m32,
			float localDepth
	) {
		currentDepth = Math.max(currentDepth, m22 * localDepth + m32);
	}

	private int getLocalDepthAssumeSafe(
			float m22,
			float m32,
			float globalDepth
	) {
		return (int) ((globalDepth - m32) / m22);
	}

	public void delete() {
		scissorDraw	.delete();
		scissorFlush.delete();
	}
}
