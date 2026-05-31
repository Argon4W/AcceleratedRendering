package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.Getter;

import java.util.List;

public class MeshUploads {

	@Getter
	private final Int2ObjectMap<Upload>	uploads;
	private final IntSet				usages;

	public MeshUploads() {
		this.uploads	= new Int2ObjectAVLTreeMap<>();
		this.usages		= new IntOpenHashSet		();
	}

	public void add(MeshUploader uploader) {
		var mesh	= uploader	.getServerMesh	();
		var count	= uploader	.getMeshCount	();
		var meshID	= mesh		.meshId			();
		var upload	= uploads	.get			(meshID);

		if (upload == null) {
			upload = new Upload(mesh, meshID);
		}

		upload.meshUploads.add(uploader);
		upload.meshCounter += count;

		usages.add(meshID);
	}

	public void clear() {
		for (var upload : this.uploads.values()) {
			upload.meshUploads.clear();
			upload.meshCounter = 0;
		}
	}

	public void endUpload() {
		var each = uploads.keySet().iterator();

		while (each.hasNext()) {
			if (!usages.contains(each.nextInt())) {
				each.remove();
			}
		}

		usages.clear();
	}

	@Getter
	public class Upload {

		private final	ServerMesh			mesh;
		private final	List<MeshUploader>	meshUploads;
		private			int					meshCounter;

		public Upload(ServerMesh mesh, int id) {
			this.meshUploads	= new ReferenceArrayList<>();
			this.meshCounter	= 0;
			this.mesh			= mesh;

			uploads.put(id, this);
		}
	}
}
