package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ProgramOverride;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;

import java.util.BitSet;
import java.util.List;

public class DenseUploads implements IMeshUploads {

	@Getter
	private final Int2ObjectMap<Group>	groups;
	private final IntSet				usages;
	private final int					count;

	public DenseUploads(Buffers buffers) {
		this.count	= buffers.getOverrideCount	();
		this.groups	= new Int2ObjectAVLTreeMap<>();
		this.usages	= new IntOpenHashSet		();
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

	public void endUpload() {
		var each = groups.values().iterator();

		while (each.hasNext()) {
			var val = each.next();

			if (!usages.contains(val.meshId)) {
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
		private			final int			meshId;

		public Group(ServerMesh mesh, int meshId) {
			this.uploads	= new Upload[count];
			this.usages		= new BitSet(count);
			this.meshId		= meshId;
			this.mesh		= mesh;

			groups.put(meshId, this);
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
