package com.github.argon4w.acceleratedrendering.features.entities;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

@ExtensionMethod(VertexConsumerExtension.class)
public class AcceleratedEntityShadowRenderer implements IAcceleratedRenderer<AcceleratedEntityShadowRenderer.Context> {

	public static final AcceleratedEntityShadowRenderer INSTANCE = new AcceleratedEntityShadowRenderer();

	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Context			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer	.getAccelerated	();
		var levelReader	= context			.levelReader	();
		var chunkAccess	= context			.chunkAccess	();
		var blockPos	= context			.blockPos		();
		var center		= context			.center			();
		var size		= context			.size			();
		var weight		= context			.weight			();

		var belowPos	= context.blockPos().below			();
		var blockState	= chunkAccess		.getBlockState	(belowPos);

		if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
			return;
		}

		var levelBrightness = levelReader.getMaxLocalRawBrightness(blockPos);

		if (levelBrightness <= 3) {
			return;
		}

		if (!blockState.isCollisionShapeFullBlock(chunkAccess, belowPos)) {
			return;
		}

		var voxelShape = blockState.getShape(chunkAccess, belowPos);

		if (voxelShape.isEmpty()) {
			return;
		}

		var dimensionBrightness	= LightTexture.getBrightness(levelReader.dimensionType(), levelBrightness);
		var shadowTransparency	= weight * 0.5f * dimensionBrightness * 255.0f;

		if (shadowTransparency < 0.0f) {
			return;
		}

		if (shadowTransparency > 255.0f) {
			shadowTransparency = 255.0f;
		}

		var bounds	= voxelShape.bounds	();
		var minX	= blockPos	.getX	() + (float) bounds.minX;
		var maxX	= blockPos	.getX	() + (float) bounds.maxX;
		var minY	= blockPos	.getY	() + (float) bounds.minY;
		var minZ	= blockPos	.getZ	() + (float) bounds.minZ;
		var maxZ	= blockPos	.getZ	() + (float) bounds.maxZ;

		var minPosX = minX - center.x;
		var maxPosX = maxX - center.x;
		var minPosY = minY - center.y;
		var minPosZ = minZ - center.z;
		var maxPosZ = maxZ - center.z;

		var u0 = -minPosX / 2.0f / size + 0.5f;
		var u1 = -maxPosX / 2.0f / size + 0.5f;
		var v0 = -minPosZ / 2.0f / size + 0.5f;
		var v1 = -maxPosZ / 2.0f / size + 0.5f;

		extension.beginTransform(transform, normal);

		var positions = new Vector3f[] {
				new Vector3f(minPosX, minPosY, minPosZ),
				new Vector3f(minPosX, minPosY, maxPosZ),
				new Vector3f(maxPosX, minPosY, maxPosZ),
				new Vector3f(maxPosX, minPosY, minPosZ),
		};

		var texCoords = new Vector2f[] {
				new Vector2f(u0, v0),
				new Vector2f(u0, v1),
				new Vector2f(u1, v1),
				new Vector2f(u1, v0),
		};

		for (var i = 0; i < 4; i ++) {
			var position = positions[i];
			var texCoord = texCoords[i];

			vertexConsumer.vertex(
					position.x,
					position.y,
					position.z,
					FastColor.ARGB32.red	(color)	/ 255.0f,
					FastColor.ARGB32.green	(color)	/ 255.0f,
					FastColor.ARGB32.blue	(color)	/ 255.0f,
					shadowTransparency				/ 255.0f,
					texCoord.x,
					texCoord.y,
					overlay,
					light,
					0.0f,
					1.0f,
					0.0f
			);
		}

		extension.endTransform();
	}

	public static Context context(
			LevelReader	levelReader,
			ChunkAccess	chunkAccess,
			BlockPos	blockPos,
			Vector3f	center,
			float		size,
			float		weight
	) {
		return new Context(
				levelReader,
				chunkAccess,
				blockPos,
				center,
				size,
				weight
		);
	}

	public record Context(
			LevelReader	levelReader,
			ChunkAccess	chunkAccess,
			BlockPos	blockPos,
			Vector3f	center,
			float		size,
			float		weight
	) {

	}
}
