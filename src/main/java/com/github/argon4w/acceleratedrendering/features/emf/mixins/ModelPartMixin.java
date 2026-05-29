package com.github.argon4w.acceleratedrendering.features.emf.mixins;

import com.github.argon4w.acceleratedrendering.features.emf.IEMFHideable;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ModelPart.class)
public class ModelPartMixin implements IEMFHideable {

	@Override
	public boolean isHidden() {
		return false;
	}
}
