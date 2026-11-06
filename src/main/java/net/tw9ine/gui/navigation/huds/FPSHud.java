/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.huds;

import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.ResizeMode;
import net.tw9ine.rendering.Renderer2D;

public class FPSHud extends HudWindow {
	public FPSHud(int x, int y) {
		super("FPSHud", x, y, 50, 24);
		setProperty(UIElement.MinWidthProperty, 50f);
		setProperty(UIElement.MinHeightProperty, 20f);
		setProperty(UIElement.MaxHeightProperty, 20f);
		resizeMode = ResizeMode.None;
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		boolean isVisible = getProperty(UIElement.IsVisibleProperty);
		if (isVisible) {
			Rectangle pos = position.getValue();
			int fps = MC.getFps();
			String fpsText = "FPS: " + fps;
			renderer.drawString(fpsText, pos.x(), pos.y(),
					GuiManager.foregroundColor.getValue(), GuiManager.fontSetting.getValue().getRenderer());
		}
		super.draw(renderer, partialTicks);
	}
}