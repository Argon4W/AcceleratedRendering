package com.github.argon4w.acceleratedrendering.core;

import com.github.argon4w.acceleratedrendering.core.backends.states.IBindingState;

public class CoreStates {

	public static final IBindingState SHADER_STORAGE_BUFFER_STATE = CoreFeature.createShaderStorageState();
	public static final IBindingState ATOMIC_COUNTER_BUFFER_STATE = CoreFeature.createAtomicCounterState();

	public static void recordBuffers() {
		if (CoreFeature.shouldRestoreBlockBuffers()) {
			SHADER_STORAGE_BUFFER_STATE.capture(null);
			ATOMIC_COUNTER_BUFFER_STATE.capture(null);
		}
	}

	public static void restoreBuffers() {
		if (CoreFeature.shouldRestoreBlockBuffers()) {
			SHADER_STORAGE_BUFFER_STATE.restore();
			ATOMIC_COUNTER_BUFFER_STATE.restore();
		}
	}

	public static void delete() {
		SHADER_STORAGE_BUFFER_STATE.delete();
		ATOMIC_COUNTER_BUFFER_STATE.delete();
	}
}
