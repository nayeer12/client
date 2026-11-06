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
import net.tw9ine.gui.UIProperty;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;

public class PolygonComponent extends Component {

	public static final UIProperty<float[]> PolygonProperty = new UIProperty<>("Polygon", null, false, true);

	public PolygonComponent() {
	}

	public PolygonComponent(float[] polygon) {
		setProperty(PolygonProperty, polygon);
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		Shader fgEffect = getProperty(UIElement.ForegroundProperty);
		float[] polygon = getProperty(PolygonProperty);

		if (fgEffect != null && polygon != null) {
			float actualX = getActualSize().x();
			float actualY = getActualSize().y();
			float actualWidth = getActualSize().width();
			float actualHeight = getActualSize().height();
			renderer.drawPolygon(actualX, actualY, actualWidth, actualHeight, polygon, fgEffect);
		}

		super.draw(renderer, partialTicks);
	}
}
