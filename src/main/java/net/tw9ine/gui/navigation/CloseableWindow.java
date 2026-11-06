/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation;

import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.gui.colors.Color;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;
import net.tw9ine.utils.types.MouseAction;
import net.tw9ine.utils.types.MouseButton;

public class CloseableWindow extends Window {
	private Runnable onClose;
	private static Shader closeButtonShader = Shader.solid(new Color(255, 0, 0, 255));

	public CloseableWindow(String ID, float x, float y) {
		super(ID, x, y);
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		// Check to see if the event is cancelled. If not, execute branch.
		if (!event.isCancelled()) {
			if (event.button == MouseButton.LEFT && event.action == MouseAction.DOWN) {
				float mouseX = (float) event.mouseX;
				float mouseY = (float) event.mouseY;

				Rectangle pos = getActualSize();

				Rectangle closeHitbox = new Rectangle(pos.x() + pos.width() - 24, pos.y() + 4, 16.0f, 16.0f);
				if (closeHitbox.intersects(mouseX, mouseY)) {
					if (onClose != null)
						onClose.run();

					parentPage.removeWindow(this);
					dispose();
					event.cancel();
					return;
				}
			}
		}

		super.onMouseClick(event);
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		super.draw(renderer, partialTicks);
		Rectangle pos = getActualSize();
		float x = pos.x();
		float y = pos.y();
		float width = pos.width();
		renderer.drawLine(x + width - 23, y + 8, x + width - 8, y + 23, closeButtonShader);
		renderer.drawLine(x + pos.width() - 23, pos.y() + 23, x + width - 8, y + 8, closeButtonShader);
	}

	public void setOnClose(Runnable runnable) {
		onClose = runnable;
	}
}
