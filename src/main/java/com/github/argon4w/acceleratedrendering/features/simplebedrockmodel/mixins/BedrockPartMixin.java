package com.github.argon4w.acceleratedrendering.features.simplebedrockmodel.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IBufferGraph;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.VertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.renderers.IAcceleratedRenderer;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.CulledMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.MeshData;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockCube;
import com.github.tartaricacid.simplebedrockmodel.client.bedrock.model.BedrockPart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.FastColor;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Pseudo
@ExtensionMethod(VertexConsumerExtension.class)
@Mixin			(BedrockPart			.class)
public class BedrockPartMixin implements IAcceleratedRenderer<Void> {

	@Unique	private static	final	PoseStack.Pose				POSE			= new PoseStack().last();
	@Unique private static	final	Vector3f[]					FIXED_NORMALS	= {
			new Vector3f(-0.0f, -1.0f, -0.0f),
			new Vector3f(+0.0f, +1.0f, +0.0f),
			new Vector3f(-0.0f, -0.0f, -1.0f),
			new Vector3f(+0.0f, +0.0f, +1.0f),
			new Vector3f(-1.0f, -0.0f, -0.0f),
			new Vector3f(+1.0f, +0.0f, +0.0f)
	};

	@Shadow @Final public			ObjectList<BedrockCube>		cubes;

	@Unique private final			Map<IBufferGraph,	IMesh>	meshes = new Object2ObjectOpenHashMap<>();
	@Unique private final			Map<MeshData,		IMesh>	merges = new Object2ObjectOpenHashMap<>();

	@Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At("HEAD"), cancellable = true)
	public void renderFast(
			PoseStack		poseStack,
			VertexConsumer	consumer,
			int				lightmap,
			int				overlay,
			float			red,
			float			green,
			float			blue,
			float			alpha,
			CallbackInfo	ci
	) {
		var extension = consumer.getAccelerated();

		if (			AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			ci.cancel();

			renderFast(
					(BedrockPart) (Object) this,
					poseStack,
					extension,
					lightmap,
					overlay,
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
			PoseStack.Pose	pose,
			VertexConsumer	consumer,
			int				texU,
			int				texV,
			float			red,
			float			green,
			float			blue,
			float			alpha,
			CallbackInfo	ci
	) {
		var extension = consumer.getAccelerated();

		if (			AcceleratedEntityRenderingFeature	.isEnabled						()
				&&		AcceleratedEntityRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature							.isRenderingLevel				()
				||	(	CoreFeature							.isRenderingGui					()
				&&		AcceleratedEntityRenderingFeature	.shouldAccelerateInGui			()))
				&&		extension							.isAccelerated					()
		) {
			ci.cancel();

			extension.doRender(
					this,
					null,
					pose.pose	(),
					pose.normal	(),
					texU,
					texV,
					FastColor.ARGB32.color	(
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
			cube.compile(
					POSE,
					FIXED_NORMALS,
					meshBuilder,
					0,
					overlay,
					1.0f,
					1.0f,
					1.0f,
					1.0f
			);
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
	@SuppressWarnings("unchecked")
	private static void renderFast(
			BedrockPart					bedrockPart,
			PoseStack					poseStack,
			IAcceleratedVertexConsumer	extension,
			int							packedLight,
			int							packedOverlay,
			int							packedColor
	) {
		if (!bedrockPart.visible) {
			return;
		}

		var xNearZero = -1E-5F < bedrockPart.xScale && bedrockPart.xScale < 1E-5F;
		var yNearZero = -1E-5F < bedrockPart.yScale && bedrockPart.yScale < 1E-5F;
		var zNearZero = -1E-5F < bedrockPart.zScale && bedrockPart.zScale < 1E-5F;

		if ((xNearZero && yNearZero) || (xNearZero && zNearZero) || (yNearZero && zNearZero)) {
			return;
		}

		if (		bedrockPart.cubes	.isEmpty()
				&&	bedrockPart.children.isEmpty()
		) {
			return;
		}

		poseStack.pushPose();

		bedrockPart.translateAndRotateAndScale(poseStack);

		var last = poseStack.last();

		extension.doRender(
				(IAcceleratedRenderer<Void>) bedrockPart,
				null,
				last.pose	(),
				last.normal	(),
				bedrockPart.illuminated ? LightTexture.FULL_BRIGHT : packedLight,
				packedOverlay,
				packedColor
		);

		for(var child : bedrockPart.children) {
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
