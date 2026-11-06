package net.tw9ine.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.module.modules.render.XRay;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = BlockRenderer.class, remap = false)
public abstract class SodiumBlockRendererMixin {
	@Inject(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/model/color/ColorProviderRegistry;getColorProvider(Lnet/minecraft/world/level/block/Block;)Lnet/caffeinemc/mods/sodium/client/model/color/ColorProvider;", remap = true), cancellable = true)
	public void onRenderModel(BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin, CallbackInfo ci) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		if (tw9ine == null) return;
		XRay xray = tw9ine.moduleManager.xray;
		if (xray.state.getValue() && !xray.isXRayBlock(state.getBlock())) {
			ci.cancel();
		}
	}
}
