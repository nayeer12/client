/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.components;

import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.types.Size;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;

public class EllipseComponent extends Component {
	public EllipseComponent() {
	}

	@Override
	public Size measure(Size availableSize) {
		Float width = getProperty(UIElement.WidthProperty);
		Float height = getProperty(UIElement.HeightProperty);
		float w = width != null ? width : availableSize.width();
		float h = height != null ? height : availableSize.height();
		return new Size(w, h);
	}
	
	public void draw(Renderer2D renderer, float partialTicks) {
		float actualX = getActualSize().x();
		float actualY = getActualSize().y();
		float actualWidth = getActualSize().width();
		float actualHeight = getActualSize().height();

		float radiusX = actualWidth / 2f;
		float radiusY = actualHeight / 2f;
		float centerX = actualX + radiusX;
		float centerY = actualY + radiusY;

		Shader bgEffect = getProperty(UIElement.BackgroundProperty);
		if (bgEffect != null) {
			renderer.drawEllipse(centerX, centerY, radiusX, radiusY, bgEffect);
		}

		super.draw(renderer, partialTicks);
	}
}
