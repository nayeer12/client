/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.gui.components;

import java.util.Arrays;
import java.util.function.Consumer;
import net.aoba.managers.rotation.goals.EasingFunction;
import net.aoba.settings.types.*;
import net.aoba.Aoba;
import net.aoba.event.events.MouseClickEvent;
import net.aoba.gui.GuiManager;
import net.aoba.gui.UIElement;
import net.aoba.gui.UIProperty;
import net.aoba.gui.colors.Color;
import net.aoba.gui.colors.Colors;
import net.aoba.gui.font.FontManager;
import net.aoba.gui.types.BindingMode;
import net.aoba.gui.types.GridDefinition;
import net.aoba.gui.types.Thickness;
import net.aoba.gui.types.VerticalAlignment;
import net.aoba.gui.types.GridDefinition.RelativeUnit;
import net.aoba.module.AntiCheat;
import net.aoba.module.Module;
import net.aoba.rendering.shaders.Shader;
import net.aoba.settings.Setting;
import net.aoba.utils.input.CursorStyle;
import net.aoba.utils.types.MouseAction;
import net.aoba.utils.types.MouseButton;

public class ModuleComponent extends Component {
	private static final Shader TRANSPARENT = Shader.solid(Colors.Transparent);
	private static final Shader DETECTABLE_FOREGROUND = Shader.solid(new Color(85, 85, 90));

	private final StackPanelComponent layout;
	private final RectangleComponent rowComponent;
	private final RectangleComponent accentBar;
	private final StringComponent nameComponent;
	private final StringComponent expandHint;
	private RectangleComponent settingsPanel;
	private boolean expanded = false;

	private final Consumer<Boolean> stateListener = this::moduleStateChanged;
	private final Consumer<AntiCheat> antiCheatListener = _ -> updateStyling();

	public static final UIProperty<Module> ModuleProperty = new UIProperty<>("Module", null, false, false, ModuleComponent::onModulePropertyChanged);

	private static void onModulePropertyChanged(UIElement sender, Module oldValue, Module newValue) {
		if (sender instanceof ModuleComponent moduleComponent) {
			moduleComponent.onModuleChanged(oldValue, newValue);
			moduleComponent.collapseSettings();
			if (newValue == null) {
				moduleComponent.setProperty(UIElement.ToolTipProperty, null);
				moduleComponent.nameComponent.setProperty(StringComponent.TextProperty, "");
				moduleComponent.expandHint.setProperty(UIElement.IsVisibleProperty, false);
				moduleComponent.updateStyling();
				return;
			}
			String tooltip = newValue.getDescription();
			if (newValue.hasSettings())
				tooltip += " (Right-click for settings)";
			moduleComponent.setProperty(UIElement.ToolTipProperty, tooltip);
			moduleComponent.nameComponent.setProperty(StringComponent.TextProperty, newValue.getName());
			moduleComponent.expandHint.setProperty(UIElement.IsVisibleProperty, newValue.hasSettings());
			moduleComponent.updateStyling();
		}
	}

	private void onModuleChanged(Module oldValue, Module newValue) {
		if (oldValue != null)
			oldValue.state.removeOnUpdate(stateListener);

		if (newValue != null)
			newValue.state.addOnUpdate(stateListener);
	}

	private void moduleStateChanged(Boolean state) {
		updateStyling();
	}

	public ModuleComponent() {
		setProperty(UIElement.BorderProperty, TRANSPARENT);
		setProperty(UIElement.BorderThicknessProperty, 0f);

		layout = new StackPanelComponent();
		layout.setSpacing(0f);

		rowComponent = new RectangleComponent();
		rowComponent.setProperty(UIElement.PaddingProperty, new Thickness(4f, 5f));
		rowComponent.setProperty(UIElement.BackgroundProperty, TRANSPARENT);
		rowComponent.setProperty(UIElement.CursorProperty, CursorStyle.Click);
		rowComponent.setOnClicked(this::onRowClicked);

		GridComponent grid = new GridComponent();
		grid.addColumnDefinition(new GridDefinition(RelativeUnit.Auto));
		grid.addColumnDefinition(new GridDefinition(1f, RelativeUnit.Relative));
		grid.addColumnDefinition(new GridDefinition(RelativeUnit.Auto));
		grid.setProperty(UIElement.IsHitTestVisibleProperty, false);

		// Small vertical bar shown to the left of enabled modules.
		accentBar = new RectangleComponent();
		accentBar.setProperty(UIElement.WidthProperty, 2f);
		accentBar.setProperty(UIElement.HeightProperty, 12f);
		accentBar.setProperty(UIElement.CornerRadiusProperty, 1f);
		accentBar.setProperty(UIElement.MarginProperty, new Thickness(0f, 0f, 5f, 0f));
		accentBar.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		accentBar.setProperty(UIElement.IsHitTestVisibleProperty, false);
		accentBar.bindProperty(UIElement.BackgroundProperty, GuiManager.accentColor);
		grid.addChild(accentBar);

		nameComponent = new StringComponent();
		nameComponent.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		nameComponent.setProperty(UIElement.IsHitTestVisibleProperty, false);
		grid.addChild(nameComponent);

		expandHint = new StringComponent("+");
		expandHint.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
		expandHint.setProperty(UIElement.IsHitTestVisibleProperty, false);
		expandHint.bindProperty(UIElement.ForegroundProperty, GuiManager.foregroundAccentColor);
		grid.addChild(expandHint);

		rowComponent.setContent(grid);
		layout.addChild(rowComponent);
		setContent(layout);
		updateStyling();

		AOBA.moduleManager.antiCheat.addOnUpdate(antiCheatListener);
	}

	@Override
	public void dispose() {
		Module module = getProperty(ModuleProperty);
		if (module != null)
			module.state.removeOnUpdate(stateListener);
		AOBA.moduleManager.antiCheat.removeOnUpdate(antiCheatListener);
		super.dispose();
	}

	private void onRowClicked(MouseClickEvent e) {
		if (e.action != MouseAction.DOWN)
			return;

		Module module = getProperty(ModuleComponent.ModuleProperty);
		if (module == null)
			return;

		if (e.button == MouseButton.LEFT) {
			if (!module.isDetectable(AOBA.moduleManager.antiCheat.getValue()))
				module.toggle();
			e.cancel();
		} else if (e.button == MouseButton.RIGHT && module.hasSettings()) {
			if (expanded)
				collapseSettings();
			else
				expandSettings(module);
			e.cancel();
		}
	}

	private void expandSettings(Module module) {
		if (settingsPanel == null) {
			settingsPanel = new RectangleComponent();
			settingsPanel.bindProperty(UIElement.BackgroundProperty, GuiManager.panelBackgroundColor);
			settingsPanel.setProperty(UIElement.CornerRadiusProperty, 4f);
			settingsPanel.setProperty(UIElement.PaddingProperty, new Thickness(6f));
			settingsPanel.setProperty(UIElement.MarginProperty, new Thickness(2f, 2f, 2f, 6f));

			StackPanelComponent stackPanel = new StackPanelComponent();
			stackPanel.setSpacing(6f);
			for (Setting<?> setting : module.getSettings()) {
				if (setting == module.state)
					continue;
				UIElement control = createSettingControl(setting);
				if (control != null)
					stackPanel.addChild(control);
			}
			settingsPanel.setContent(stackPanel);
			layout.addChild(settingsPanel);
		}

		settingsPanel.setProperty(UIElement.IsVisibleProperty, true);
		expandHint.setProperty(StringComponent.TextProperty, "-");
		expanded = true;
	}

	private void collapseSettings() {
		if (settingsPanel != null)
			settingsPanel.setProperty(UIElement.IsVisibleProperty, false);
		expandHint.setProperty(StringComponent.TextProperty, "+");
		expanded = false;
	}

	/**
	 * Creates the UI control used to edit a setting, or null if the setting type
	 * has no editor.
	 */
	public static UIElement createSettingControl(Setting<?> setting) {
		UIElement c;
		if (setting instanceof FloatSetting floatSetting) {
			SliderComponent slider = new SliderComponent();
			slider.setProperty(SliderComponent.MinimumProperty, floatSetting.min_value);
			slider.setProperty(SliderComponent.MaximumProperty, floatSetting.max_value);
			slider.setProperty(SliderComponent.StepProperty, floatSetting.step);
			slider.bindProperty(SliderComponent.ValueProperty, setting, BindingMode.TwoWay);
			slider.setProperty(SliderComponent.HeaderProperty, setting.displayName);
			c = slider;
		} else if (setting instanceof RangeSetting rangeSetting) {
			RangeComponent range = new RangeComponent();
			range.setProperty(RangeComponent.MinimumProperty, rangeSetting.min_value);
			range.setProperty(RangeComponent.MaximumProperty, rangeSetting.max_value);
			range.setProperty(RangeComponent.StepProperty, rangeSetting.step);
			range.bindProperty(RangeComponent.ValueProperty, setting, BindingMode.TwoWay);
			range.setProperty(RangeComponent.HeaderProperty, setting.displayName);
			c = range;
		} else if (setting instanceof BooleanSetting) {
			CheckboxComponent boolCheckbox = new CheckboxComponent();
			boolCheckbox.setProperty(CheckboxComponent.HeaderProperty, setting.displayName);
			boolCheckbox.bindProperty(CheckboxComponent.IsCheckedProperty, setting, BindingMode.TwoWay);
			c = boolCheckbox;
		} else if (setting instanceof ShaderSetting) {
			ExpanderComponent shaderExpander = new ExpanderComponent(setting.displayName);
			ShaderComponent shaderControl = new ShaderComponent();
			shaderControl.bindProperty(ShaderComponent.ShaderProperty, setting, BindingMode.TwoWay);
			shaderExpander.setContent(shaderControl);
			c = shaderExpander;
		} else if (setting instanceof ColorSetting) {
			ColorPickerComponent colorPicker = new ColorPickerComponent();
			colorPicker.bindProperty(ColorPickerComponent.ColorProperty, setting, BindingMode.TwoWay);
			c = colorPicker;
		} else if (setting instanceof BlocksSetting) {
			ExpanderComponent blocksExpander = new ExpanderComponent(setting.displayName);
			blocksExpander.setContent(new BlocksComponent((BlocksSetting) setting));
			c = blocksExpander;
		} else if (setting instanceof EntitiesSetting) {
			ExpanderComponent entitiesExpander = new ExpanderComponent(setting.displayName);
			entitiesExpander.setContent(new EntitiesComponent((EntitiesSetting) setting));
			c = entitiesExpander;
		} else if (setting instanceof EnumSetting) {
			if (setting.getValue() instanceof EasingFunction) {
				ExpanderComponent easingExpander = new ExpanderComponent(setting.displayName);
				EasingComponent easingComponent = new EasingComponent();
				easingComponent.bindProperty(EasingComponent.SelectedValueProperty, setting, BindingMode.TwoWay);
				easingExpander.setContent(easingComponent);
				c = easingExpander;
			} else {
				StackPanelComponent comboStack = new StackPanelComponent();

				StringComponent enumHeader = new StringComponent();
				enumHeader.setProperty(StringComponent.TextProperty, setting.displayName);
				comboStack.addChild(enumHeader);
				ComboBoxComponent comboBox = new ComboBoxComponent();
				comboBox.setProperty(ComboBoxComponent.ItemsSourceProperty, Arrays.asList(setting.getValue().getClass().getEnumConstants()));
				comboBox.bindProperty(ComboBoxComponent.SelectedItemProperty, setting, BindingMode.TwoWay);
				comboStack.addChild(comboBox);
				c = comboStack;
			}
		} else if (setting instanceof HotbarSetting) {
			c = new HotbarComponent((HotbarSetting) setting);
		} else if(setting instanceof KeybindSetting) {
			KeybindComponent keybind = new KeybindComponent();
			keybind.setProperty(KeybindComponent.HeaderProperty, setting.displayName);
			keybind.bindProperty(KeybindComponent.SelectedKeyProperty, setting, BindingMode.TwoWay);
			c = keybind;
		} else {
			c = null;
		}

		return c;
	}

	private void updateStyling() {
		Module module = getProperty(ModuleComponent.ModuleProperty);

		nameComponent.unbindProperty(ForegroundProperty);
		if (module == null) {
			nameComponent.bindProperty(ForegroundProperty, GuiManager.foregroundAccentColor);
			accentBar.setProperty(UIElement.IsVisibleProperty, false);
			return;
		}

		boolean enabled = module.state.getValue();
		accentBar.setProperty(UIElement.IsVisibleProperty, enabled);

		if (module.isDetectable(AOBA.moduleManager.antiCheat.getValue()))
			nameComponent.setProperty(ForegroundProperty, DETECTABLE_FOREGROUND);
		else if (enabled)
			nameComponent.bindProperty(ForegroundProperty, GuiManager.foregroundColor);
		else
			nameComponent.bindProperty(ForegroundProperty, GuiManager.foregroundAccentColor);

		nameComponent.setProperty(UIElement.FontWeightProperty,
				enabled ? FontManager.WEIGHT_MEDIUM : FontManager.WEIGHT_NORMAL);
	}
}
