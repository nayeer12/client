/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.event.events.PlayerHealthEvent;
import net.tw9ine.event.events.SendMovementPacketEvent;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.mixin.interfaces.ICamera;
import net.tw9ine.module.modules.movement.Fly;
import net.tw9ine.module.modules.movement.HighJump;
import net.tw9ine.module.modules.movement.Noclip;
import net.tw9ine.module.modules.movement.Step;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayerMixin {
	@Shadow
	private ClientPacketListener connection;

	@Shadow
	protected abstract void sendPosition();

	@Inject(at = { @At("HEAD") }, method = "setShowDeathScreen(Z)V")
	private void onShowDeathScreen(boolean state, CallbackInfo ci) {
		GuiManager hudManager = Tw9ine.getInstance().guiManager;

		if (state && hudManager.isClickGuiOpen()) {
			hudManager.setClickGuiOpen(false);
		}
	}

	@Override
	public void onSetHealth(float health, CallbackInfo ci) {
		PlayerHealthEvent event = new PlayerHealthEvent(null, health);
		Tw9ine.getInstance().eventManager.Fire(event);
	}

	@Override
	protected void onGetOffGroundSpeed(CallbackInfoReturnable<Float> cir) {
		if (Tw9ine.getInstance().moduleManager.fly.state.getValue()) {
			Fly fly = Tw9ine.getInstance().moduleManager.fly;
			cir.setReturnValue((float) fly.getSpeed());
		} else if (Tw9ine.getInstance().moduleManager.noclip.state.getValue()) {
			Noclip noclip = Tw9ine.getInstance().moduleManager.noclip;
			cir.setReturnValue(noclip.getSpeed());
		}
	}

	@Override
	public void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
		Step stepHack = Tw9ine.getInstance().moduleManager.step;
		if (stepHack.state.getValue()) {
			cir.setReturnValue(cir.getReturnValue());
		}
	}

	@Override
	public void onGetJumpVelocityMultiplier(CallbackInfoReturnable<Float> cir) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		HighJump higherJump = tw9ine.moduleManager.higherjump;
		if (higherJump.state.getValue()) {
			cir.setReturnValue(higherJump.getJumpHeightMultiplier());
		}
	}

	@Override
	public void onChangeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
		if (Tw9ine.getInstance().moduleManager.freecam.state.getValue()) {
			float f = (float) cursorDeltaY * 0.15f;
			float g = (float) cursorDeltaX * 0.15f;

			Minecraft mc = Minecraft.getInstance();
			Camera camera = mc.gameRenderer.mainCamera();
			ICamera icamera = (ICamera) camera;

			float newYaw = camera.yRot() + g;
			float newPitch = Math.min(90, Math.max(camera.xRot() + f, -90));

			icamera.setCameraRotation(newYaw, newPitch);
			ci.cancel();
		}
	}

	@Inject(method = "sendChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0))
	private void onTickHasVehicleBeforeSendPackets(CallbackInfo info) {
		SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
		Tw9ine.getInstance().eventManager.Fire(sendMovementPacketPreEvent);
	}

	@Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
	private void onSendMovementPacketsHead(CallbackInfo info) {
		SendMovementPacketEvent.Pre sendMovementPacketPreEvent = new SendMovementPacketEvent.Pre();
		Tw9ine.getInstance().eventManager.Fire(sendMovementPacketPreEvent);
		if (sendMovementPacketPreEvent.isCancelled())
			info.cancel();
	}

	@Inject(method = "sendPosition", at = @At("TAIL"), cancellable = true)
	private void onSendMovementPacketsTail(CallbackInfo info) {
		SendMovementPacketEvent.Post sendMovementPacketPostEvent = new SendMovementPacketEvent.Post();
		Tw9ine.getInstance().eventManager.Fire(sendMovementPacketPostEvent);
		if (sendMovementPacketPostEvent.isCancelled())
			info.cancel();
	}

	@Inject(method = "sendChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 1, shift = At.Shift.AFTER))
	private void onTickHasVehicleAfterSendPackets(CallbackInfo info) {
		SendMovementPacketEvent.Post sendMovementPacketPostEvent = new SendMovementPacketEvent.Post();

		Tw9ine.getInstance().eventManager.Fire(sendMovementPacketPostEvent);
	}
}
