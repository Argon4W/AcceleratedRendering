package com.github.argon4w.acceleratedrendering.features.text.mixins;

import com.github.argon4w.acceleratedrendering.core.CoreFeature;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import com.github.argon4w.acceleratedrendering.features.text.AcceleratedTextRenderingFeature;
import com.github.argon4w.acceleratedrendering.features.text.IAcceleratedFont;
import com.github.argon4w.acceleratedrendering.features.text.cache.*;
import com.github.argon4w.acceleratedrendering.features.text.extensions.StringRenderOutputExtension;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@ExtensionMethod({
		StringRenderOutputExtension	.class,
		BufferSourceExtension		.class,
})
@Mixin(Font.class)
public class FontMixin implements IAcceleratedFont {

	@Unique private final Object2IntMap	<ISeekableFormattedText>					widths			= new Object2IntOpenHashMap		<>	();
	@Unique private final Map			<ISeekableFormattedText,	OutlineMesh>	outlineMeshes	= new Object2ObjectOpenHashMap	<>	();
	@Unique private final Map			<IFormattedTextKey,			ComponentMesh>	normalMeshes	= new Object2ObjectOpenHashMap	<>	();
	@Unique private final Map			<IFormattedTextKey,			ComponentMesh>	shadowMeshes	= new Object2ObjectOpenHashMap	<>	();
	@Unique private final IFormattedTextKey.Mutable									scratchKey		= new IFormattedTextKey.Mutable		();

	@Shadow
	public FontSet getFontSet(ResourceLocation fontLocation) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@Shadow
	private static int adjustColor(int color) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@Shadow
	public int width(FormattedCharSequence text) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	@WrapMethod(
			method	= "width(Ljava/lang/String;)I",
			require	= 0
	)
	public int wrapWidth1(String text, Operation<Integer> original) {
		if (			CoreFeature						.isLoaded						()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			var seekable = new StringText(text);

			var width = widths.getOrDefault(seekable, -1);

			if (width == -1) {
				width = original.call(text);

				widths.put(seekable, width);
			}

			return width;
		}

		return original.call(text);
	}

	@WrapMethod(
			method	= "width(Lnet/minecraft/network/chat/FormattedText;)I",
			require	= 0
	)
	public int wrapWidth2(FormattedText text, Operation<Integer> original) {
		if (			text instanceof ISeekableFormattedText seekable
				&&		CoreFeature						.isLoaded						()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			var width = widths.getOrDefault(seekable, -1);

			if (width == -1) {
				width = original.call(text);

				widths.put(seekable, width);
			}

			return width;
		}

		return original.call(text);
	}

	@WrapMethod(
			method	= "width(Lnet/minecraft/util/FormattedCharSequence;)I",
			require	= 0
	)
	public int wrapWidth3(FormattedCharSequence text, Operation<Integer> original) {
		if (			text				instanceof ISeekableFormattedCharSequence	source
				&&		source.getSource()	instanceof ISeekableFormattedText			seekable
				&&		CoreFeature						.isLoaded						()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			var width = widths.getOrDefault(seekable, -1);

			if (width == -1) {
				width = original.call(text);

				widths.put(seekable, width);
			}

			return width;
		}

		return original.call(text);
	}

	@Inject(
			method		= "renderText(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
			at			= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
					shift	= At.Shift.BEFORE
			),
			cancellable	= true,
			require		= 0
	)
	public void onRenderTextFormatted(
			String							string,
			float							positionX,
			float							positionY,
			int								color,
			boolean							dropShadow,
			Matrix4f						transform,
			MultiBufferSource				buffer,
			Font.DisplayMode				displayMode,
			int								background,
			int								packedLight,
			CallbackInfoReturnable<Float>	cir,
			@Local Font.StringRenderOutput	sink
	) {
		if (			CoreFeature						.isLoaded						()
				&&		buffer.getAcceleratable()		.isBufferSourceAcceleratable	()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			var seekable	= new StringText(string);
			var advance		= 0.0f;
			var extension	= sink.getAccelerated();
			var meshes		= dropShadow
					? shadowMeshes
					: normalMeshes;

			scratchKey.setText	(seekable);
			scratchKey.setMode	(displayMode);
			scratchKey.setShadow(dropShadow);

			var mesh = meshes.get(scratchKey);

			if (mesh != null) {
				advance = mesh.render(
						(Font) (Object) this,
						displayMode,
						buffer,
						transform,
						positionX,
						positionY,
						packedLight,
						color
				);

				if (background != 0) {
					var glyph = getFontSet(Style.DEFAULT_FONT).whiteGlyph();

					glyph.renderEffect(
							new BakedGlyph.Effect(
									positionX - 1.0f,
									positionY + 9.0f,
									positionX + 1.0f + advance,
									positionY - 1.0f,
									0.01f,
									(float) (background >> 16	& 0xFF) / 255.0f,
									(float) (background >> 8	& 0xFF) / 255.0f,
									(float) (background >> 0	& 0xFF) / 255.0f,
									(float) (background >> 24	& 0xFF) / 255.0f
							),
							transform,
							buffer.getBuffer(glyph.renderType(displayMode)),
							packedLight
					);
				}
			} else {
				extension.beginMesh();

				StringDecomposer.iterateFormatted(
						string,
						Style.EMPTY,
						sink
				);

				advance = sink.finish(background, positionX);

				meshes.put(scratchKey.bake(), extension.bake());
			}

			cir.setReturnValue(advance);
		}
	}

	@Inject(
			method		= "renderText(Lnet/minecraft/util/FormattedCharSequence;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/gui/Font$DisplayMode;II)F",
			at			= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z",
					shift	= At.Shift.BEFORE
			),
			cancellable	= true,
			require		= 0
	)
	public void onRenderTextFormatted(
			FormattedCharSequence			formatted,
			float							positionX,
			float							positionY,
			int								color,
			boolean							dropShadow,
			Matrix4f						transform,
			MultiBufferSource				buffer,
			Font.DisplayMode				displayMode,
			int								background,
			int								packedLight,
			CallbackInfoReturnable<Float>	cir,
			@Local Font.StringRenderOutput	sink
	) {
		if (			formatted			instanceof ISeekableFormattedCharSequence	source
				&&		source.getSource()	instanceof ISeekableFormattedText			seekable
				&&		CoreFeature						.isLoaded						()
				&&		buffer.getAcceleratable()		.isBufferSourceAcceleratable	()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			var advance	= 0.0f;
			var meshes	= dropShadow
					? shadowMeshes
					: normalMeshes;

			scratchKey.setText	(seekable);
			scratchKey.setMode	(displayMode);
			scratchKey.setShadow(dropShadow);

			var mesh = meshes.get(scratchKey);

			if (mesh != null) {
				advance = mesh.render(
						(Font) (Object) this,
						displayMode,
						buffer,
						transform,
						positionX,
						positionY,
						packedLight,
						color
				);

				if (background != 0) {
					var glyph = getFontSet(Style.DEFAULT_FONT).whiteGlyph();

					glyph.renderEffect(
							new BakedGlyph.Effect(
									positionX - 1.0f,
									positionY + 9.0f,
									positionX + 1.0f + advance,
									positionY - 1.0f,
									0.01f,
									(float) (background >> 16	& 0xFF) / 255.0f,
									(float) (background >> 8	& 0xFF) / 255.0f,
									(float) (background >> 0	& 0xFF) / 255.0f,
									(float) (background >> 24	& 0xFF) / 255.0f
							),
							transform,
							buffer.getBuffer(glyph.renderType(displayMode)),
							packedLight
					);
				}
			} else {
				var extension = sink.getAccelerated();

				extension		.beginMesh	();
				formatted		.accept		(sink);
				advance	= sink	.finish		(background, positionX);

				meshes.put(scratchKey.bake(), extension.bake());
			}

			cir.setReturnValue(advance);
		}
	}

	@Inject(
			method		= "drawInBatch8xOutline",
			at			= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/client/gui/Font$StringRenderOutput;<init>(Lnet/minecraft/client/gui/Font;Lnet/minecraft/client/renderer/MultiBufferSource;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/gui/Font$DisplayMode;I)V",
					ordinal	= 0,
					shift	= At.Shift.BY,
					by		= 2
			),
			cancellable	= true,
			require		= 0
	)
	public void onDraw8xOutline1(
			FormattedCharSequence							formatted,
			float											positionX,
			float											positionY,
			int												color,
			int												backgroundColor,
			Matrix4f										transform,
			MultiBufferSource								bufferSource,
			int												packedLight,
			CallbackInfo									ci,
			@Local(ordinal = 0) 	Font.StringRenderOutput	sink,
			@Local(name = "i")		int						adjustedBackgroundColor,
			@Share("accelerated")	LocalBooleanRef			accelerated
	) {
		if (			CoreFeature						.isLoaded						()
				&&		bufferSource.getAcceleratable()	.isBufferSourceAcceleratable	()
				&&		AcceleratedTextRenderingFeature	.isEnabled						()
				&&		AcceleratedTextRenderingFeature	.shouldUseAcceleratedPipeline	()
				&&	(	CoreFeature						.isRenderingLevel				()
				||		CoreFeature						.isRenderingGui					())
		) {
			accelerated.set(true);

			if (		formatted			instanceof ISeekableFormattedCharSequence	source
					&&	source.getSource()	instanceof ISeekableFormattedText			seekable
			) {
				ci.cancel();

				color = adjustColor(color);

				var mesh = outlineMeshes.get(seekable);

				if (mesh != null) {
					mesh.render(
							(Font) (Object) this,
							bufferSource,
							transform,
							positionX,
							positionY,
							packedLight,
							adjustedBackgroundColor,
							color
					);
				} else {
					var outlined	= (ComponentMesh) null;
					var centered	= (ComponentMesh) null;
					var extension	= sink.getAccelerated();

					extension.setPosition	(positionX, positionY);
					extension.setOutline	(true);
					extension.beginMesh		();

					formatted.accept(WithColorSink.of(sink, adjustedBackgroundColor));
					extension.flush	();

					outlined = extension.bake();

					extension.setPosition	(positionX,	positionY);
					extension.setOutline	(false);
					extension.setColor		(color);
					extension.setMode		(Font.DisplayMode.POLYGON_OFFSET);
					extension.beginMesh		();

					formatted.accept(sink);
					extension.flush	();

					centered = extension.bake();

					outlineMeshes.put(seekable, new OutlineMesh(outlined, centered));
				}
			}
		} else {
			accelerated.set(false);
		}
	}

	@WrapOperation(
			method	= "drawInBatch8xOutline",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z",
					ordinal	= 0
			),
			require	= 0
	)
	public boolean onDraw8xOutline2(
			FormattedCharSequence					instance,
			FormattedCharSink						formattedCharSink,
			Operation<Boolean>						original,
			FormattedCharSequence					text,
			float									positionX,
			float									positionY,
			int										color,
			int										backgroundColor,
			Matrix4f								transform,
			MultiBufferSource						bufferSource,
			int										packedLight,
			@Share("accelerated") LocalBooleanRef	accelerated
	) {
		if (!accelerated.get()) {
			return original.call(instance, formattedCharSink);
		}

		return true;
	}

	@Inject(
			method	= "drawInBatch8xOutline",
			at		= @At(
					value	= "INVOKE",
					target	= "Lnet/minecraft/util/FormattedCharSequence;accept(Lnet/minecraft/util/FormattedCharSink;)Z",
					shift	= At.Shift.BEFORE,
					ordinal	= 1
			),
			require	= 0
	)
	public void onDraw8xOutline3(
			FormattedCharSequence							formatted,
			float											positionX,
			float											positionY,
			int												color,
			int												backgroundColor,
			Matrix4f										transform,
			MultiBufferSource								bufferSource,
			int												packedLight,
			CallbackInfo									ci,
			@Local(ordinal = 0) 	Font.StringRenderOutput	sink,
			@Local(name = "i")		int						adjustedBackgroundColor,
			@Share("accelerated")	LocalBooleanRef			accelerated
	) {
		if (accelerated.get()) {
			var extension = sink.getAccelerated();

			extension.setPosition	(positionX, positionY);
			extension.setOutline	(true);

			formatted.accept(WithColorSink.of(sink, adjustedBackgroundColor));
			extension.flush	();
		}
	}

	@Unique
	@Override
	public void reload() {
		widths			.clear();
		normalMeshes	.clear();
		shadowMeshes	.clear();
		outlineMeshes	.clear();
		scratchKey		.reset();
	}
}
