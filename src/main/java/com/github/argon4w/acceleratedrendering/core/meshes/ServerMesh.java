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
		int				meshLayer,
		int				meshId,
		int				size,
		int				offset,
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

	public boolean isDense(int count) {
		return forceDense || count >= CoreFeature.getSparseThreshold();
	}

	public static class Builder implements IMesh.Builder {

		public static final Builder													INSTANCE;
		public static final Reference2ObjectMap<VertexLayout, List<IServerBuffer>>	NORMAL_BUFFERS;
		public static final Reference2ObjectMap<VertexLayout, List<IServerBuffer>>	RELOAD_BUFFERS;
		public static		int														COUNTER;

		static {
			INSTANCE		= new Builder						();
			NORMAL_BUFFERS	= new Reference2ObjectOpenHashMap<>	();
			RELOAD_BUFFERS	= new Reference2ObjectOpenHashMap<>	();

			NORMAL_BUFFERS.defaultReturnValue(ReferenceLists.singleton(EmptyServerBuffer.INSTANCE));
			RELOAD_BUFFERS.defaultReturnValue(ReferenceLists.singleton(EmptyServerBuffer.INSTANCE));
		}

		private Builder() {

		}

		@Override
		public IMesh build(
				IMeshCollector	collector,
				boolean			forceDense,
				boolean			reloadSensitive,
				int				meshLayer
		) {
			var vertexCount	= collector.getVertexCount();

			if (vertexCount == 0) {
				return EmptyMesh.INSTANCE;
			}

			var builder	= collector				.getBuffer	();
			var layout	= collector				.getLayout	();
			var data	= collector				.getData	();
			var mesh	= MeshDataCaches.SERVER	.get		(layout, data);

			if (mesh != null) {
				builder.close();
				return mesh;
			}

			var result = builder.build();

			if (result == null) {
				builder.close();
				return EmptyMesh.INSTANCE;
			}

			var buffers = reloadSensitive
					? RELOAD_BUFFERS
					: NORMAL_BUFFERS;

			var buffer		= result	.byteBuffer		();
			var capacity	= buffer	.capacity		();
			var meshBuffers	= buffers	.getOrDefault	(layout, null);

			var meshBuffer = (MappedBuffer) null;

			if (meshBuffers == null) {
				meshBuffers	= new ReferenceArrayList<>	();
				meshBuffer	= new MappedBuffer			(64L);
				meshBuffers	.add						(meshBuffer);
				buffers		.put						(layout, meshBuffers);
			} else {
				meshBuffer	= (MappedBuffer) meshBuffers.get(meshBuffers.size() - 1);
			}

			if (meshBuffer.overflow(capacity)) {
				meshBuffer = new MappedBuffer(64L);

				meshBuffers.add(meshBuffer);
			}

			var position	= meshBuffer.getPosition();
			var srcAddress	= MemoryUtil.memAddress0(buffer);
			var destAddress	= meshBuffer.reserve	(capacity);

			MemoryUtil.memCopy(
					srcAddress,
					destAddress,
					capacity
			);

			builder.close();

			mesh = new ServerMesh(
					meshLayer,
					COUNTER ++,
					vertexCount,
					(int) (position / layout.getSize()),
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
		public IMesh build(
				IMeshCollector	collector,
				boolean			forceDense,
				int				meshLayer
		) {
			return build(
					collector,
					forceDense,
					false,
					meshLayer
			);
		}

		@Override
		public IMesh build(IMeshCollector collector, boolean forceDense) {
			return build(collector, forceDense, 0);
		}

		@Override
		public IMesh build(IMeshCollector collector) {
			return build(collector, false);
		}

		@Override
		public void delete() {
			for (var buffers : NORMAL_BUFFERS.values()) for (var buffer : buffers) buffer.delete();
			for (var buffers : RELOAD_BUFFERS.values()) for (var buffer : buffers) buffer.delete();
		}

		@Override
		public void reload() {
			for (var buffers : RELOAD_BUFFERS.values()) for (var buffer : buffers) ((MappedBuffer) buffer).reset();
		}
	}
}
