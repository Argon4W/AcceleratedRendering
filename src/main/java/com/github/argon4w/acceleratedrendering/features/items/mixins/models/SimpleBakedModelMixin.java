package com.github.argon4w.acceleratedrendering.features.items.mixins.models;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.core.utils.DirectionUtils;
import com.github.argon4w.acceleratedrendering.core.utils.FastColorUtils;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.items.IAcceleratedBakedModel;
import com.github.argon4w.acceleratedrendering.features.items.colors.FixedColors;
import com.github.argon4w.acceleratedrendering.features.items.colors.ItemLayerColors;
import com.github.argon4w.acceleratedrendering.features.items.contexts.AcceleratedModelRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Map;

@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(SimpleBakedModel		.class)
public abstract class SimpleBakedModelMixin implements IAcceleratedBakedModel, IAcceleratedRenderer<AcceleratedModelRenderContext> {

	@Shadow public abstract List<BakedQuad> getQuads(BlockState pState, Direction pDirection, RandomSource pRandom);

	@Unique private final Map<IBufferGraph,	Int2ObjectMap<IMesh>>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final Map<MeshData,		IMesh>					merges = new Object2ObjectOpenHashMap<>();

	@Unique
	@Override
	public void renderItemFast(
			ItemStack					itemStack,
			RandomSource				random,
			PoseStack.Pose				pose,
			IAcceleratedVertexConsumer	extension,
			int							combinedLight,
			int							combinedOverlay
	) {
		extension.doRender(
				this,
				new AcceleratedModelRenderContext(random, new ItemLayerColors(itemStack)),
				pose.pose	(),
				pose.normal	(),
				combinedLight,
				combinedOverlay,
				-1
		);
	}

	@Override
	public void renderBlockFast(
			BlockState					state,
			RandomSource				random,
			PoseStack.Pose				pose,
			IAcceleratedVertexConsumer	extension,
			int							combinedLight,
			int							combinedOverlay,
			int							color,
			ModelData					data,
			RenderType					renderType
	) {
		extension.doRender(
				this,
				new AcceleratedModelRenderContext(random, new FixedColors(color)),
				pose.pose	(),
				pose.normal	(),
				combinedLight,
				combinedOverlay,
				-1
		);
	}

	@Unique
	@Override
	public void render(
			VertexConsumer					vertexConsumer,
			AcceleratedModelRenderContext	context,
			Matrix4f						transform,
			Matrix3f						normal,
			int								light,
			int								overlay,
			int								color
	) {
		var extension		= vertexConsumer.getAccelerated	();
		var randomSource	= context		.randomSource	();
		var layerColors		= context		.layerColors	();
		var layers			= meshes		.get			(extension);

		extension.beginTransform(transform, normal);

		if (layers != null) {
			for (int layer : layers.keySet()) {
				var mesh = layers.get(layer);

				mesh.write(
						extension,
						getCustomColor(layer, layerColors.getColor(layer)),
						light,
						overlay
				);
			}

			extension.endTransform();
			return;
		}

		var culledMeshCollectors	= new Int2ObjectOpenHashMap	<CulledMeshCollector>	();
		layers 						= new Int2ObjectAVLTreeMap	<>						();

		meshes.put(extension, layers);

		for (var direction : DirectionUtils.FULL) {
			for (var quad : getQuads(
					null,
					direction,
					randomSource
			)) {
				var culledMeshCollector = culledMeshCollectors.get(quad.getTintIndex());

				if (culledMeshCollector == null) {
					culledMeshCollector = new CulledMeshCollector	(extension);
					culledMeshCollectors.put						(quad.getTintIndex(), culledMeshCollector);
				}

				var meshBuilder = extension	.decorate	(culledMeshCollector);
				var data		= quad		.getVertices();

				for (int i = 0; i < data.length / 8; i++) {
					var vertexOffset	= i				* IQuadTransformer.STRIDE;
					var posOffset		= vertexOffset	+ IQuadTransformer.POSITION;
					var colorOffset		= vertexOffset	+ IQuadTransformer.COLOR;
					var uv0Offset		= vertexOffset	+ IQuadTransformer.UV0;
					var uv2Offset		= vertexOffset	+ IQuadTransformer.UV2;
					var normalOffset	= vertexOffset	+ IQuadTransformer.NORMAL;
					var packedNormal	= data[normalOffset];

					meshBuilder.addVertex(
							Float			.intBitsToFloat	(data[posOffset + 0]),
							Float			.intBitsToFloat	(data[posOffset + 1]),
							Float			.intBitsToFloat	(data[posOffset + 2]),
							FastColorUtils	.convert		(data[colorOffset]),
							Float			.intBitsToFloat	(data[uv0Offset + 0]),
							Float			.intBitsToFloat	(data[uv0Offset + 1]),
							-1,
							data[uv2Offset],
							((byte) (	packedNormal		& 0xFF)) / 127.0f,
							((byte) ((	packedNormal >> 8)	& 0xFF)) / 127.0f,
							((byte) ((	packedNormal >> 16)	& 0xFF)) / 127.0f
					);
				}
			}
		}

		for (int layer : culledMeshCollectors.keySet()) {
			var culledMeshCollector = culledMeshCollectors.get(layer);

			culledMeshCollector.flush();

			var data	= culledMeshCollector	.getData	();
			var buffer	= culledMeshCollector	.getBuffer	();
			var mesh	= merges				.get		(data);

			if (mesh != null) {
				buffer.discard	();
				buffer.close	();
			} else {
				mesh = AcceleratedEntityRenderingFeature
						.getMeshType()
						.getBuilder	()
						.build		(culledMeshCollector);
			}

			layers	.put	(layer, mesh);
			merges	.put	(data,	mesh);
			mesh	.write	(
					extension,
					getCustomColor(layer, layerColors.getColor(layer)),
					light,
					overlay
			);
		}

		extension.endTransform();
	}


	@Unique
	@Override
	public boolean isAccelerated() {
		return true;
	}

	@Unique
	@Override
	public boolean isAcceleratedInHand() {
		return false;
	}

	@Unique
	@Override
	public boolean isAcceleratedInGui() {
		return false;
	}

	@Unique
	@Override
	public int getCustomColor(int layer, int color) {
		return layer == -1 ? -1 : color;
	}
}
