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

public class TimeHud extends HudWindow {
	private String timeText = null;

	public TimeHud(int x, int y) {
		super("TimeHud", x, y, 80, 24);
		setProperty(UIElement.MinWidthProperty, 80f);
		setProperty(UIElement.MinHeightProperty, 20f);
		setProperty(UIElement.MaxHeightProperty, 20f);
		resizeMode = ResizeMode.None;
	}

	@Override
	public void update() {
		super.update();
		int time = ((int) MC.level.getGameTime() + 6000) % 24000;
		String suffix = time >= 12000 ? "PM" : "AM";
		StringBuilder timeString = new StringBuilder((time / 10) % 1200 + "");
		for (int n = timeString.length(); n < 4; ++n) {
			timeString.insert(0, "0");
		}
		String[] strsplit = timeString.toString().split("");
		String hours = strsplit[0] + strsplit[1];
		if (hours.equalsIgnoreCase("00")) {
			hours = "12";
		}
		int minutes = (int) Math.floor(Double.parseDouble(strsplit[2] + strsplit[3]) / 100.0 * 60.0);
		String sm = minutes + "";
		if (minutes < 10) {
			sm = "0" + minutes;
		}
		timeString = new StringBuilder(hours + ":" + sm.charAt(0) + sm.charAt(1) + suffix);

		timeText = timeString.toString();
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		boolean isVisible = getProperty(UIElement.IsVisibleProperty);
		if (timeText != null && isVisible) {
			Rectangle pos = position.getValue();
			renderer.drawString(timeText, pos.x(), pos.y(),
					GuiManager.foregroundColor.getValue(), GuiManager.fontSetting.getValue().getRenderer());
		}

		super.draw(renderer, partialTicks);
	}
}