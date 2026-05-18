package com.github.argon4w.acceleratedrendering.compat.iris.mixins.acceleratedrendering;

import com.github.argon4w.acceleratedrendering.compat.iris.environments.IrisBufferEnvironment;
import com.github.argon4w.acceleratedrendering.compat.iris.programs.IrisPrograms;
import com.github.argon4w.acceleratedrendering.core.buffers.environments.IBufferEnvironment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IBufferEnvironment.Presets.class)
public class IBufferEnvironmentPresetsMixin {

	@Mutable @Shadow(remap = false) @Final public static IBufferEnvironment BLOCK;
	@Mutable @Shadow(remap = false) @Final public static IBufferEnvironment ENTITY;
	@Mutable @Shadow(remap = false) @Final public static IBufferEnvironment POS_COLOR_TEX_LIGHT;

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/environments/IBufferEnvironment$Presets;BLOCK:Lcom/github/argon4w/acceleratedrendering/core/buffers/environments/IBufferEnvironment;",
					opcode	= Opcodes.PUTSTATIC
			),
			remap	= false
	)
	private static void useIrisBlockEnvironment(IBufferEnvironment value, Operation<Void> original) {
		original.call(new IrisBufferEnvironment(
				value,
				DefaultVertexFormat	.BLOCK,
				IrisVertexFormats	.TERRAIN,
				IrisPrograms		.IRIS_BLOCK_MESH_UPLOADING_KEY,
				IrisPrograms		.IRIS_BLOCK_VERTEX_TRANSFORM_KEY
		));
	}

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/environments/IBufferEnvironment$Presets;ENTITY:Lcom/github/argon4w/acceleratedrendering/core/buffers/environments/IBufferEnvironment;",
					opcode	= Opcodes.PUTSTATIC
			),
			remap	= false
	)
	private static void useIrisEntityEnvironment(IBufferEnvironment value, Operation<Void> original) {
		original.call(new IrisBufferEnvironment(
				value,
				DefaultVertexFormat	.NEW_ENTITY,
				IrisVertexFormats	.ENTITY,
				IrisPrograms		.IRIS_ENTITY_MESH_UPLOADING_KEY,
				IrisPrograms		.IRIS_ENTITY_VERTEX_TRANSFORM_KEY
		));
	}

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lcom/github/argon4w/acceleratedrendering/core/buffers/environments/IBufferEnvironment$Presets;POS_COLOR_TEX_LIGHT:Lcom/github/argon4w/acceleratedrendering/core/buffers/environments/IBufferEnvironment;",
					opcode	= Opcodes.PUTSTATIC
			),
			remap	= false
	)
	private static void useIrisGlyphEnvironment(IBufferEnvironment value, Operation<Void> original) {
		original.call(new IrisBufferEnvironment(
				value,
				DefaultVertexFormat	.POSITION_COLOR_TEX_LIGHTMAP,
				IrisVertexFormats	.GLYPH,
				IrisPrograms		.IRIS_GLYPH_MESH_UPLOADING_KEY,
				IrisPrograms		.IRIS_GLYPH_VERTEX_TRANSFORM_KEY
		));
	}
}
