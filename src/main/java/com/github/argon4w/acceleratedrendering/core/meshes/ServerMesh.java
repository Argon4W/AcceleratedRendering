package com.github.argon4w.acceleratedrendering.core.meshes;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.backends.GLConstants;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.EmptyServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.IServerBuffer;
import com.github.argon4w.acceleratedrendering.core.backends.buffers.MappedBuffer;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.IAcceleratedVertexConsumer;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.VertexLayout;
import com.github.argon4w.acceleratedrendering.core.meshes.collectors.IMeshCollector;
import com.github.argon4w.acceleratedrendering.core.meshes.data.cache.MeshDataCaches;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import org.lwjgl.system.MemoryUtil;

import java.util.List;

public record ServerMesh(
		int				size,
		long			offset,
		boolean			forceDense,
		IServerBuffer	meshBuffer
) implements IMesh {

	@Override
	public void write(
			IAcceleratedVertexConsumer	extension,
			int							color,
			int							light,
			int							overlay
	) {
		extension.addServerMesh(
				this,
				color,
				light,
				overlay
		);
	}

	public static class Builder implements IMesh.Builder {

		public static final Builder													INSTANCE;
		public static final Reference2ObjectMap<VertexLayout, List<IServerBuffer>>	BUFFERS;

		static {
			INSTANCE	= new Builder						();
			BUFFERS		= new Reference2ObjectOpenHashMap<>	();
			BUFFERS.defaultReturnValue						(ReferenceLists.singleton(EmptyServerBuffer.INSTANCE));
		}

		private Builder() {

		}

		@Override
		public IMesh build(IMeshCollector collector, boolean forceDense) {
			var vertexCount	= collector.getVertexCount	();

			if (vertexCount == 0) {
				return EmptyMesh.INSTANCE;
			}

			var builder		= collector				.getBuffer		();
			var layout		= collector				.getLayout		();
			var data		= collector				.getData		();
			var mesh		= MeshDataCaches.SERVER	.get			(layout, data);

			if (mesh != null) {
				builder.close();
				return mesh;
			}

			var builderResult = builder.build();

			if (builderResult == null) {
				builder.close();
				return EmptyMesh.INSTANCE;
			}

			var byteBuffer		= builderResult	.byteBuffer		();
			var capacity		= byteBuffer	.capacity		();
			var meshBuffers		= BUFFERS		.getOrDefault	(layout, null);
			var meshBuffer		= (MappedBuffer) null;

			if (meshBuffers == null) {
				meshBuffers = new ReferenceArrayList<>	();
				meshBuffer	= new MappedBuffer			(64L);
				meshBuffers	.add						(meshBuffer);
				BUFFERS		.put 						(layout, meshBuffers);
			} else {
				meshBuffer	= (MappedBuffer) meshBuffers.get(meshBuffers.size() - 1);
			}

			if (meshBuffer.getPosition() + capacity >= GLConstants.MAX_SHADER_STORAGE_BLOCK_SIZE) {
				if (CoreFeature.shouldUploadMeshImmediately()) {
					collector
							.getBuffer	()
							.close		();

					var crashReport	= CrashReport	.forThrowable	(new OutOfMemoryError("Mesh buffer size exceeds limits."), "Exception in building meshes.");
					var category	= crashReport	.addCategory	("Mesh being built");

					category.setDetail("Mesh type",						"Server side mesh");
					category.setDetail("Mesh layout size (bytes)",		collector.getLayout()	.getSize		());
					category.setDetail("Mesh size (vertices)",			collector				.getVertexCount	());
					category.setDetail("Mesh buffer limits (bytes)",	GLConstants				.MAX_SHADER_STORAGE_BLOCK_SIZE);

					throw new ReportedException(crashReport);
				}

				meshBuffer = new MappedBuffer	(64L);
				meshBuffers.add					(meshBuffer);
			}

			var position	= meshBuffer.getPosition();
			var srcAddress	= MemoryUtil.memAddress0(byteBuffer);
			var destAddress	= meshBuffer.reserve	(capacity);

			MemoryUtil.memCopy(
					srcAddress,
					destAddress,
					capacity
			);

			builder.close();

			mesh = new ServerMesh(
					vertexCount,
					position / layout.getSize(),
					forceDense,
					meshBuffer
			);

			MeshDataCaches.SERVER.set(
					layout,
					data,
					mesh
			);

			return mesh;
		}

		@Override
		public IMesh build(IMeshCollector collector) {
			return build(collector, false);
		}

		@Override
		public void delete() {
			for (		var buffers	: BUFFERS.values()) {
				for (	var buffer	: buffers) {
					buffer.delete();
				}
			}
		}
	}
}
