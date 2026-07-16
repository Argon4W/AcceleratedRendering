package com.github.argon4w.acceleratedrendering.core.meshes.data;

import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.utils.Vertex;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class MeshData {

	private final VertexLayout	layout;
	private final int[]			data;

	private MeshData(VertexLayout layout, int[] data) {
		this.layout	= layout;
		this.data	= data;
	}

	public static Builder builder(VertexLayout layout) {
		return new Builder(layout);
	}

	public static class Builder {

		private final VertexLayout	layout;
		private final List<Vertex>	vertices;
		private final Vertex		scratch;

		public Builder(VertexLayout layout) {
			this.layout		= layout;
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

		public Builder setTexCoord(float u, float v) {
			scratch.getTexCoord().set(u, v);
			return this;
		}

		public Builder setLight(int u, int v) {
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

			var position	= vertex.getPosition();
			var texCoord	= vertex.getTexCoord();
			var color		= vertex.getColor	();
			var light		= vertex.getLight	();
			var normal		= vertex.getNormal	();

			position.x = posX;
			position.y = posY;
			position.z = posZ;

			texCoord.x = texU;
			texCoord.y = texV;

			color	.w = colorA;
			color	.x = colorR;
			color	.y = colorG;
			color	.z = colorB;

			light	.x = lightU;
			light	.y = lightV;

			normal	.x = normalX;
			normal	.y = normalY;
			normal	.z = normalZ;

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
				data[i * 8 + 4] = Float	.floatToRawIntBits	(vertex.getTexCoord().x);
				data[i * 8 + 5] = Float	.floatToRawIntBits	(vertex.getTexCoord().y);
				data[i * 8 + 6] = vertex.getPackedLight		();
				data[i * 8 + 7] = vertex.getPackedNormal	();
			}

			return new MeshData(layout, data);
		}
	}
}
