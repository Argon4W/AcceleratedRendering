package com.github.argon4w.acceleratedrendering.features.touhoulittlemaid.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.render.built.GeoMesh;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;

import java.util.Map;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(GeoBone				.class)
public class GeoBoneMixin implements IAcceleratedRenderer<Void> {

	@Shadow(remap = false) @Final private	GeoMesh						cubes;

	@Unique private final					Map<IBufferGraph,	IMesh>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final					Map<MeshData,		IMesh>	merges = new Object2ObjectOpenHashMap<>();

	@Unique
	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Void			context,
			Matrix4f		transformMatrix,
			Matrix3f		normalMatrix,
			int				light,
			int				overlay,
			int				color
	) {
		var extension	= vertexConsumer.getAccelerated	();
		var mesh		= meshes		.get			(extension);

		extension.beginTransform(transformMatrix, normalMatrix);

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

		var meshCollector	= CoreFeature	.createMeshCollector(extension);
		var meshBuilder		= extension		.decorate			(meshCollector);

		for(int i = 0; i < cubes.getCubeCount(); ++i) {
			var deltaX			= new Vector3f	(cubes.dx(i));
			var deltaY			= new Vector3f	(cubes.dy(i));
			var deltaZ			= new Vector3f	(cubes.dz(i));

			var p000			= new Vector3f	(cubes.position(i));
			var p100			= p000	.add	(deltaX, new Vector3f());
			var p110			= p100	.add	(deltaY, new Vector3f());
			var p010			= p000	.add	(deltaY, new Vector3f());
			var p001			= p000	.add	(deltaZ, new Vector3f());
			var p101			= p100	.add	(deltaZ, new Vector3f());
			var p111			= p110	.add	(deltaZ, new Vector3f());
			var p011			= p010	.add	(deltaZ, new Vector3f());
			
			var positiveNormalZ	= deltaX.cross	(deltaY, new Vector3f()).normalize();
			var positiveNormalX	= deltaY.cross	(deltaZ, new Vector3f()).normalize();
			var positiveNormalY	= deltaZ.cross	(deltaX, new Vector3f()).normalize();
			var faces			= cubes	.faces	(i);
			var mirrored		= (faces & 64) != 0;

			if (mirrored) {
				positiveNormalX.mul(-1.0F);
				positiveNormalY.mul(-1.0F);
				positiveNormalZ.mul(-1.0F);
			}

			var negativeNormalX	= positiveNormalX.negate(new Vector3f());
			var negativeNormalY	= positiveNormalY.negate(new Vector3f());
			var negativeNormalZ	= positiveNormalZ.negate(new Vector3f());

			var positions		= new Vector3f[][] {
					{p101, p001, p000, p100},
					{p110, p010, p011, p111},
					{p100, p000, p010, p110},
					{p001, p101, p111, p011},
					{p000, p001, p011, p010},
					{p101, p100, p110, p111}
			};

			var texCoords		= new float[][] {
					{cubes.downU0	(i), cubes.downU1	(i), cubes.downV0	(i), cubes.downV1	(i)},
					{cubes.upU0		(i), cubes.upU1		(i), cubes.upV0		(i), cubes.upV1		(i)},
					{cubes.northU0	(i), cubes.northU1	(i), cubes.northV0	(i), cubes.northV1	(i)},
					{cubes.southU0	(i), cubes.southU1	(i), cubes.southV0	(i), cubes.southV1	(i)},
					{cubes.westU0	(i), cubes.westU1	(i), cubes.westV0	(i), cubes.westV1	(i)},
					{cubes.eastU0	(i), cubes.eastU1	(i), cubes.eastV0	(i), cubes.eastV1	(i)},
			};

			var texOrders		= new Vector2i[] {
					new Vector2i(0, 3),
					new Vector2i(1, 3),
					new Vector2i(1, 2),
					new Vector2i(0, 2)
			};

			var normals			= new Vector3f[] {
					negativeNormalY,
					positiveNormalY,
					negativeNormalZ,
					positiveNormalZ,
					negativeNormalX,
					positiveNormalX,
			};

			for (var j = 0; j < 6; j ++) {
				if ((faces & (1 << j)) != 0) {
					for (var k = 0; k < 4; k ++) {
						var position	= positions	[j][k];
						var texCoord	= texCoords	[j];
						var texOrder	= texOrders	[k];
						var normal		= normals	[j];

						meshBuilder.vertex(
								position.x,
								position.y,
								position.z,
								1.0f,
								1.0f,
								1.0f,
								1.0f,
								texCoord[texOrder.x],
								texCoord[texOrder.y],
								overlay,
								0,
								normal.x,
								normal.y,
								normal.z
						);
					}
				}
			}
		}

		meshCollector.flush();

		var data	= meshCollector	.getData	();
		var buffer	= meshCollector	.getBuffer	();
		mesh		= merges		.get		(data);

		if (mesh != null) {
			buffer.close();
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
				color,
				light,
				overlay
		);

		extension.endTransform();
	}
}
