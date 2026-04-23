package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(
		value		= IrisVertexFormats.class,
		priority	= Integer.MAX_VALUE
)
public class IrisVertexFormatsMixin {

	@Shadow(remap = false) @Final @Mutable	public static VertexFormat			ENTITY;
	@Shadow(remap = false) @Final @Mutable	public static VertexFormat			GLYPH;
	@Shadow(remap = false) @Final			public static VertexFormatElement	ENTITY_ID_ELEMENT;
	@Shadow(remap = false) @Final			public static VertexFormatElement	MID_TEXTURE_ELEMENT;
	@Shadow(remap = false) @Final			public static VertexFormatElement	TANGENT_ELEMENT;

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lnet/irisshaders/iris/vertices/IrisVertexFormats;ENTITY:Lcom/mojang/blaze3d/vertex/VertexFormat;",
					opcode	= Opcodes.PUTSTATIC
			),
			remap	= false
	)
	private static void addPaddingForEntityFormat(VertexFormat value, Operation<Void> original) {
		original.call(new VertexFormat(ImmutableMap
				.<String, VertexFormatElement>builder()
				.put	("Position",		DefaultVertexFormat.ELEMENT_POSITION)
				.put	("Color",			DefaultVertexFormat.ELEMENT_COLOR)
				.put	("UV0",				DefaultVertexFormat.ELEMENT_UV0)
				.put	("UV1",				DefaultVertexFormat.ELEMENT_UV1)
				.put	("UV2",				DefaultVertexFormat.ELEMENT_UV2)
				.put	("Normal",			DefaultVertexFormat.ELEMENT_NORMAL)
				.put	("Padding1",		DefaultVertexFormat.ELEMENT_PADDING)
				.put	("iris_Entity",		ENTITY_ID_ELEMENT)
				.put	("Padding2",		DefaultVertexFormat.ELEMENT_PADDING)
				.put	("Padding3",		DefaultVertexFormat.ELEMENT_PADDING)
				.put	("mc_midTexCoord",	MID_TEXTURE_ELEMENT)
				.put	("at_tangent",		TANGENT_ELEMENT)
				.build	()
		));
	}

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lnet/irisshaders/iris/vertices/IrisVertexFormats;GLYPH:Lcom/mojang/blaze3d/vertex/VertexFormat;",
					opcode	= Opcodes.PUTSTATIC
			),
			remap	= false
	)
	private static void addPaddingForGlyphFormat(VertexFormat value, Operation<Void> original) {
		original.call(new VertexFormat(ImmutableMap
				.<String, VertexFormatElement>builder()
				.put	("Position",		DefaultVertexFormat.ELEMENT_POSITION)
				.put	("Color",			DefaultVertexFormat.ELEMENT_COLOR)
				.put	("UV0",				DefaultVertexFormat.ELEMENT_UV0)
				.put	("UV2",				DefaultVertexFormat.ELEMENT_UV2)
				.put	("Normal",			DefaultVertexFormat.ELEMENT_NORMAL)
				.put	("Padding1",		DefaultVertexFormat.ELEMENT_PADDING)
				.put	("iris_Entity",		ENTITY_ID_ELEMENT)
				.put	("Padding2",		DefaultVertexFormat.ELEMENT_PADDING)
				.put	("Padding3",		DefaultVertexFormat.ELEMENT_PADDING)
				.put	("mc_midTexCoord",	MID_TEXTURE_ELEMENT)
				.put	("at_tangent",		TANGENT_ELEMENT)
				.build	()
		));
	}
}
