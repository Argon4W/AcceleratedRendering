package com.github.argon4w.acceleratedrendering.features.geckolib.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.*;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.util.RenderUtil;

import java.util.List;
import java.util.Map;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(GeoBone				.class)
public class GeoBoneMixin implements IAcceleratedRenderer<Void> {

	@Shadow @Final private	List<GeoCube>				cubes;

	@Unique private	final	Map<IBufferGraph,	IMesh>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final	Map<MeshData,		IMesh>	merges = new Object2ObjectOpenHashMap<>();

	@Override
	public void render(
			VertexConsumer vertexConsumer,
			Void			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
		var mesh		= meshes		.get			(extension);

		extension.beginTransform(transform, normal);

		if (mesh != null) {
			mesh.write(
					extension,
					color,
					light,
					overlay
			);

			extension.endTransform();
			return;
		}

		var culledMeshCollector	= new CulledMeshCollector	(extension);
		var meshBuilder			= extension.decorate		(culledMeshCollector);

		for (GeoCube cube : cubes) {
			var poseStack = new PoseStack();

			RenderUtil.translateToPivotPoint		(poseStack, cube);
			RenderUtil.rotateMatrixAroundCube		(poseStack, cube);
			RenderUtil.translateAwayFromPivotPoint	(poseStack, cube);

			var pose			= poseStack	.last	();
			var cubeTransform	= pose		.pose	();
			var cubeNormal		= pose		.normal	();

			for (var quad : cube.quads()) {
				if (quad != null) {
					var polygonNormal = cubeNormal.transform(new Vector3f(quad.normal()));

					for (var vertex : quad.vertices()) {
						var vertexPosition = cubeTransform.transform(new Vector4f(vertex.position(), 1.0f));

						meshBuilder.addVertex(
								vertexPosition.x,
								vertexPosition.y,
								vertexPosition.z,
								-1,
								vertex.texU(),
								vertex.texV(),
								overlay,
								0,
								polygonNormal.x,
								polygonNormal.y,
								polygonNormal.z
						);
					}
				}
			}
		}

		culledMeshCollector.flush();

		var data	= culledMeshCollector	.getData	();
		var buffer	= culledMeshCollector	.getBuffer	();
		mesh		= merges				.get		(data);

		if (mesh != null) {
			buffer.discard	();
			buffer.close	();
		} else {
			mesh = AcceleratedEntityRenderingFeature
					.getMeshType()
					.getBuilder	()
					.build		(culledMeshCollector);
		}

		meshes	.put	(extension, mesh);
		merges	.put	(data,		mesh);
		mesh	.write	(
				extension,
				color,
				light,
				overlay
		);

		extension.endTransform();
	}
}
