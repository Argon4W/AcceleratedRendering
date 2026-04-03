package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import net.irisshaders.iris.vertices.sodium.ModelToEntityVertexSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Pseudo
@Mixin(ModelToEntityVertexSerializer.class)
public class ModelToEntityVertexSerializerMixin {

	@ModifyConstant(
			method		= "serialize",
			constant	= @Constant(longValue = 42L)
	)
	public long modifyMidU(long constant) {
		return 44L;
	}

	@ModifyConstant(
			method		= "serialize",
			constant	= @Constant(longValue = 46L)
	)
	public long modifyMidV(long constant) {
		return 48L;
	}

	@ModifyConstant(
			method		= "serialize",
			constant	= @Constant(longValue = 50L)
	)
	public long modifyTangent(long constant) {
		return 52L;
	}
}
