package com.github.argon4w.acceleratedrendering.features.modelpart.mixins;

import com.github.argon4w.acceleratedrendering.core.buffers.builders.IVertexConsumerExtension;
import com.github.argon4w.acceleratedrendering.core.meshes.IMesh;
import com.github.argon4w.acceleratedrendering.core.meshes.MeshCollector;
import com.github.argon4w.acceleratedrendering.core.utils.CullerUtils;
import com.github.argon4w.acceleratedrendering.core.utils.TextureUtils;
import com.github.argon4w.acceleratedrendering.features.entities.AcceleratedEntityRenderingFeature;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin {

    @Shadow @Final private List<ModelPart.Cube> cubes;

    @Unique private final Map<RenderType, IMesh> meshes = new Object2ObjectOpenHashMap<>();

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    public void compile(
            PoseStack.Pose pPose,
            VertexConsumer pBuffer,
            int pPackedLight,
            int pPackedOverlay,
            int pColor,
            CallbackInfo ci
    ) {
        IVertexConsumerExtension extension = (IVertexConsumerExtension) pBuffer;

        if (!AcceleratedEntityRenderingFeature.isEnabled()) {
            return;
        }

        if (!AcceleratedEntityRenderingFeature.shouldUseAcceleratedPipeline()) {
            return;
        }

        if (!extension.supportAcceleratedRendering()) {
            return;
        }

        ci.cancel();
        extension.beginTransform(pPose);

        for (RenderType renderType : extension.getRenderTypes()) {
            IMesh mesh = meshes.get(renderType);

            if (mesh != null) {
                mesh.write(
                        extension,
                        pColor,
                        pPackedLight,
                        pPackedOverlay
                );
                continue;
            }

            IMesh.Builder builder = AcceleratedEntityRenderingFeature.getMeshBuilder();
            MeshCollector meshCollector = builder.newMeshCollector(renderType);
            NativeImage image = TextureUtils.downloadTexture(renderType);

            for (ModelPart.Cube cube : cubes) {
                for (ModelPart.Polygon polygon : cube.polygons) {
                    Vector3f normal = polygon.normal();

                    if (CullerUtils.shouldCull(polygon.vertices(), image)) {
                        continue;
                    }

                    for (ModelPart.Vertex vertex : polygon.vertices()) {
                        meshCollector.addVertex(
                                vertex.pos().x / 16.0f,
                                vertex.pos().y / 16.0f,
                                vertex.pos().z / 16.0f,
                                pColor,
                                vertex.u(),
                                vertex.v(),
                                pPackedOverlay,
                                pPackedLight,
                                normal.x,
                                normal.y,
                                normal.z
                        );
                    }
                }
            }

            mesh = builder.build(meshCollector);
            meshes.put(renderType, mesh);
            mesh.write(
                    extension,
                    pColor,
                    pPackedLight,
                    pPackedOverlay
            );

            if (image != null) {
                image.close();
            }
        }

        extension.endTransform();
    }
}
