package com.github.argon4w.acceleratedrendering.core.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public class AvailabilityUtils {

	private static boolean AVAILABILITY	= false;
	private static boolean CACHED		= false;

	public static boolean isAvailable() {
		if (CACHED) {
			return AVAILABILITY;
		}

		if (!RenderSystem.isOnRenderThreadOrInit()) {
			return false;
		}

		var cap = GL.createCapabilities();

		CACHED = true;

		AVAILABILITY =	cap.GL_ARB_shader_image_load_store
				&&		cap.GL_ARB_sync
				&&		cap.GL_ARB_direct_state_access
				&&		cap.GL_ARB_compute_shader
				&&		cap.GL_ARB_buffer_storage
				&&		cap.GL_ARB_shader_atomic_counters;

		var renderer = GL11.glGetString(GL11.GL_RENDERER);

		if (renderer != null) {
			AVAILABILITY &=	!renderer.contains("MobileGlues")
					&&		!renderer.contains("gl4es")
					&&		!renderer.contains("LTW");
		}

		return AVAILABILITY;
	}
}
