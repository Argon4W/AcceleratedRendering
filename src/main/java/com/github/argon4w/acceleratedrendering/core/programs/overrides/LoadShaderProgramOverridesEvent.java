package com.github.argon4w.acceleratedrendering.core.programs.overrides;

import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

public class LoadShaderProgramOverridesEvent extends Event implements IModBusEvent {

	private final	VertexFormat										vertexFormat;
	private final	Int2ObjectMap	<				ProgramOverride>	overridesById;
	private final	Object2ObjectMap<RenderType,	ProgramOverride>	overridesByType;
	private			int													overrideCounter;

	public LoadShaderProgramOverridesEvent(VertexFormat vertexFormat) {
		this.vertexFormat		= vertexFormat;
		this.overridesById		= new Int2ObjectOpenHashMap		<>();
		this.overridesByType	= new Object2ObjectOpenHashMap	<>();
		this.overrideCounter	= 1;
	}

	public void loadFor(
			VertexFormat		vertexFormat,
			RenderType			renderType,
			ITransformOverride	transform,
			IUploadingOverride	uploading
	) {
		if (this.vertexFormat == vertexFormat) {
			var overrideId = overrideCounter ++;

			var override = new ProgramOverride(
					overrideId,
					transform,
					uploading
			);

			overridesById	.put(overrideId, override);
			overridesByType	.put(renderType, override);
		}
	}

	public IShaderProgramOverrides getOverrides(ITransformOverride defaultTransformOverride, IUploadingOverride defaultUploadingOverride) {
		return new ProgramOverrides(
				overrideCounter,
				overridesById,
				overridesByType,
				defaultTransformOverride,
				defaultUploadingOverride
		);
	}

	public static class ProgramOverrides implements IShaderProgramOverrides {

		private final	Int2ObjectMap		<				ProgramOverride>	overridesById;
		private final	Object2ObjectMap	<RenderType,	ProgramOverride>	overridesByType;
		private final										ProgramOverride		overrideDefault;
		private final										int					overrideCount;

		public ProgramOverrides(
				int													overrideCount,
				Int2ObjectMap	<				ProgramOverride>	overridesById,
				Object2ObjectMap<RenderType,	ProgramOverride>	overridesByType,
				ITransformOverride									defaultTransformOverride,
				IUploadingOverride									defaultUploadingOverride
		) {

			this.overridesByType	= overridesByType;
			this.overridesById		= overridesById;
			this.overrideCount		= overrideCount;

			this.overrideDefault = new ProgramOverride(
					0,
					defaultTransformOverride,
					defaultUploadingOverride
			);
		}

		@Override
		public ProgramOverride getOverride(int overrideId) {
			return overridesById.getOrDefault(overrideId, overrideDefault);
		}

		@Override
		public ProgramOverride getOverride(RenderType renderType) {
			return overridesByType.getOrDefault(renderType, overrideDefault);
		}

		@Override
		public int getCount() {
			return overrideCount;
		}
	}
}
