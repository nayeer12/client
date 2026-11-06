package net.tw9ine.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.module.modules.misc.Timer;
import net.minecraft.client.DeltaTracker;

@Mixin(DeltaTracker.Timer.class)
public class DeltaTrackerTimerMixin {
	@Shadow
	private float deltaTicks;

	@Inject(at = {
			@At(value = "FIELD", target = "Lnet/minecraft/client/DeltaTracker$Timer;lastMs:J", opcode = Opcodes.PUTFIELD, ordinal = 0) }, method = {
					"advanceGameTime(J)I" })
	public void onBeginRenderTick(long long_1, CallbackInfoReturnable<Integer> cir) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		if (tw9ine.moduleManager != null) {
			Timer timer = Tw9ine.getInstance().moduleManager.timer;
			if (timer.state.getValue()) {
				deltaTicks *= timer.getMultiplier();
			}
		}
	}
}
