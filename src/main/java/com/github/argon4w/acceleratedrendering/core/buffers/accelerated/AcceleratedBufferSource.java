package com.github.argon4w.acceleratedrendering.core.buffers.accelerated;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.AcceleratedBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerDrawType;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.LayerKey;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.functions.CustomLayerFunction;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.functions.EmptyLayerFunction;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.layers.storage.empty.EmptyLayerStorage;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.github.argon4w.acceleratedrendering.core.programs.dispatchers.MeshUploadingProgramDispatcher;
import com.github.argon4w.acceleratedrendering.core.utils.RenderTypeUtils;
import com.github.argon4w.acceleratedrendering.core.utils.ShaderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;

import java.util.Map;
import java.util.Set;

import static org.lwjgl.opengl.GL46.*;

public class AcceleratedBufferSource implements IAcceleratedBufferSource {

	@Getter private	final	IBufferEnvironment						environment;
	private			final	AcceleratedRingBuffers					ringBuffers;
	private			final	Set<AcceleratedRingBuffers.Buffers>		buffers;
	private			final	Map<LayerKey, AcceleratedBufferBuilder> activeBuilders;
	private			final	IntSet									activeLayers;

	private					AcceleratedRingBuffers.Buffers			currentBuffer;
	private 				boolean									used;
	private					int										barriers;

	public AcceleratedBufferSource(IBufferEnvironment bufferEnvironment) {
		this.environment	= bufferEnvironment;
		this.ringBuffers	= new AcceleratedRingBuffers		(this.environment);
		this.buffers		= new ObjectLinkedOpenHashSet	<>	();
		this.activeBuilders	= new Object2ObjectOpenHashMap	<>	();
		this.activeLayers	= new IntAVLTreeSet					();

		this.currentBuffer	= this.ringBuffers.get(false);
		this.used			= false;
		this.barriers		= GL_SHADER_STORAGE_BARRIER_BIT;

		this.buffers.add(this.currentBuffer);
	}

	public void delete() {
		ringBuffers.delete();
	}

	@Override
	public AcceleratedBufferBuilder getBuffer(
			RenderType	renderType,
			Runnable	before,
			Runnable	after,
			int			layerIndex
	) {
		var layerKey	= new LayerKey					(layerIndex, renderType);
		var builder		= activeBuilders.get			(layerKey);
		var builders	= currentBuffer	.getBuilders	();
		var functions	= currentBuffer	.getFunctions	();
		var layers		= currentBuffer	.getLayers		();
		var function	= functions		.get			(layerIndex);
		var layer		= layers		.get			(layerIndex);

		if (builder != null) {
			function = builder	.getFunction();
			function			.addBefore	(before);
			function			.addAfter	(after);

			return builder;
		}

		var vertexBuffer	= currentBuffer.getVertexBuffer		();
		var varyingBuffer	= currentBuffer.getVaryingBuffer	();
		var elementSegment	= currentBuffer.getElementSegment	();

		if (vertexBuffer == null) {
			currentBuffer	= ringBuffers	.get				(true);
			builders		= currentBuffer	.getBuilders		();
			functions		= currentBuffer	.getFunctions		();
			layers			= currentBuffer	.getLayers			();
			function		= functions		.get				(layerIndex);
			layer			= layers		.get				(layerIndex);

			vertexBuffer	= currentBuffer	.getVertexBuffer	();
			varyingBuffer	= currentBuffer	.getVaryingBuffer	();
			elementSegment	= currentBuffer	.getElementSegment	();

			buffers.add(currentBuffer);
		}

		if (layer == null) {
			function	= new CustomLayerFunction			();
			layer 		= CoreFeature	.createLayerStorage	();
			layers						.put				(layerIndex, layer);
			functions					.put				(layerIndex, function);
		}

		builder = new AcceleratedBufferBuilder(
				vertexBuffer,
				varyingBuffer,
				elementSegment,
				currentBuffer,
				function,
				renderType
		);

		used = true;

		builders		.put		(layerKey, builder);
		function		.addBefore	(before);
		function		.addAfter	(after);
		activeBuilders	.put		(layerKey, builder);
		activeLayers	.add		(layerIndex);

		return builder;
	}

	public void prepareBuffers() {
		if (!used) {
			return;
		}

		for (var buffer : buffers) {
			var elementBuffer	= buffer.getElementBuffer	();
			var builders		= buffer.getBuilders		();
			var program			= glGetInteger				(GL_CURRENT_PROGRAM);

			if (builders.isEmpty()) {
				continue;
			}

			environment.getImmediateMeshBuffer				().bindBase	(GL_SHADER_STORAGE_BUFFER,	MeshUploadingProgramDispatcher.SPARSE_MESH_BUFFER_INDEX);
			environment.selectMeshUploadingProgramDispatcher().dispatch	(builders.values(),			buffer);
			environment.selectTransformProgramDispatcher	().dispatch	(builders.values());

			glMemoryBarrier(barriers);

			for (var layerKey : builders.keySet()) {
				var builder = builders.get(layerKey);

				if (builder.isEmpty()) {
					continue;
				}

				var drawContext		= buffer			.getDrawContext		();
				var elementSegment	= builder			.getElementSegment	();
				var renderType		= layerKey			.renderType			();
				var layer			= layerKey			.layer				();
				var drawType		= RenderTypeUtils	.getDrawType		(renderType);

				builder			.setOutdated();
				elementSegment	.setup		();

				drawContext.setupContext(
						builder,
						elementSegment,
						elementBuffer,
						renderType
				);

				buffer
						.getLayers	()
						.get		(layer)
						.get		(drawType)
						.add		(drawContext);

				barriers |= builder.getPolygonProgramDispatcher().dispatch(builder);
				barriers |= builder.getCullingProgramDispatcher().dispatch(builder);
			}

			glUseProgram(program);
		}
	}

	public void drawBuffers(LayerDrawType drawType) {
		if (!used) {
			return;
		}

		glMemoryBarrier(GL_VERTEX_ATTRIB_ARRAY_BARRIER_BIT
				|		GL_ELEMENT_ARRAY_BARRIER_BIT
				|		GL_COMMAND_BARRIER_BIT
		);

		for (		int layerIndex	: activeLayers) {
			for (	var buffer		: buffers) {
				var function = buffer.getFunctions	().getOrDefault(layerIndex, EmptyLayerFunction	.INSTANCE);
				var contexts = buffer.getLayers		().getOrDefault(layerIndex, EmptyLayerStorage	.INSTANCE).get(drawType);

				if (contexts.isEmpty()) {
					continue;
				}

				BufferUploader	.invalidate		();
				buffer			.bindDrawBuffers();
				contexts		.prepare		();
				function		.runBefore		();

				for (var drawContext : contexts) {
					var renderType = drawContext.getRenderType();

					renderType.setupRenderState();

					var mode	= renderType	.mode();
					var shader	= RenderSystem	.getShader();

					ShaderUtils.setDefaultUniforms(
							mode,
							shader,
							RenderSystem			.getModelViewMatrix	(),
							RenderSystem			.getProjectionMatrix(),
							Minecraft.getInstance()	.getWindow			()
					);

					shader		.apply				();
					drawContext	.drawElements		(mode);
					shader		.clear				();
					renderType	.clearRenderState	();
				}

				function.runAfter			();
				contexts.reset				();
				buffer	.unbindVertexArray	();
			}
		}
	}

	public void clearBuffers() {
		if (!used) {
			return;
		}

		for (var buffer : buffers) {
			buffer.reset		();
			buffer.setInFlight	();
		}

		used			= false;
		currentBuffer	= ringBuffers.get(false);

		activeBuilders	.clear	();
		activeLayers	.clear	();
		buffers			.clear	();
		buffers			.add	(currentBuffer);
	}
}
