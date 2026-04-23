package com.github.argon4w.acceleratedrendering.core.meshes.data;

import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.EqualsAndHashCode;

import java.util.List;

//@EqualsAndHashCode
public class MeshData {

	private final int[] data;

	private MeshData(int[] data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private final List<Vertex>	vertices;
		private final Vertex		scratch;

		public Builder() {
			this.vertices	= new ReferenceArrayList<>	();
			this.scratch	= new Vertex				();
		}

		public Builder setPosition(
				float positionX,
				float positionY,
				float positionZ
		) {
			scratch.getPosition().set(
					positionX,
					positionY,
					positionZ
			);

			return this;
		}

		public Builder setColor(
				int colorRed,
				int colorGreen,
				int colorBlue,
				int colorAlpha
		) {
			scratch.getColor().set(
					colorRed,
					colorGreen,
					colorBlue,
					colorAlpha
			);

			return this;
		}

		public Builder setNormal(
				float normalX,
				float normalY,
				float normalZ
		) {
			scratch.getNormal().set(
					normalX,
					normalY,
					normalZ
			);

			return this;
		}

		public Builder setUv(float u, float v) {
			scratch.getUv().set(u, v);
			return this;
		}

		public Builder setUv2(int u, int v) {
			scratch.getLight().set(u, v);
			return this;
		}

		public Builder addVertex() {
			vertices.add(new Vertex(scratch));
			return this;
		}

		public Builder addVertex(
				float	posX,
				float	posY,
				float	posZ,
				float	texU,
				float	texV,
				int		colorR,
				int		colorG,
				int		colorB,
				int		colorA,
				int		lightU,
				int		lightV,
				float	normalX,
				float	normalY,
				float	normalZ
		) {
			var vertex = new Vertex();

			vertex.getPosition().x = posX;
			vertex.getPosition().y = posY;
			vertex.getPosition().z = posZ;

			vertex.getUv()		.x = texU;
			vertex.getUv()		.y = texV;

			vertex.getColor()	.w = colorA;
			vertex.getColor()	.x = colorR;
			vertex.getColor()	.y = colorG;
			vertex.getColor()	.z = colorB;

			vertex.getLight()	.x = lightU;
			vertex.getLight()	.y = lightV;

			vertex.getNormal()	.x = normalX;
			vertex.getNormal()	.y = normalY;
			vertex.getNormal()	.z = normalZ;

			vertices.add(vertex);

			return this;
		}

		public MeshData build() {
			var data = new int[vertices.size() * 8];

			for (var i = 0; i < vertices.size(); i++) {
				var vertex = vertices.get(i);

				data[i * 8 + 0] = Float	.floatToRawIntBits	(vertex.getPosition().x);
				data[i * 8 + 1] = Float	.floatToRawIntBits	(vertex.getPosition().y);
				data[i * 8 + 2] = Float	.floatToRawIntBits	(vertex.getPosition().z);
				data[i * 8 + 3] = vertex.getPackedColor		();
				data[i * 8 + 4] = Float	.floatToRawIntBits	(vertex.getUv().x);
				data[i * 8 + 5] = Float	.floatToRawIntBits	(vertex.getUv().y);
				data[i * 8 + 6] = vertex.getPackedLight		();
				data[i * 8 + 7] = vertex.getPackedNormal	();
			}

			return new MeshData(data);
		}
	}
}
