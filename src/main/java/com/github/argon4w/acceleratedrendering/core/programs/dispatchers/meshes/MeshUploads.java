package com.github.argon4w.acceleratedrendering.core.programs.dispatchers.meshes;

import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool.MeshUploader;
import com.github.argon4w.acceleratedrendering.core.meshes.ServerMesh;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import lombok.Getter;

import java.util.List;

public class MeshUploads extends MeshSparseMap<MeshUploads.Upload> implements IMeshUploads {

	@Override
	public Upload create(ServerMesh mesh) {
		return new Upload(mesh);
	}

	@Override
	public void remove(Upload object) {

	}

	@Override
	public void clear(Upload upload) {
		upload.meshUploads.clear();
		upload.meshCounter = 0;
	}

	@Override
	public void add(MeshUploader uploader) {
		var mesh	= uploader	.getServerMesh	();
		var count	= uploader	.getMeshCount	();
		var upload	= get						(mesh);

		upload.meshUploads.add(uploader);
		upload.meshCounter += count;
	}

	public static class Upload {

		@Getter private final	ServerMesh			mesh;
		@Getter private final	List<MeshUploader>	meshUploads;
		@Getter private			int					meshCounter;

		public Upload(ServerMesh mesh) {
			this.meshUploads	= new ReferenceArrayList<>();
			this.meshCounter	= 0;
			this.mesh			= mesh;
		}
	}
}
