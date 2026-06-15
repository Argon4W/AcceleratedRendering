package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import com.github.argon4w.acceleratedrendering.core.programs.overrides.ProgramOverride;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.Getter;

import java.util.BitSet;
import java.util.List;

public class DenseUploads extends MeshSparseMap<DenseUploads.Group> implements IMeshUploads {

	private final int count;

	public DenseUploads(Buffers buffers) {
		this.count = buffers.getOverrideCount();
	}

	@Override
	public Group create(ServerMesh mesh) {
		return new Group(mesh);
	}

	@Override
	public void clear(Group group) {
		for (int index = 0, size = group.uploads.length; index < size; index ++) {
			var upload = group.uploads[index];

			if (upload != null) {
				upload.meshUploads.clear();
				upload.meshCounter = 0;
			}
		}
	}

	@Override
	public void remove(Group group) {
		var usages	= group.usages;
		var uploads	= group.uploads;

		for (var i = 0; i < count; i++) {
			if (!usages.get(i)) {
				uploads[i] = null;
			}
		}

		usages.clear();
	}

	@Override
	public void add(MeshUploader uploader) {
		get(uploader.getServerMesh()).add(uploader);
	}

	public class Group {

		@Getter private	final ServerMesh	mesh;
		@Getter private final Upload[]		uploads;
		private			final BitSet		usages;

		public Group(ServerMesh mesh) {
			this.uploads	= new Upload[count];
			this.usages		= new BitSet(count);
			this.mesh		= mesh;
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
