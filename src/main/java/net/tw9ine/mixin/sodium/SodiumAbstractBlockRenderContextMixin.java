package net.tw9ine.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.module.modules.render.XRay;
import net.caffeinemc.mods.sodium.client.render.model.AbstractBlockRenderContext;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(value = AbstractBlockRenderContext.class, remap = false)
public abstract class SodiumAbstractBlockRenderContextMixin {

	@Shadow
	protected BlockState state;

	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
	private void onShouldDrawSide(Direction face, CallbackInfoReturnable<Boolean> cir) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		if (tw9ine == null) return;
		XRay xray = tw9ine.moduleManager.xray;
		if (xray.state.getValue() && state != null && xray.isXRayBlock(state.getBlock())) {
			cir.setReturnValue(true);
		}
	}
}
