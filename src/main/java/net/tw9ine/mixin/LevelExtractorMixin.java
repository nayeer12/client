/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.PlayerRenderState;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

	@Inject(method = "extractPlayerState(Lnet/minecraft/client/Camera;Lnet/minecraft/client/DeltaTracker;FLnet/minecraft/client/renderer/state/level/PlayerRenderState;)V", at = @At("TAIL"))
	private void onExtractPlayerState(Camera camera, DeltaTracker deltaTracker, float worldPartialTicks,
			PlayerRenderState state, CallbackInfo ci) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		if (tw9ine == null || tw9ine.moduleManager == null)
			return;

		if (tw9ine.moduleManager.norender.state.getValue()) {
			state.portalEffectIntensity = 0.0F;
			state.nauseaEffectIntensity = 0.0F;
		}
	}
}
