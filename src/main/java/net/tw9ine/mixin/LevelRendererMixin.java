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

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.event.events.Render3DEvent;
import net.tw9ine.rendering.Renderer3D;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	@Inject(method = "render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V", at = @At("TAIL"))
	private void onAfterRenderLevel(GraphicsResourceAllocator resourceAllocator, boolean renderOutline,
			CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky,
			boolean consistentDepthRequired, CallbackInfo ci) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		Minecraft mc = Minecraft.getInstance();
		if (tw9ine == null || tw9ine.moduleManager == null || mc.level == null || mc.player == null)
			return;

		DeltaTracker deltaTracker = mc.getDeltaTracker();
		Camera camera = mc.gameRenderer.mainCamera();
		Matrix4f viewRot = new Matrix4f();
		camera.getViewRotationMatrix(viewRot);

		PoseStack matrixStack = new PoseStack();
		matrixStack.mulPose(viewRot);

		Frustum cullFrustum = cameraState.cullFrustum;

		Renderer3D renderer = tw9ine.render3D;
		renderer.beginFrame(matrixStack, cullFrustum, camera, cameraState, deltaTracker);
		tw9ine.eventManager.Fire(new Render3DEvent(renderer));
		renderer.render();

		if (tw9ine.render2D != null) {
			tw9ine.render2D.captureGameSnapshot();
		}
	}

	@Inject(at = @At("HEAD"), method = "submitBlockOutline(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/LevelRenderState;)V", cancellable = true)
	private void onSubmitBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (Tw9ine.getInstance().moduleManager.freecam.state.getValue())
			ci.cancel();
	}
}
