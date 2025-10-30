package com.github.argon4w.acceleratedrendering.core.buffers;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.IAcceleratedBufferSource;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.utils.RenderTypeUtils;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;
import java.util.Set;

public class AcceleratedBufferSources implements IAcceleratedBufferSource {

	private final Map<VertexFormat, AcceleratedBufferSource>	sources;
	private final Set<VertexFormat.Mode>						validModes;
	private final boolean										supportTranslucent;
	private final boolean										supportDynamic;

	private AcceleratedBufferSources(
			Map<VertexFormat, AcceleratedBufferSource>	sources,
			Set<VertexFormat.Mode>						validModes,
			boolean										supportTranslucent,
			boolean										supportDynamic
	) {
		this.sources			= sources;
		this.validModes			= validModes;
		this.supportTranslucent	= supportTranslucent;
		this.supportDynamic		= supportDynamic;
	}

	@Override
	public AcceleratedBufferBuilder getBuffer(
			RenderType	renderType,
			Runnable	before,
			Runnable	after,
			int			layer
	) {
		if (			renderType		!= null
				&& 	(	CoreFeature		.shouldForceAccelerateTranslucent	()	|| supportTranslucent	|| !RenderTypeUtils.isTranslucent	(renderType))
				&& 	(	CoreFeature		.shouldCacheDynamicRenderType		()	|| supportDynamic		|| !RenderTypeUtils.isDynamic		(renderType))
				&&		validModes		.contains							(renderType.mode)
				&&		sources			.containsKey						(renderType.format)
		) {
			return sources
					.get		(renderType.format)
					.getBuffer	(
							renderType,
							before,
							after,
							layer
					);
		}

		return null;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final	Map<VertexFormat, AcceleratedBufferSource>	sources;
		private final	Set<VertexFormat.Mode>						validModes;

		private			boolean										supportTranslucent;
		private			boolean										supportDynamic;

		private Builder() {
			this.sources			= new Reference2ObjectOpenHashMap	<>();
			this.validModes			= new ReferenceOpenHashSet			<>();

			this.supportTranslucent	= false;
			this.supportDynamic		= false;
		}

		public Builder source(AcceleratedBufferSource bufferSource) {
			for (var format : bufferSource
					.getEnvironment		()
					.getVertexFormats	()
			) {
				sources.put(format, bufferSource);
			}

			return this;
		}

		public Builder mode(VertexFormat.Mode mode) {
			validModes.add(mode);
			return this;
		}

		public Builder supportTranslucent() {
			supportTranslucent = true;
			return this;
		}

		public Builder supportDynamic() {
			supportDynamic = true;
			return this;
		}

		public AcceleratedBufferSources build() {
			return new AcceleratedBufferSources(
					sources,
					validModes,
					supportTranslucent,
					supportDynamic
			);
		}
	}
}
