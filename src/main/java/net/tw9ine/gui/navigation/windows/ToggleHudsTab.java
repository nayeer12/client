/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.windows;

import java.util.ArrayList;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.components.HudComponent;
import net.tw9ine.gui.components.SeparatorComponent;
import net.tw9ine.gui.components.StackPanelComponent;
import net.tw9ine.gui.components.StringComponent;
import net.tw9ine.gui.font.FontManager;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.navigation.Window;
import net.tw9ine.gui.types.SizeToContent;
import net.tw9ine.gui.GuiManager;

public class ToggleHudsTab extends Window {
	public ToggleHudsTab(ArrayList<HudWindow> huds) {
		super("Toggle HUDs", 0, 0);
		sizeToContent = SizeToContent.Both;
		StackPanelComponent stackPanel = new StackPanelComponent();
		stackPanel.setSpacing(8f);
		
		StringComponent header = new StringComponent("Toggle HUDs");
		header.setProperty(UIElement.FontWeightProperty, FontManager.WEIGHT_BOLD);
		header.bindProperty(UIElement.ForegroundProperty, GuiManager.foregroundHeaderColor);
		stackPanel.addChild(header);
		
		stackPanel.addChild(new SeparatorComponent());

		for (HudWindow hud : huds) {
			HudComponent hudComponent = new HudComponent(hud.getID(), hud);
			stackPanel.addChild(hudComponent);
		}

		setContent(stackPanel);
		setProperty(UIElement.MinWidthProperty, 300f);
	}
}
