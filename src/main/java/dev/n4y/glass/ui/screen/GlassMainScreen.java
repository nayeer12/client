package dev.n4y.glass.ui.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Pantalla principal de n4y Glass.
 * Paso 1: solo fondo desenfocado y título. Los paneles de cristal llegan en el paso 2.
 */
public class GlassMainScreen extends Screen {
	@Nullable
	private final Screen parent;

	public GlassMainScreen(@Nullable Screen parent) {
		super(Component.translatable("n4y_glass.screen.title"));
		this.parent = parent;
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		if (this.minecraft.level == null) {
			// En el menú principal no hay mundo: se desenfoca el panorama.
			this.renderPanorama(graphics, partialTick);
		}
		// Blur nativo de 1.21.11 (blurBeforeThisStratum): desenfoca todo lo dibujado antes.
		this.renderBlurredBackground(graphics);
		// Velo oscuro suave para dar contraste al contenido.
		graphics.fillGradient(0, 0, this.width, this.height, 0x40000000, 0x70000000);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		int cx = this.width / 2;
		int cy = this.height / 2;
		graphics.drawCenteredString(this.font, this.title, cx, cy - 10, 0xFFFFFFFF);
		graphics.drawCenteredString(this.font, Component.translatable("n4y_glass.screen.step1"), cx, cy + 4, 0xB0FFFFFF);
	}

	@Override
	public boolean isPauseScreen() {
		// El mundo sigue vivo detrás del cristal.
		return false;
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parent);
	}
}
