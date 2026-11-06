/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.windows;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.components.ButtonComponent;
import net.tw9ine.gui.components.ComboBoxComponent;
import net.tw9ine.gui.components.GridComponent;
import net.tw9ine.gui.components.SeparatorComponent;
import net.tw9ine.gui.components.StackPanelComponent;
import net.tw9ine.gui.components.StringComponent;
import net.tw9ine.gui.components.TextBoxComponent;
import net.tw9ine.gui.font.FontManager;
import net.tw9ine.gui.navigation.Window;
import net.tw9ine.gui.types.GridDefinition;
import net.tw9ine.gui.types.SizeToContent;
import net.tw9ine.gui.types.TextAlign;
import net.tw9ine.gui.types.VerticalAlignment;
import net.tw9ine.gui.types.GridDefinition.RelativeUnit;
import net.tw9ine.managers.SettingManager;

/**
 * Represents the Settings window that contains a list of all of the available
 * toggle-able settings.
 */
public class SettingsWindow extends Window {
	private final TextBoxComponent fileName;
	private final ComboBoxComponent configNames;

	public SettingsWindow() {
		super("Settings", 50, 825);
		sizeToContent = SizeToContent.Both;
		StackPanelComponent stackPanel = new StackPanelComponent();
		stackPanel.setSpacing(8f);

		// Title Bar
		StringComponent headerText = new StringComponent("Settings Manager");
		headerText.setProperty(UIElement.FontWeightProperty, FontManager.WEIGHT_BOLD);
		headerText.bindProperty(ForegroundProperty, GuiManager.foregroundHeaderColor);
		stackPanel.addChild(headerText);
		stackPanel.addChild(new SeparatorComponent());

		stackPanel.addChild(new StringComponent("Profile:"));

		GridComponent loadConfigGrid = new GridComponent();
		loadConfigGrid.setProperty(GridComponent.HorizontalSpacingProperty, 8f);
		loadConfigGrid.setProperty(GridComponent.VerticalSpacingProperty, 8f);
		loadConfigGrid.addColumnDefinition(new GridDefinition(1, RelativeUnit.Relative));
		loadConfigGrid.addColumnDefinition(new GridDefinition(100, RelativeUnit.Absolute));

		configNames = new ComboBoxComponent();
		configNames.setProperty(ComboBoxComponent.ItemsSourceProperty, SettingManager.configNames);
		if (!SettingManager.configNames.isEmpty())
			configNames.setProperty(ComboBoxComponent.SelectedItemProperty, SettingManager.configNames.get(0));
		loadConfigGrid.addChild(configNames);

		// Load Config Button
		ButtonComponent btnLoadConfig = new ButtonComponent(() -> {
			Object selectedValue = configNames.getProperty(ComboBoxComponent.SelectedItemProperty);
			if(selectedValue instanceof String selected) {
				if (selected != null)
					SettingManager.setCurrentConfig(selected);
			}
		});

		StringComponent buttonLoadString = new StringComponent("Load");
		buttonLoadString.setProperty(StringComponent.TextAlignmentProperty, TextAlign.Center);
		buttonLoadString.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		btnLoadConfig.setContent(buttonLoadString);

		loadConfigGrid.addChild(btnLoadConfig);
		stackPanel.addChild(loadConfigGrid);

		// Grid containing config name textbox and save button.
		GridComponent fileSaveGrid = new GridComponent();
		fileSaveGrid.setProperty(GridComponent.HorizontalSpacingProperty, 8f);
		fileSaveGrid.setProperty(GridComponent.VerticalSpacingProperty, 8f);
		fileSaveGrid.addColumnDefinition(new GridDefinition(1, RelativeUnit.Relative));
		fileSaveGrid.addColumnDefinition(new GridDefinition(100, RelativeUnit.Absolute));

		// Config Name TextBox
		fileName = new TextBoxComponent();
		fileSaveGrid.addChild(fileName);

		// Save Button
		ButtonComponent btnSaveACopy = new ButtonComponent(() -> {
			String newFileName = fileName.getProperty(TextBoxComponent.TextProperty);
			if (!newFileName.isBlank()) {
				try {
					SettingManager.saveCopy(newFileName);
					SettingManager.refreshSettingFiles();
					configNames.setProperty(ComboBoxComponent.ItemsSourceProperty, SettingManager.configNames);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					fileName.setProperty(TextBoxComponent.TextProperty, "");
				}
			}
		});

		// Text inside of save button.
		StringComponent buttonStr = new StringComponent("Save");
		buttonStr.setProperty(StringComponent.TextAlignmentProperty, TextAlign.Center);
		buttonStr.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		btnSaveACopy.setContent(buttonStr);
		fileSaveGrid.addChild(btnSaveACopy);
		stackPanel.addChild(fileSaveGrid);
		setContent(stackPanel);

		setProperty(UIElement.MinWidthProperty, 300.0f);
	}
}
