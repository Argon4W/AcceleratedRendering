package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.IMeshCollector;
import com.github.argon4w.acceleratedrendering.core.utils.ByteBufferBuilder;
import com.github.argon4w.acceleratedrendering.core.meshes.data.cache.MeshDataCaches;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import lombok.AllArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Set;

@AllArgsConstructor
public class ClientMesh implements IMesh {

	private final int			size;
	private final ByteBuffer	vertexBuffer;

	@Override
	public void write(
			IAcceleratedVertexConsumer	extension,
			int							color,
			int							light,
			int							overlay
	) {
		extension.addClientMesh(
				vertexBuffer,
				size,
				color,
				light,
				overlay
		);
	}

	public static class Builder implements IMesh.Builder {

		public static	final Builder					INSTANCE = new Builder();

		private			final Set<ByteBufferBuilder>	builders;

		private Builder() {
			this.builders = new ReferenceLinkedOpenHashSet<>();
		}

		@Override
		public IMesh build(IMeshCollector collector) {
			var vertexCount = collector.getVertexCount();

			if (vertexCount == 0) {
				return EmptyMesh.INSTANCE;
			}

			var builder		= collector				.getBuffer		();
			var layout		= collector				.getLayout		();
			var data		= collector				.getData		();
			var mesh		= MeshDataCaches.CLIENT	.get			(layout, data);

			if (mesh != null) {
				builder.close();
				return mesh;
			}

			var result = builder.build();

			if (result == null) {
				builder.close();
				return EmptyMesh.INSTANCE;
			}

			builders.add(builder);

			mesh = new ClientMesh(vertexCount, result.byteBuffer());

			MeshDataCaches.CLIENT.set(
					layout,
					data,
					mesh
			);

			return mesh;
		}

		@Override
		public IMesh build(IMeshCollector collector, boolean forceDense) {
			return build(collector);
		}

		@Override
		public void delete() {
			for (var builder : builders) {
				builder.close();
			}
		}
	}
}
