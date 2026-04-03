package com.github.argon4w.acceleratedrendering.features.emf.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.IMeshData;
import com.github.argon4w.acceleratedrendering.features.emf.IEMFModelVariant;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.modelparts.mixins.ModelPartMixin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_model_features.models.parts.EMFModelPart;

import java.util.Map;

@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(EMFModelPart			.class)
public class EMFModelPartMixin extends ModelPartMixin implements IEMFModelVariant {

	@Unique private final	Int2ReferenceMap<Map<IBufferGraph,	IMesh>>	emfMeshes	= new Int2ReferenceOpenHashMap<>();
	@Unique private final	Int2ReferenceMap<Map<IMeshData,		IMesh>>	emfMerges	= new Int2ReferenceOpenHashMap<>();
	@Unique private			int											emfVariant	= Integer.MIN_VALUE;

	@Inject(
			method		= "compile",
			at			= @At("HEAD"),
			cancellable	= true
	)
	public void compileFast(
			PoseStack.Pose	pPose,
			VertexConsumer	pBuffer,
			int				pPackedLight,
			int				pPackedOverlay,
			int				pColor,
			CallbackInfo	ci
	) {
		var extension = pBuffer.getAccelerated();

		if (			AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			ci			.cancel		();
			extension	.doRender	(
					this,
					null,
					pPose.pose	(),
					pPose.normal(),
					pPackedLight,
					pPackedOverlay,
					pColor
			);
		}
	}

	@Unique
	@Override
	public void render(
			VertexConsumer	vertexConsumer,
			Void			context,
			Matrix4f		transform,
			Matrix3f		normal,
			int				light,
			int				overlay,
			int				color
	) {
		var meshes = emfMeshes.get(emfVariant);
		var merges = emfMerges.get(emfVariant);

		if (		meshes == null
				||	merges == null
		) {
			meshes = new Object2ObjectOpenHashMap<>();
			merges = new Object2ObjectOpenHashMap<>();

			emfMeshes.put(emfVariant, meshes);
			emfMerges.put(emfVariant, merges);
		}

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

		for (var cube : cubes) {
			for (var polygon : cube.polygons) {
				var polygonNormal = polygon.normal;

				for (var vertex : polygon.vertices) {
					var vertexPosition = vertex.pos;

					meshBuilder.addVertex(
							vertexPosition.x / 16.0f,
							vertexPosition.y / 16.0f,
							vertexPosition.z / 16.0f,
							-1,
							vertex.u,
							vertex.v,
							overlay,
							0,
							polygonNormal.x,
							polygonNormal.y,
							polygonNormal.z
					);
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

	@Unique
	@Override
	public void setCurrentVariant(int variant) {
		emfVariant = variant;
	}
}
