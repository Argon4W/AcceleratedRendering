package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ProgramOverride;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;

import java.util.List;

public class DenseUploads implements IMeshUploads {

	@Getter
	private final Long2ObjectMap<Group> groups;
	private final LongSet				usages;
	private final int					count;

	public DenseUploads(Buffers buffers) {
		this.count	= buffers.getOverrideCount		();
		this.groups	= new Long2ObjectOpenHashMap<>	();
		this.usages	= new LongOpenHashSet			();
	}

	@Override
	public void add(MeshUploader uploader) {
		var mesh	= uploader	.getServerMesh	();
		var meshId	= mesh		.meshId			();
		var group	= groups	.get			(meshId);

		if (group == null) {
			group = new Group(mesh, meshId);
		}

		group	.add(uploader);
		usages	.add(meshId);
	}

	public void clear() {
		for (var group : groups.values()) {
			group.clear();
		}
	}

	public void remove() {
		var each = groups.values().iterator();

		while (each.hasNext()) {
			var val = each.next();

			if (!usages.contains(val.meshId)) {
				each.remove();
			} else {
				val.remove();
			}
		}
	}

	public class Group {

		@Getter private	final	ServerMesh	mesh;
		@Getter private final	Upload[]	uploads;
		private			final	long		meshId;

		public Group(ServerMesh mesh, long meshId) {
			this.uploads	= new Upload[count];
			this.meshId		= meshId;
			this.mesh		= mesh;

			groups.put(meshId, this);
		}

		public void add(MeshUploader uploader) {
			var override	= uploader.getOverride	();
			var count		= uploader.getMeshCount	();
			var id			= override.overrideId	();

			var upload = uploads[id];

			if (upload == null) {
				upload = new Upload(override, id);
			}

			upload.meshUploads.add(uploader);
			upload.meshCounter	+=	count;
			upload.meshFree		=	false;
		}

		public void clear() {
			for (var upload : uploads) {
				if (upload != null) {
					upload.meshUploads.clear();
					upload.meshCounter	= 0;
				}
			}
		}

		public void remove() {
			for (var i = 0; i < count; i++) {
				var upload = uploads[i];

				if (upload != null) {
					if (upload.meshFree) {
						uploads[i] = null;
					} else {
						upload.meshFree = true;
					}
				}
			}
		}

		@Getter
		public class Upload {

			private final	ProgramOverride		override;
			private final	List<MeshUploader>	meshUploads;
			private			int					meshCounter;
			private			boolean				meshFree;

			public Upload(ProgramOverride override, int id) {
				this.override		= override;
				this.meshUploads	= new ReferenceArrayList<>();
				this.meshCounter	= 0;
				this.meshFree		= true;

				uploads[id] = this;
			}
		}
	}
}
