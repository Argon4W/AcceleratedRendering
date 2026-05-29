package com.github.argon4w.acceleratedrendering.core.programs;

import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeProgram;
import com.github.argon4w.acceleratedrendering.core.backends.programs.ComputeShader;
import com.github.argon4w.acceleratedrendering.core.utils.AvailabilityUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.fml.ModLoader;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ComputeShaderProgramLoader extends SimplePreparableReloadListener<Map<ResourceLocation, ComputeShaderProgramLoader.ShaderSource>> {

	public	static final	ComputeShaderProgramLoader				INSTANCE		= new ComputeShaderProgramLoader();
	private	static final	Map<ResourceLocation, ComputeProgram>	COMPUTE_SHADERS	= new Object2ObjectOpenHashMap<>();
	private	static			boolean									LOADED			= false;

	@Override
	protected Map<ResourceLocation, ShaderSource> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		try {
			var shaderSources	= new Object2ObjectOpenHashMap<ResourceLocation, ShaderSource>				();
			var shaderLocations	= ModLoader.get().postEventWithReturn(new LoadComputeShaderEvent()).build	();

			for (ResourceLocation key : shaderLocations.keySet()) {
				var definition			= shaderLocations	.get			(key);
				var resourceLocation	= definition		.location		();
				var barrierFlags		= definition		.barrierFlags	();

				if (resourceLocation == null) {
					throw new IllegalStateException("Found empty shader location on: \"" + key + "\"");
				}

				var resource = resourceManager.getResource(resourceLocation);

				if (resource.isEmpty()) {
					throw new IllegalStateException("Cannot found compute shader: \"" + resourceLocation + "\"");
				}

				try (var stream = resource.get().open()) {
					shaderSources.put(key, new ShaderSource(IOUtils.toString(stream, StandardCharsets.UTF_8), barrierFlags));
				}
			}

			return shaderSources;
		} catch (Exception e) {
			throw new ReportedException(CrashReport.forThrowable(e, "Exception while loading compute shader"));
		}
	}

	@Override
	protected void apply(
			Map<ResourceLocation, ShaderSource>	shaderSources,
			ResourceManager						resourceManager,
			ProfilerFiller						profiler
	) {
		RenderSystem.recordRenderCall(() -> {
			if (!AvailabilityUtils.isAvailable()) {
				return;
			}

			try {
				for (var key : shaderSources.keySet()) {
					var source			= shaderSources	.get(key);
					var shaderSource	= source		.source;
					var barrierFlags	= source		.barrierFlags;

					var program			= new ComputeProgram(barrierFlags);
					var computeShader	= new ComputeShader	();

					computeShader.setShaderSource	(shaderSource);
					computeShader.compileShader		();

					if (!computeShader.isCompiled()) {
						throw new IllegalStateException("Shader \"" + key + "\" failed to compile because of the following errors: " + computeShader.getInfoLog());
					}

					program.attachShader(computeShader);
					program.linkProgram	();

					if (!program.isLinked()) {
						throw new IllegalStateException("Program \"" + key + "\" failed to link because of the following errors: " + program.getInfoLog());
					}

					computeShader	.delete	();
					COMPUTE_SHADERS	.put	(key, program);
				}
			} catch (Exception e) {
				throw new ReportedException(CrashReport.forThrowable(e, "Exception while compiling/linking compute shader"));
			} finally {
				LOADED = true;
			}
		});
	}

	public static ComputeProgram getProgram(ResourceLocation resourceLocation) {
		var program = COMPUTE_SHADERS.get(resourceLocation);

		if (program == null) {
			throw new IllegalStateException("Get shader program \""+ resourceLocation + "\" too early! Program is not loaded yet!");
		}

		return program;
	}

	public static void delete() {
		for (var program : COMPUTE_SHADERS.values()) {
			program.delete();
		}

		LOADED	= false;
	}

	public static boolean isProgramsLoaded() {
		return LOADED;
	}

	public record ShaderSource(String source, int barrierFlags) {

	}
}
