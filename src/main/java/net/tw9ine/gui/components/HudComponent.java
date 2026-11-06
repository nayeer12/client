/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.components;

import net.tw9ine.Tw9ine;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.colors.Color;
import net.tw9ine.gui.colors.Colors;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.types.GridDefinition;
import net.tw9ine.gui.types.VerticalAlignment;
import net.tw9ine.gui.types.GridDefinition.RelativeUnit;
import net.tw9ine.rendering.shaders.Shader;
import net.tw9ine.utils.types.MouseAction;
import net.tw9ine.utils.types.MouseButton;

public class HudComponent extends Component {
	private final HudWindow hud;
	private final StringComponent statusComponent;

	public HudComponent(String text, HudWindow hud) {
		this.hud = hud;
		GridComponent grid = new GridComponent();
		grid.addColumnDefinition(new GridDefinition(1f, RelativeUnit.Relative));
		grid.addColumnDefinition(new GridDefinition(RelativeUnit.Auto));

		StringComponent nameComponent = new StringComponent(text);
		nameComponent.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		grid.addChild(nameComponent);

		statusComponent = new StringComponent(hud.activated.getValue() ? "-" : "+");
		statusComponent.setProperty(ForegroundProperty, Shader.solid(hud.activated.getValue() ? new Color(255, 0, 0) : new Color(0, 255, 0)));
		statusComponent.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		grid.addChild(statusComponent);

		setContent(grid);

		setOnClicked(e -> {
			if (e.button == MouseButton.LEFT && e.action == MouseAction.DOWN) {
				boolean visibility = hud.activated.getValue();
				Tw9ine.getInstance().guiManager.setHudActive(hud, !visibility);
				e.cancel();
			}
		});
	}

	@Override
	public void update() {
		super.update();

		if (hud.activated.getValue()) {
			statusComponent.setProperty(StringComponent.TextProperty, "-");
			statusComponent.setProperty(ForegroundProperty, Shader.solid(Colors.Red));
		} else {
			statusComponent.setProperty(StringComponent.TextProperty, "+");
			statusComponent.setProperty(ForegroundProperty, Shader.solid(Colors.Green));
		}
	}
}
