package com.github.argon4w.acceleratedrendering.features.modelparts.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
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
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@ExtensionMethod	(VertexConsumerExtension.class)
@Mixin				(ModelPart				.class)
public class ModelPartMixin implements IAcceleratedRenderer<Void> {

	@Shadow @Final public	List<ModelPart.Cube>		cubes;

	@Unique private final	Map<IBufferGraph,	IMesh>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final	Map<MeshData,		IMesh>	merges = new Object2ObjectOpenHashMap<>();

	@Inject(
			method		= "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V",
			at			= @At("HEAD"),
			cancellable	= true
	)
	public void renderFast(
			PoseStack		poseStack,
			VertexConsumer	buffer,
			int				packedLight,
			int				packedOverlay,
			float			red,
			float			green,
			float			blue,
			float			alpha,
			CallbackInfo	ci
	) {
		var extension = buffer.getAccelerated();

		if (			AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			ci.cancel();

			renderFast(
					(ModelPart) (Object) this,
					poseStack,
					extension,
					packedLight,
					packedOverlay,
					FastColor.ARGB32.color(
							(int) (alpha	* 255.0f),
							(int) (red		* 255.0f),
							(int) (green	* 255.0f),
							(int) (blue		* 255.0f)
					)
			);
		}
	}

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
			float			red,
			float			green,
			float			blue,
			float			alpha,
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
					FastColor.ARGB32.color(
							(int) (alpha	* 255.0f),
							(int) (red		* 255.0f),
							(int) (green	* 255.0f),
							(int) (blue		* 255.0f)
					)
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

					meshBuilder.vertex(
							vertexPosition.x / 16.0f,
							vertexPosition.y / 16.0f,
							vertexPosition.z / 16.0f,
							1.0f,
							1.0f,
							1.0f,
							1.0f,
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
			buffer.close();
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
	@SuppressWarnings("unchecked")
	private static void renderFast(
			ModelPart					modelPart,
			PoseStack					poseStack,
			IAcceleratedVertexConsumer	extension,
			int							packedLight,
			int							packedOverlay,
			int							packedColor
	) {
		if (!modelPart.visible) {
			return;
		}

		if (		modelPart.cubes		.isEmpty()
				&&	modelPart.children	.isEmpty()
		) {
			return;
		}

		poseStack.pushPose();

		modelPart.translateAndRotate(poseStack);

		if (!modelPart.skipDraw) {
			var last = poseStack.last();

			extension.doRender(
					(IAcceleratedRenderer<Void>) (Object) modelPart,
					null,
					last.pose	(),
					last.normal	(),
					packedLight,
					packedOverlay,
					packedColor
			);
		}

		for(var child : modelPart.children.values()) {
			renderFast(
					child,
					poseStack,
					extension,
					packedLight,
					packedOverlay,
					packedColor
			);
		}

		poseStack.popPose();
	}
}
