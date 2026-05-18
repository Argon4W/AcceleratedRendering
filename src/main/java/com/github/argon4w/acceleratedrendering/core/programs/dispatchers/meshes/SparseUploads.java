package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.AcceleratedRingBuffers.Buffers;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.Getter;

import java.util.List;

public class SparseUploads implements IMeshUploads {

	@Getter
	private final Upload[]		uploads;
	private final MeshOffsets	offsets;
	private final int			count;

	public SparseUploads(Buffers buffers, MeshOffsets offsets) {
		this.offsets	= offsets;
		this.count		= buffers.getSize();
		this.uploads	= new Upload[this.count];
	}

	@Override
	public void add(MeshUploader uploader) {
		var index = uploader.getIndex();

		var upload = uploads[index];

		if (upload == null) {
			upload = new Upload(index);
		}

		offsets	.reserve(uploader);
		upload	.add	(uploader);
	}

	public void clear() {
		for (var upload : uploads) {
			if (upload != null) {
				upload.uploaders.clear();
			}
		}
	}

	public void remove() {
		for (var i = 0; i < count; i++) {
			var upload = uploads[i];

			if (upload != null) {
				if (upload.uploadFree) {
					uploads[i] = null;
				} else {
					upload.uploadFree = true;
				}
			}
		}
	}

	@Getter
	public class Upload {

		private	final	List<MeshUploader>	uploaders;
		private			boolean				uploadFree;

		public Upload(int index) {
			this.uploaders	= new ReferenceArrayList<>();
			this.uploadFree	= true;

			uploads[index] = this;
		}

		public void add(MeshUploader uploader) {
			uploaders.add(uploader);
		}
	}
}
