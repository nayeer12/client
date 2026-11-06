package net.tw9ine.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.gui.GuiManager;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
	@Unique
	private final CubeMap tw9ine$cubeMap = new CubeMap(Identifier.fromNamespaceAndPath("tw9ine", "textures/mainmenu/panorama"));

	@Inject(method = "render", at = @At("TAIL"))
	private void onRenderTail(CallbackInfo ci) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		if (tw9ine != null && tw9ine.render2D != null) {
			tw9ine.render2D.render();
		}
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CubeMap;render(FF)V"))
	private void tw9ine$redirectPanoramaCubeMap(CubeMap instance, float rotX, float rotY) {
		Tw9ineClient tw9ine = Tw9ine.getInstance();
		if (tw9ine != null && tw9ine.guiManager != null && GuiManager.enableCustomTitle.getValue()) {
			this.tw9ine$cubeMap.render(rotX, rotY);
		}else {
			instance.render(rotX, rotY);
		}
	}

	@Inject(method = "registerPanoramaTextures", at = @At("TAIL"))
	private void tw9ine$registerPanoramaTextures(TextureManager textureManager, CallbackInfo ci) {
		this.tw9ine$cubeMap.registerTextures(textureManager);
	}

	@Inject(method = "close", at = @At("TAIL"))
	private void tw9ine$closePanoramaCubeMap(CallbackInfo ci) {
		this.tw9ine$cubeMap.close();
	}
}
