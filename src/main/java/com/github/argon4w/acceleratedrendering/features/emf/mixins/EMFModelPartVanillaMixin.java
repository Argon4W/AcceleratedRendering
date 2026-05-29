package com.github.argon4w.acceleratedrendering.features.emf.mixins;

import com.github.argon4w.acceleratedrendering.features.emf.IEMFHideable;
import org.spongepowered.asm.mixin.*;
import traben.entity_model_features.models.parts.EMFModelPartVanilla;

import java.util.Set;

@Pseudo
@Mixin(EMFModelPartVanilla.class)
public abstract class EMFModelPartVanillaMixin extends EMFModelPartWithStateMixin implements IEMFHideable {

	@Shadow(remap = false) @Final Set<Integer> hideInTheseStates;

	@Unique
	@Override
	public boolean isHidden() {
		return hideInTheseStates.contains(currentModelVariant);
	}
}
