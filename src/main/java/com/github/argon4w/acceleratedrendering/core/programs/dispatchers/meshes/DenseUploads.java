package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ProgramOverride;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;

import java.util.BitSet;
import java.util.List;

public class DenseUploads implements IMeshUploads {

	@Getter
	private final Long2ObjectMap<Group>	groups;
	private final LongSet				usages;
	private final int					count;

	public DenseUploads(Buffers buffers) {
		this.count	= buffers.getOverrideCount		();
		this.groups	= new Long2ObjectAVLTreeMap<>	();
		this.usages	= new LongOpenHashSet			();
	}

	@Override
	public void add(MeshUploader uploader) {
		var mesh	= uploader	.getServerMesh	();
		var meshKey	= mesh		.meshKey		();
		var group	= groups	.get			(meshKey);

		if (group == null) {
			group = new Group(mesh, meshKey);
		}

		group	.add(uploader);
		usages	.add(meshKey);
	}

	public void clear() {
		for (var group : groups.values()) {
			group.clear();
		}
	}

	public void endUpload() {
		var each = groups.values().iterator();

		while (each.hasNext()) {
			var val = each.next();

			if (!usages.contains(val.meshKey)) {
				each.remove();
			} else {
				val.remove();
			}
		}

		usages.clear();
	}

	public class Group {

		@Getter private	final ServerMesh	mesh;
		@Getter private final Upload[]		uploads;
		private			final BitSet		usages;
		private			final long			meshKey;

		public Group(ServerMesh mesh, long meshKey) {
			this.uploads	= new Upload[count];
			this.usages		= new BitSet(count);
			this.meshKey	= meshKey;
			this.mesh		= mesh;

			groups.put(meshKey, this);
		}

		public void add(MeshUploader uploader) {
			var meshCount	= uploader.getMeshCount	();
			var override	= uploader.getOverride	();
			var id			= override.overrideId	();

			var upload = uploads[id];

			if (upload == null) {
				upload = new Upload(override, id);
			}

			this	.usages		.set(id);
			upload	.meshUploads.add(uploader);
			upload	.meshCounter += meshCount;
		}

		public void clear() {
			for (var upload : uploads) {
				if (upload != null) {
					upload.meshUploads.clear();
					upload.meshCounter = 0;
				}
			}
		}

		public void remove() {
			for (var i = 0; i < count; i++) {
				if (!usages.get(i)) {
					uploads[i] = null;
				}
			}

			usages.clear();
		}

		@Getter
		public class Upload {

			private final	ProgramOverride		override;
			private final	List<MeshUploader>	meshUploads;
			private			int					meshCounter;

			public Upload(ProgramOverride override, int id) {
				this.override		= override;
				this.meshUploads	= new ReferenceArrayList<>();
				this.meshCounter	= 0;

				uploads[id] = this;
			}
		}
	}
}
