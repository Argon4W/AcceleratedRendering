package com.github.argon4w.acceleratedrendering.features.emf.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;
import traben.entity_model_features.models.parts.EMFModelPartVanilla;

import java.util.Set;

@Pseudo
@Mixin(EMFModelPartVanilla.class)
public interface EMFModelPartVanillaAccessor {

	@Accessor(
			value = "hideInTheseStates",
			remap = false
	)
	Set<Integer> getHideInTheseStates();
}
