package com.github.argon4w.acceleratedrendering.core.mixins.buffers;

import com.github.argon4w.acceleratedrendering.core.CoreBuffersProvider;
import com.github.argon4w.acceleratedrendering.core.buffers.accelerated.builders.BufferSourceExtension;
import lombok.experimental.ExtensionMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.RenderBuffers;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ExtensionMethod(BufferSourceExtension	.class)
@Mixin			(Minecraft				.class)
public class MinecraftMixin {

	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Inject(
			method	= "<init>",
			at		= @At(
					value	= "FIELD",
					target	= "Lnet/minecraft/client/Minecraft;renderBuffers:Lnet/minecraft/client/renderer/RenderBuffers;",
					opcode	= Opcodes.PUTFIELD,
					shift	= At.Shift.AFTER
			)
	)
	public void bindAcceleratedBufferSources(GameConfig gameConfig, CallbackInfo ci) {
		CoreBuffersProvider.bindAcceleratedBufferSources(renderBuffers);
	}
}
