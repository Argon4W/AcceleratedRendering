package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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
		this.uploads	= new Long2ObjectOpenHashMap<>	();
		this.usages		= new LongOpenHashSet			();
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

	public void remove() {
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

		public Upload(ServerMesh mesh, long meshId) {
			this.meshUploads	= new ReferenceArrayList<>();
			this.meshCounter	= 0;
			this.mesh			= mesh;

			uploads.put(meshId, this);
		}
	}
}
