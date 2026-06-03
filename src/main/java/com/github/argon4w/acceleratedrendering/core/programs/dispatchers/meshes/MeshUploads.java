package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.Getter;

import java.util.List;

public class MeshUploads {

	@Getter
	private final Long2ObjectMap<Upload>	uploads;
	private final LongSet					usages;

	public MeshUploads() {
		this.uploads	= new Long2ObjectAVLTreeMap<>	();
		this.usages		= new LongOpenHashSet			();
	}

	public void add(MeshUploader uploader) {
		var mesh	= uploader	.getServerMesh	();
		var count	= uploader	.getMeshCount	();
		var meshKey	= mesh		.meshKey		();
		var upload	= uploads	.get			(meshKey);

		if (upload == null) {
			upload = new Upload(mesh, meshKey);
		}

		upload.meshUploads.add(uploader);
		upload.meshCounter += count;

		usages.add(meshKey);
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
			if (!usages.contains(each.nextLong())) {
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

		public Upload(ServerMesh mesh, long id) {
			this.meshUploads	= new ReferenceArrayList<>();
			this.meshCounter	= 0;
			this.mesh			= mesh;

			uploads.put(id, this);
		}
	}
}
