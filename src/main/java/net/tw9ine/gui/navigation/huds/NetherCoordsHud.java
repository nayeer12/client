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

public class NetherCoordsHud extends HudWindow {
	public NetherCoordsHud(int x, int y) {
		super("NetherCoordsHud", x, y, 50, 24);
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
			String coordsText = String.format("X: %.1f, Y: %.1f, Z: %.1f", MC.player.getX() * 8, MC.player.getY(),
					MC.player.getZ() * 8);
			renderer.drawString(coordsText, pos.x(), pos.y(),
					GuiManager.foregroundColor.getValue(), GuiManager.fontSetting.getValue().getRenderer());
		}

		super.draw(renderer, partialTicks);
	}
}
