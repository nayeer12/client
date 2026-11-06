/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.huds;

import net.minecraft.client.gui.Font;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.colors.Color;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;

/**
 * Shared colours and helpers for the HUD. Shaders are created once and reused
 * every frame to keep the HUD cheap to draw.
 */
final class HudStyle {
	/** Text is drawn at twice the font's native size. */
	static final float TEXT_SCALE = 2f;

	private static Shader background;
	private static Shader text;
	private static Shader mutedText;
	private static Shader barTrack;
	private static Shader danger;

	private HudStyle() {
	}

	static Shader background() {
		if (background == null)
			background = Shader.solid(new Color(14, 14, 16, 175));
		return background;
	}

	static Shader text() {
		if (text == null)
			text = Shader.solid(new Color(236, 238, 240));
		return text;
	}

	static Shader mutedText() {
		if (mutedText == null)
			mutedText = Shader.solid(new Color(150, 150, 156));
		return mutedText;
	}

	static Shader barTrack() {
		if (barTrack == null)
			barTrack = Shader.solid(new Color(255, 255, 255, 30));
		return barTrack;
	}

	static Shader danger() {
		if (danger == null)
			danger = Shader.solid(new Color(232, 96, 96));
		return danger;
	}

	static Shader accent() {
		return GuiManager.accentColor.getValue();
	}

	static Font font() {
		return GuiManager.fontSetting.getValue().getRenderer();
	}

	static float width(String s) {
		return Renderer2D.getStringWidth(s, font()) * TEXT_SCALE;
	}

	static float lineHeight() {
		return font().lineHeight * TEXT_SCALE;
	}

	/**
	 * Draws text and returns the x position right after it.
	 */
	static float draw(Renderer2D renderer, String s, float x, float y, Shader shader) {
		renderer.drawString(s, x, y, shader, font());
		return x + width(s);
	}

	/** Draws the tw9ine logo text with the accent "9" and returns the end x. */
	static float drawLogo(Renderer2D renderer, float x, float y) {
		x = draw(renderer, "tw", x, y, text());
		x = draw(renderer, "9", x, y, accent());
		return draw(renderer, "ine", x, y, text());
	}
}
