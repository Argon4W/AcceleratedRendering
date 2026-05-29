package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import net.irisshaders.iris.compat.sodium.impl.vertex_format.entity_xhfp.EntityVertex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(EntityVertex.class)
public class EntityVertexMixin {

	@ModifyConstant(
			method		= "write2",
			constant	= @Constant(longValue = 42L),
			remap		= false
	)
	private static long modifyMidUV1(long constant) {
		return 44L;
	}

	@ModifyConstant(
			method		= "write",
			constant	= @Constant(longValue = 42L),
			remap		= false
	)
	private static long modifyMidUV2(long constant) {
		return 44L;
	}

	@ModifyConstant(
			method		= "endQuad",
			constant	= @Constant(longValue = 50L),
			remap		= false
	)
	private static long modifyTangent1(long constant) {
		return 52L;
	}

	@ModifyConstant(
			method		= "write",
			constant	= @Constant(longValue = 50L),
			remap		= false
	)
	private static long modifyTangent2(long constant) {
		return 52L;
	}
}
