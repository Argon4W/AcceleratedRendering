package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.core.buffers.AcceleratedBufferSources;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.mojang.blaze3d.vertex.VertexFormat;

public class CoreBuffers {

	public static final AcceleratedBufferSource BLOCK					= new AcceleratedBufferSource(IBufferEnvironment.Presets.BLOCK);
	public static final AcceleratedBufferSource ENTITY					= new AcceleratedBufferSource(IBufferEnvironment.Presets.ENTITY);
	public static final AcceleratedBufferSource	POS						= new AcceleratedBufferSource(IBufferEnvironment.Presets.POS);
	public static final AcceleratedBufferSource POS_COLOR				= new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_COLOR);
	public static final AcceleratedBufferSource POS_TEX					= new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_TEX);
	public static final AcceleratedBufferSource POS_TEX_COLOR			= new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_TEX_COLOR);
	public static final AcceleratedBufferSource POS_COLOR_TEX_LIGHT		= new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_COLOR_TEX_LIGHT);
	public static final AcceleratedBufferSource POS_TEX_COLOR_OUTLINE	= new AcceleratedBufferSource(IBufferEnvironment.Presets.POS_TEX_COLOR);

	public static final AcceleratedBufferSources CORE = AcceleratedBufferSources
			.builder()
			.source	(CoreBuffers		.BLOCK)
			.source	(CoreBuffers		.ENTITY)
			.source	(CoreBuffers		.POS)
			.source	(CoreBuffers		.POS_COLOR)
			.source	(CoreBuffers		.POS_TEX)
			.source	(CoreBuffers		.POS_TEX_COLOR)
			.source	(CoreBuffers		.POS_COLOR_TEX_LIGHT)
			.mode	(VertexFormat.Mode	.QUADS)
			.mode	(VertexFormat.Mode	.TRIANGLES)
			.build	();

	public static final AcceleratedBufferSources OUTLINE = AcceleratedBufferSources
			.builder()
			.source	(CoreBuffers		.POS_TEX_COLOR_OUTLINE)
			.mode	(VertexFormat.Mode	.QUADS)
			.mode	(VertexFormat.Mode	.TRIANGLES)
			.build	();
}
