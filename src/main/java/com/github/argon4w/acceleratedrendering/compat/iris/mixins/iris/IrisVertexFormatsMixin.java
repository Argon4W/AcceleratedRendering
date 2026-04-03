package com.github.argon4w.acceleratedrendering.compat.iris.mixins.iris;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

	@Shadow @Final @Mutable	public static VertexFormat			ENTITY;
	@Shadow @Final @Mutable	public static VertexFormat			GLYPH;
	@Shadow @Final			public static VertexFormatElement	ENTITY_ID_ELEMENT;
	@Shadow @Final			public static VertexFormatElement	MID_TEXTURE_ELEMENT;
	@Shadow @Final			public static VertexFormatElement	TANGENT_ELEMENT;

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lnet/irisshaders/iris/vertices/IrisVertexFormats;ENTITY:Lcom/mojang/blaze3d/vertex/VertexFormat;",
					opcode	= Opcodes.PUTSTATIC
			)
	)
	private static void addPaddingForEntityFormat(VertexFormat value, Operation<Void> original) {
		original.call	(VertexFormat
				.builder()
				.add	("Position",		VertexFormatElement.POSITION)
				.add	("Color",			VertexFormatElement.COLOR)
				.add	("UV0",				VertexFormatElement.UV0)
				.add	("UV1",				VertexFormatElement.UV1)
				.add	("UV2",				VertexFormatElement.UV2)
				.add	("Normal",			VertexFormatElement.NORMAL)
				.padding(1)
				.add	("iris_Entity",		ENTITY_ID_ELEMENT)
				.padding(2)
				.add	("mc_midTexCoord",	MID_TEXTURE_ELEMENT)
				.add	("at_tangent",		TANGENT_ELEMENT)
				.build	()
		);
	}

	@WrapOperation(
			method	= "<clinit>",
			at		= @At(
					value	= "FIELD",
					target	= "Lnet/irisshaders/iris/vertices/IrisVertexFormats;GLYPH:Lcom/mojang/blaze3d/vertex/VertexFormat;",
					opcode	= Opcodes.PUTSTATIC
			)
	)
	private static void addPaddingForGlyphFormat(VertexFormat value, Operation<Void> original) {
		original.call	(VertexFormat
				.builder()
				.add	("Position",		VertexFormatElement.POSITION)
				.add	("Color",			VertexFormatElement.COLOR)
				.add	("UV0",				VertexFormatElement.UV0)
				.add	("UV2",				VertexFormatElement.UV2)
				.add	("Normal",			VertexFormatElement.NORMAL)
				.padding(1)
				.add	("iris_Entity",		ENTITY_ID_ELEMENT)
				.padding(2)
				.add	("mc_midTexCoord",	MID_TEXTURE_ELEMENT)
				.add	("at_tangent",		TANGENT_ELEMENT)
				.build	()
		);
	}
}
