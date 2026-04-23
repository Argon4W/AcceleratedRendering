package com.github.argon4w.acceleratedrendering.core.utils;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class RenderTypeUtils {

	public static final Map<RenderType, RenderType> WITH_DEPTH_CACHE = new Object2ObjectOpenHashMap<>();

	public static ResourceLocation getTextureLocation(RenderType renderType) {
		if (renderType == null) {
			return null;
		}

		if (!(renderType instanceof RenderType.CompositeRenderType composite)) {
			return null;
		}

		return composite
				.state
				.textureState
				.cutoutTexture()
				.orElse(null);
	}

	public static boolean isCulled(RenderType renderType) {
		if (renderType == null) {
			return false;
		}

		if (!(renderType instanceof RenderType.CompositeRenderType composite)) {
			return false;
		}

		return composite
				.state
				.cullState
				.enabled;
	}

	public static boolean isDynamic(RenderType renderType) {
		if (renderType == null) {
			return false;
		}

		if (!(renderType instanceof RenderType.CompositeRenderType composite)) {
			return false;
		}

		return composite
				.state
				.texturingState instanceof RenderStateShard.OffsetTexturingStateShard;
	}

	public static boolean hasDepth(RenderType renderType) {
		if (renderType == null) {
			return false;
		}

		if (!(renderType instanceof RenderType.CompositeRenderType composite)) {
			return false;
		}

		return composite
				.state
				.depthTestState != RenderStateShard.NO_DEPTH_TEST;
	}

	public static RenderType withDepth(RenderType renderType) {
		if (renderType == null) {
			return null;
		}

		if (hasDepth(renderType)) {
			return null;
		}

		if (!(renderType instanceof RenderType.CompositeRenderType composite)) {
			return null;
		}

		var result = WITH_DEPTH_CACHE.get(renderType);

		if (result != null) {
			return result;
		}

		var state = composite.state;

		result = RenderType.create(
				renderType.name,
				renderType.format			(),
				renderType.mode				(),
				renderType.bufferSize		(),
				renderType.affectsCrumbling	(),
				renderType.sortOnUpload,
				RenderType.CompositeState
						.builder				()
						.setDepthTestState		(RenderStateShard	.LEQUAL_DEPTH_TEST)
						.setWriteMaskState		(state				.writeMaskState.writeColor ? RenderStateShard.COLOR_DEPTH_WRITE : RenderStateShard.DEPTH_WRITE)
						.setTextureState		(state				.textureState)
						.setShaderState			(state				.shaderState)
						.setTransparencyState	(state				.transparencyState)
						.setCullState			(state				.cullState)
						.setLightmapState		(state				.lightmapState)
						.setOverlayState		(state				.overlayState)
						.setLayeringState		(state				.layeringState)
						.setOutputState			(state				.outputState)
						.setTexturingState		(state				.texturingState)
						.setLineState			(state				.lineState)
						.setColorLogicState		(state				.colorLogicState)
						.createCompositeState	(state				.outlineProperty)
		);

		return result;
	}

	public static LayerDrawType getDrawType(RenderType renderType) {
		return renderType.sortOnUpload ? LayerDrawType.TRANSLUCENT : LayerDrawType.OPAQUE;
	}

	public static boolean isTranslucent(RenderType renderType) {
		return renderType.sortOnUpload;
	}
}
