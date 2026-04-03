package com.github.argon4w.acceleratedrendering.features.items.gui;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class GuiRenderTypes extends RenderType {

	public static final ShaderStateShard POSITION_TEX_COLOR_SHADER = new ShaderStateShard(GameRenderer::getPositionTexColorShader);

	public static final Function<ResourceLocation, RenderType> BLIT = Util.memoize(atlasLocation -> create(
				"acceleratedrendering:blit",
				DefaultVertexFormat	.POSITION_TEX_COLOR,
				VertexFormat.Mode	.QUADS,
				256,
				false,
				false,
				CompositeState
						.builder()
						.setTextureState		(new TextureStateShard(atlasLocation, false, false))
						.setShaderState			(POSITION_TEX_COLOR_SHADER)
						.setDepthTestState		(LEQUAL_DEPTH_TEST)
						.setTransparencyState	(TRANSLUCENT_TRANSPARENCY)
						.createCompositeState	(false)
	));

	private GuiRenderTypes(
			String				name,
			VertexFormat		format,
			VertexFormat.Mode	formatMode,
			int					bufferSize,
			boolean				affectsCrumbling,
			boolean				sortOnUpload,
			Runnable			setupState,
			Runnable			clearState
	) {
		super(
				name,
				format,
				formatMode,
				bufferSize,
				affectsCrumbling,
				sortOnUpload,
				setupState,
				clearState
		);
	}

	public static RenderType blit(ResourceLocation atlasLocation) {
		return BLIT.apply(atlasLocation);
	}
}
