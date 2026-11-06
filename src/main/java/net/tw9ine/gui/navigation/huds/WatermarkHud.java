/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.huds;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.ResizeMode;
import net.tw9ine.rendering.Renderer2D;

/**
 * Watermark: the tw9ine logo with FPS and ping in a small pill.
 */
public class WatermarkHud extends HudWindow {
	private static final float PAD_X = 10f;
	private static final float PAD_Y = 6f;

	public WatermarkHud(int x, int y) {
		super("WatermarkHud", x, y, 50, 24);
		setProperty(UIElement.MinWidthProperty, 20f);
		setProperty(UIElement.MinHeightProperty, 10f);
		resizeMode = ResizeMode.None;
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		if (getProperty(UIElement.IsVisibleProperty)) {
			Rectangle pos = position.getValue();
			if (pos.x() == 0 && pos.y() == 0) {
				position.setX(12f);
				position.setY(12f);
				pos = position.getValue();
			}

			String info = "  " + MC.getFps() + " fps" + pingText();
			float lineHeight = HudStyle.lineHeight();
			float width = PAD_X * 2 + HudStyle.width("tw9ine") + HudStyle.width(info);
			float height = PAD_Y * 2 + lineHeight;
			resizeTo(width, height);

			renderer.drawRoundedBox(pos.x(), pos.y(), width, height, 6f, HudStyle.background());
			float textY = pos.y() + PAD_Y;
			float x = HudStyle.drawLogo(renderer, pos.x() + PAD_X, textY);
			HudStyle.draw(renderer, info, x, textY, HudStyle.mutedText());
		}

		super.draw(renderer, partialTicks);
	}

	private void resizeTo(float width, float height) {
		if (Math.abs(position.getWidth() - width) > 0.5f)
			position.setWidth(width);
		if (Math.abs(position.getHeight() - height) > 0.5f)
			position.setHeight(height);
	}

	private static String pingText() {
		if (MC.player == null || MC.getConnection() == null)
			return "";
		PlayerInfo info = MC.getConnection().getPlayerInfo(MC.player.getUUID());
		if (info == null)
			return "";
		return "  " + info.getLatency() + " ms";
	}
}
