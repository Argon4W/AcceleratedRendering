package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class CullerUtils {

    public static boolean shouldCull(ModelPart.Vertex[] vertices, NativeImage image) {
        if (image == null) {
            return false;
        }

        float minU = 1.0f;
        float minV = 1.0f;

        float maxU = 0.0f;
        float maxV = 0.0f;

        if (vertices.length == 4) {
            Vector3f vertex0 = new Vector3f(vertices[0].pos());
            Vector3f vector1 = new Vector3f(vertices[1].pos()).sub(vertex0);
            Vector3f vector2 = new Vector3f(vertices[2].pos()).sub(vertex0);
            Vector3f vector3 = new Vector3f(vertices[3].pos()).sub(vertex0);

            float length1 = vector1.cross(vector2).length();
            float length2 = vector1.cross(vector3).length();

            if (length1 == 0 && length2 == 0) {
                return true;
            }
        }

        for (ModelPart.Vertex vertex : vertices) {
            float u = vertex.u();
            float v = vertex.v();

            u = u < 0 ? 1.0f + u : u;
            v = v < 0 ? 1.0f + v : v;

            minU = Math.min(minU, u);
            minV = Math.min(minV, v);
            maxU = Math.max(maxU, u);
            maxV = Math.max(maxV, v);
        }

        int minX = Math.max(0, Mth.floor(minU * image.getWidth()));
        int minY = Math.max(0, Mth.floor(minV * image.getHeight()));

        int maxX = Math.min(image.getWidth(), Mth.ceil(maxU * image.getWidth()));
        int maxY = Math.min(image.getHeight(), Mth.ceil(maxV * image.getHeight()));

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                if ((image.getPixel(x, y) & 0xFF) != 0) {
                    return false;
                }
            }
        }

        return true;
    }
}
