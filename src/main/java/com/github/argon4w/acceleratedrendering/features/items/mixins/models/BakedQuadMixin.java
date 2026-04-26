package com.github.argon4w.acceleratedrendering.features.items.mixins.models;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.core.utils.FastColorUtils;
import com.github.argon4w.acceleratedrendering.core.utils.IntArrayHashStrategy;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.IAcceleratedBakedQuad;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Map;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements IAcceleratedBakedQuad {

	@Unique private static final	Map<int[], Map<IBufferGraph,	IMesh>>	MESHES = new Object2ObjectOpenCustomHashMap<>(IntArrayHashStrategy.INSTANCE);
	@Unique private static final	Map<int[], Map<MeshData,		IMesh>>	MERGES = new Object2ObjectOpenCustomHashMap<>(IntArrayHashStrategy.INSTANCE);

	@Shadow @Final protected		int[]									vertices;

	@Shadow public abstract			boolean									isTinted();

	@Unique
	@Override
	public void renderFast(
			Matrix4f					transform,
			Matrix3f					normal,
			IAcceleratedVertexConsumer	extension,
			int							combinedLight,
			int							combinedOverlay,
			int							color
	) {
		var meshes = MESHES.get(vertices);
		var merges = MERGES.get(vertices);

		if (meshes == null) {
			meshes = new Object2ObjectOpenHashMap<>	();
			merges = new Object2ObjectOpenHashMap<>	();
			MESHES.put								(vertices, meshes);
			MERGES.put								(vertices, merges);
		}

		var mesh = meshes.get(extension);

		if (mesh != null) {
			mesh.write(
					extension,
					getCustomColor(color),
					combinedLight,
					combinedOverlay
			);
			return;
		}

		var meshCollector	= CoreFeature	.createMeshCollector(extension);
		var meshBuilder		= extension		.decorate			(meshCollector);

		for (var i = 0; i < vertices.length / 8; i++) {
			var vertexOffset	= i				* IQuadTransformer.STRIDE;
			var posOffset		= vertexOffset	+ IQuadTransformer.POSITION;
			var colorOffset		= vertexOffset	+ IQuadTransformer.COLOR;
			var uv0Offset		= vertexOffset	+ IQuadTransformer.UV0;
			var uv2Offset		= vertexOffset	+ IQuadTransformer.UV2;
			var normalOffset	= vertexOffset	+ IQuadTransformer.NORMAL;
			var packedNormal	= vertices[normalOffset];

			meshBuilder.addVertex(
					Float			.intBitsToFloat	(vertices[posOffset + 0]),
					Float			.intBitsToFloat	(vertices[posOffset + 1]),
					Float			.intBitsToFloat	(vertices[posOffset + 2]),
					FastColorUtils	.convert		(vertices[colorOffset]),
					Float			.intBitsToFloat	(vertices[uv0Offset + 0]),
					Float			.intBitsToFloat	(vertices[uv0Offset + 1]),
					combinedOverlay,
					vertices[uv2Offset],
					((byte) (	packedNormal		& 0xFF)) / 127.0f,
					((byte) ((	packedNormal >> 8)	& 0xFF)) / 127.0f,
					((byte) ((	packedNormal >> 16)	& 0xFF)) / 127.0f
			);
		}

		meshCollector.flush();

		var data	= meshCollector	.getData	();
		var buffer	= meshCollector	.getBuffer	();
		mesh		= merges		.get		(data);

		if (mesh != null) {
			buffer.discard	();
			buffer.close	();
		} else {
			mesh = AcceleratedEntityRenderingFeature
					.getMeshType()
					.getBuilder	()
					.build		(meshCollector);
		}

		meshes	.put	(extension, mesh);
		merges	.put	(data,		mesh);
		mesh	.write	(
				extension,
				getCustomColor(color),
				combinedLight,
				combinedOverlay
		);
	}

	@Unique
	@Override
	public int getCustomColor(int color) {
		return isTinted() ? color : -1;
	}
}
