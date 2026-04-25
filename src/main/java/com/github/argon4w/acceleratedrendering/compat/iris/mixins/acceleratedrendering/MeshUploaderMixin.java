package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.interfaces.IIrisMeshInfoCache;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.IMeshInfoCache;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.pools.meshes.MeshUploaderPool;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.IMemoryInterface;
import com.github.argon4w.acceleratedrendering.core.buffers.memory.SimpleDynamicMemoryInterface;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeshUploaderPool.MeshUploader.class)
public abstract class MeshUploaderMixin {

	@Shadow(remap = false) @Final private	IMeshInfoCache meshInfos;

	@Unique private final					IMemoryInterface IRIS_INFO_ENTITY		= new SimpleDynamicMemoryInterface(7L * 4L + 0L * 2L, (MeshUploaderPool.MeshUploader) (Object) this);
	@Unique private final					IMemoryInterface IRIS_INFO_BLOCK_ENTITY	= new SimpleDynamicMemoryInterface(7L * 4L + 1L * 2L, (MeshUploaderPool.MeshUploader) (Object) this);
	@Unique private final					IMemoryInterface IRIS_INFO_ITEM			= new SimpleDynamicMemoryInterface(7L * 4L + 2L * 2L, (MeshUploaderPool.MeshUploader) (Object) this);

	@Inject(
			method	= "upload",
			at		= @At(
					value	= "INVOKE",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/memory/IMemoryInterface;putInt(JI)V",
					ordinal	= 4,
					shift	= At.Shift.AFTER
			),
			remap	= false
	)
	public void uploadIrisData(
			long					meshInfoAddress,
			int						vertexOffset,
			int						varyingOffset,
			CallbackInfo			ci,
			@Local(name = "i") int	offset
	) {
		IRIS_INFO_ENTITY		.at(offset).putShort(meshInfoAddress, ((IIrisMeshInfoCache) meshInfos).getRenderedEntity		(offset));
		IRIS_INFO_BLOCK_ENTITY	.at(offset).putShort(meshInfoAddress, ((IIrisMeshInfoCache) meshInfos).getRenderedBlockEntity	(offset));
		IRIS_INFO_ITEM			.at(offset).putShort(meshInfoAddress, ((IIrisMeshInfoCache) meshInfos).getRenderedItem			(offset));
	}
}
