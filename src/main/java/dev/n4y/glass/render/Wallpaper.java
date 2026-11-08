package dev.n4y.glass.render;

import dev.n4y.glass.N4yGlassClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

/**
 * Fondo de pantalla de n4y Glass. Se dibuja en modo "cover": llena toda la pantalla
 * manteniendo la proporción y recortando lo que sobra, centrado.
 */
public final class Wallpaper {
	public static final Wallpaper DEFAULT = new Wallpaper(N4yGlassClient.id("textures/gui/wallpaper.png"), 1024, 1024);

	private final Identifier texture;
	private final int textureWidth;
	private final int textureHeight;

	public Wallpaper(Identifier texture, int textureWidth, int textureHeight) {
		this.texture = texture;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public void renderCover(GuiGraphics graphics, int width, int height) {
		float screenRatio = (float) width / height;
		float textureRatio = (float) textureWidth / textureHeight;

		int regionWidth = textureWidth;
		int regionHeight = textureHeight;
		if (screenRatio > textureRatio) {
			regionHeight = Math.round(textureWidth / screenRatio);
		} else {
			regionWidth = Math.round(textureHeight * screenRatio);
		}
		float u = (textureWidth - regionWidth) / 2f;
		float v = (textureHeight - regionHeight) / 2f;

		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, u, v, width, height,
				regionWidth, regionHeight, textureWidth, textureHeight);
	}
}
