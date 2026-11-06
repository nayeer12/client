/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.tw9ine.Tw9ine;
import net.tw9ine.Tw9ineClient;
import net.tw9ine.event.events.KeyDownEvent;
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.event.events.MouseMoveEvent;
import net.tw9ine.event.events.MouseScrollEvent;
import org.joml.Matrix3x2fStack;
import net.tw9ine.event.events.Render2DEvent;
import net.tw9ine.event.events.TickEvent;
import net.tw9ine.event.listeners.KeyDownListener;
import net.tw9ine.event.listeners.MouseClickListener;
import net.tw9ine.event.listeners.MouseMoveListener;
import net.tw9ine.event.listeners.MouseScrollListener;
import net.tw9ine.event.listeners.Render2DListener;
import net.tw9ine.event.listeners.TickListener;
import net.tw9ine.gui.colors.Color;
import net.tw9ine.gui.colors.Colors;
import net.tw9ine.gui.font.FontManager;
import net.tw9ine.gui.components.GridComponent;
import net.tw9ine.gui.types.Thickness;
import net.tw9ine.gui.components.ModuleComponent;
import net.tw9ine.gui.components.TabComponent;
import net.tw9ine.gui.components.TabItemComponent;
import net.tw9ine.gui.components.StackPanelComponent;
import net.tw9ine.gui.components.StringComponent;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.navigation.Page;
import net.tw9ine.gui.navigation.Popup;
import net.tw9ine.gui.navigation.Window;
import net.tw9ine.gui.navigation.huds.ArmorHud;
import net.tw9ine.gui.navigation.huds.CoordsHud;
import net.tw9ine.gui.navigation.huds.DayHud;
import net.tw9ine.gui.navigation.huds.FPSHud;
import net.tw9ine.gui.navigation.huds.InputHud;
import net.tw9ine.gui.navigation.huds.ModuleArrayListHud;
import net.tw9ine.gui.navigation.huds.ModuleSelectorHud;
import net.tw9ine.gui.navigation.huds.NetherCoordsHud;
import net.tw9ine.gui.navigation.huds.PingHud;
import net.tw9ine.gui.navigation.huds.RadarHud;
import net.tw9ine.gui.navigation.huds.SpeedHud;
import net.tw9ine.gui.navigation.huds.TimeHud;
import net.tw9ine.gui.navigation.huds.WatermarkHud;
import net.tw9ine.gui.navigation.windows.AntiCheatWindow;
import net.tw9ine.gui.navigation.windows.AuthCrackerWindow;
import net.tw9ine.gui.navigation.windows.FriendsWindow;
import net.tw9ine.gui.navigation.windows.PathfindingWindow;
import net.tw9ine.gui.navigation.windows.SearchWindow;
import net.tw9ine.gui.navigation.windows.UIOptionsWindow;
import net.tw9ine.gui.types.GridDefinition;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.Size;
import net.tw9ine.gui.types.SizeToContent;
import net.tw9ine.gui.types.VerticalAlignment;
import net.tw9ine.gui.types.GridDefinition.RelativeUnit;
import net.tw9ine.gui.navigation.windows.MacroWindow;
import net.tw9ine.gui.navigation.windows.SettingsWindow;
import net.tw9ine.gui.navigation.windows.ToggleHudsTab;
import net.tw9ine.managers.SettingManager;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.ShaderSetting;
import net.tw9ine.settings.types.FloatSetting;
import net.tw9ine.settings.types.FontSetting;
import net.tw9ine.settings.types.KeybindSetting;
import net.tw9ine.utils.input.CursorStyle;
import net.tw9ine.utils.input.Input;
import net.tw9ine.utils.input.Keybind;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.rendering.shaders.Shader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class GuiManager implements KeyDownListener, TickListener, Render2DListener, MouseClickListener,
		MouseMoveListener, MouseScrollListener {
	private static final Minecraft MC = Minecraft.getInstance();
	
	public KeybindSetting clickGuiButton = KeybindSetting.builder().id("key.clickgui").displayName("ClickGUI Key")
			.defaultValue(Keybind.from(InputConstants.KEY_RSHIFT)).build();
	
	private static CursorStyle currentCursor = CursorStyle.Default;
	private static String tooltip = null;
	private final KeyMapping esc = new KeyMapping("key.esc", InputConstants.KEY_ESCAPE, Tw9ineClient.TW9INE_CATEGORY);

	private boolean clickGuiOpen = false;
	/** Blurs and darkens the world behind the ClickGUI. */
	private static final Shader CLICKGUI_BACKDROP = Shader.blur(new Color(10, 10, 12, 110), 24f, 5f);
	private static boolean isKeyboardInputActive = false;
	private static UIElement focusedElement = null;
	private final HashMap<Object, Window> pinnedHuds = new HashMap<Object, Window>();
	private Popup popup;

	// Tab Control and Pages
	public TabComponent clickGuiTabControl;
	private final List<Page> pages = new ArrayList<>();
	public Page modulesPane = new Page("Modules");
	public Page toolsPane = new Page("Tools");
	public Page hudPane = new Page("Hud");

	// Global HUD Settings
	public static BooleanSetting enableCustomTitle = BooleanSetting.builder().id("enable_custom_title")
			.displayName("Enable Custom Title Screen").defaultValue(true).build();

	public static BooleanSetting enableTooltips = BooleanSetting.builder().id("enable_tooltips")
			.displayName("Enable Tooltips").defaultValue(true).build();

	public static ShaderSetting foregroundColor = ShaderSetting.builder().id("tw9ine_ui_foreground_color")
			.displayName("UI Foreground Color").description("Color of the foreground.")
			.defaultValue(Shader.solid(Colors.White)).build();

	public static ShaderSetting foregroundHeaderColor = ShaderSetting.builder().id("tw9ine_ui_header_foreground_color")
			.displayName("UI Header Foreground Color").description("Color of the header foreground.")
			.defaultValue(Shader.solid(new Color(236, 238, 240))).build();

	public static ShaderSetting foregroundAccentColor = ShaderSetting.builder().id("tw9ine_ui_accent_foreground_color")
			.displayName("UI Accent Foreground Color").description("Color of the accented foreground.")
			.defaultValue(Shader.solid(new Color(138, 138, 142))).build();

	public static ShaderSetting windowBorderColor = ShaderSetting.builder().id("tw9ine_ui_window_border_color")
			.displayName("UI Border Color").description("Color of the borders.")
			.defaultValue(Shader.solid(new Color(0, 0, 0, 0))).build();

	public static ShaderSetting panelBorderColor = ShaderSetting.builder().id("tw9ine_ui_panel_border_color")
			.displayName("UI Panel Border Color").description("Accent color of the background.")
			.defaultValue(Shader.solid(new Color(255, 255, 255, 14))).build();

	public static ShaderSetting componentBackgroundColor = ShaderSetting.builder().id("tw9ine_ui_component_background_color")
			.displayName("UI Component Background Color").description("Background colors of components (i.e TextBox).")
			.defaultValue(Shader.solid(new Color(42, 42, 45, 200))).build();
	
	public static ShaderSetting componentHoveredBackgroundColor = ShaderSetting.builder().id("tw9ine_ui_component_hover_background_color")
			.displayName("UI Component Hovered Background Color").description("Background colors of components (i.e TextBox) when hovered.")
			.defaultValue(Shader.solid(new Color(52, 52, 56, 220))).build();
	
	public static ShaderSetting componentBorderColor = ShaderSetting.builder().id("tw9ine_ui_component_border_color")
			.displayName("UI Component Border Color").description("Background colors of components (i.e TextBox).")
			.defaultValue(Shader.solid(new Color(255, 255, 255, 20))).build();
	
	public static ShaderSetting windowBackgroundColor = ShaderSetting.builder().id("tw9ine_ui_window_background_color")
			.displayName("UI Background Color").description("Color of the background.")
			.defaultValue(Shader.blur(new Color(26, 26, 28, 235), 40f, 5f)).build();

	public static ShaderSetting panelBackgroundColor = ShaderSetting.builder().id("tw9ine_ui_panel_background_color")
			.displayName("UI Panel Background Color").description("Accent color of the background.")
			.defaultValue(Shader.solid(new Color(38, 38, 41, 220))).build();

	public static ShaderSetting buttonBackgroundColor = ShaderSetting.builder().id("tw9ine_ui_button_background_color")
			.displayName("UI Button Background Color").description("Background color of buttons.")
			.defaultValue(Shader.solid(new Color(56, 56, 60, 230))).build();

	public static ShaderSetting buttonHoverBackgroundColor = ShaderSetting.builder()
			.id("tw9ine_ui_button_hover_background_color").displayName("UI Button Hover Background Color")
			.description("Background color of buttons when hovered.")
			.defaultValue(Shader.solid(new Color(68, 68, 73, 235))).build();
	
	public static ShaderSetting buttonPressedBackgroundColor = ShaderSetting.builder()
			.id("tw9ine_ui_button_pressed_background_color").displayName("UI Button Pressed Background Color")
			.description("Background color of buttons when pressed.")
			.defaultValue(Shader.solid(new Color(46, 46, 50, 235))).build();

	public static ShaderSetting buttonBorderColor = ShaderSetting.builder().id("tw9ine_ui_button_border_color")
			.displayName("UI Button Border Color").description("Border color of buttons.")
			.defaultValue(Shader.solid(new Color(0, 0, 0, 0))).build();

	public static ShaderSetting accentColor = ShaderSetting.builder().id("tw9ine_ui_accent_color")
			.displayName("UI Accent Color")
			.description("Highlight color for enabled modules, checkboxes, sliders and the module list.")
			.defaultValue(Shader.solid(new Color(156, 200, 220))).build();

	public static FloatSetting roundingRadius = FloatSetting.builder().id("tw9ine_hud_rounding_radius")
			.displayName("Corner Rounding").description("The radius of the rounding on hud.").defaultValue(6f)
			.minValue(0f).maxValue(20f).step(1f).build();

	public static FloatSetting lineThickness = FloatSetting.builder().id("tw9ine_ui_line_thickness")
			.displayName("Line Thickness").description("The thickness of borders and outlines.").defaultValue(0.5f)
			.minValue(0.5f).maxValue(4f).step(0.5f).build();

	public static FontSetting fontSetting = FontSetting.builder().id("tw9ine_font").displayName("Font")
			.description("The font that tw9ine will use.").fontName(FontManager.DEFAULT_FONT).build();

	public ModuleSelectorHud moduleSelector;
	public ArmorHud armorHud;
	public RadarHud radarHud;
	public TimeHud timeHud;
	public DayHud dayHud;
	public ModuleArrayListHud moduleArrayListHud;
	public WatermarkHud watermarkHud;
	public CoordsHud coordsHud;
	public NetherCoordsHud netherCoordsHud;
	public FPSHud fpsHud;
	public PingHud pingHud;
	public SpeedHud speedHud;
	public InputHud inputHud;

	public GuiManager() {
		clickGuiTabControl = new TabComponent();

		SettingManager.registerGlobalSetting(windowBorderColor);
		SettingManager.registerGlobalSetting(windowBackgroundColor);
		SettingManager.registerGlobalSetting(panelBorderColor);
		SettingManager.registerGlobalSetting(panelBackgroundColor);
		SettingManager.registerGlobalSetting(componentBackgroundColor);
		SettingManager.registerGlobalSetting(componentHoveredBackgroundColor);
		SettingManager.registerGlobalSetting(foregroundColor);
		SettingManager.registerGlobalSetting(foregroundHeaderColor);
		SettingManager.registerGlobalSetting(foregroundAccentColor);
		SettingManager.registerGlobalSetting(buttonBackgroundColor);
		SettingManager.registerGlobalSetting(buttonHoverBackgroundColor);
		SettingManager.registerGlobalSetting(buttonPressedBackgroundColor);
		SettingManager.registerGlobalSetting(buttonBorderColor);
		SettingManager.registerGlobalSetting(accentColor);
		SettingManager.registerGlobalSetting(roundingRadius);
		SettingManager.registerGlobalSetting(lineThickness);
		SettingManager.registerGlobalSetting(fontSetting);

		SettingManager.registerSetting(clickGuiButton);
		Tw9ine.getInstance().eventManager.AddListener(KeyDownListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(Render2DListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(MouseClickListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(MouseMoveListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(MouseScrollListener.class, this);
	}

	public void Initialize() {
		System.out.println("Initializing");
		toolsPane.addWindow(new AuthCrackerWindow());
		toolsPane.addWindow(new PathfindingWindow());
		toolsPane.addWindow(new MacroWindow());
		
		moduleSelector = new ModuleSelectorHud();
		armorHud = new ArmorHud(0, 0);
		radarHud = new RadarHud(0, 0);
		timeHud = new TimeHud(0, 0);
		dayHud = new DayHud(0, 0);
		moduleArrayListHud = new ModuleArrayListHud(0, 0);
		watermarkHud = new WatermarkHud(0, 0);
		coordsHud = new CoordsHud(0, 0);
		netherCoordsHud = new NetherCoordsHud(0, 0);
		fpsHud = new FPSHud(0, 0);
		pingHud = new PingHud(0, 0);
		speedHud = new SpeedHud(0, 0);
		inputHud = new InputHud(0,0);

		ArrayList<HudWindow> huds = Lists.newArrayList(moduleSelector, armorHud, radarHud, timeHud, dayHud,
				moduleArrayListHud, watermarkHud, coordsHud, netherCoordsHud, fpsHud, pingHud, speedHud, inputHud);
		hudPane.addWindow(new UIOptionsWindow());
		hudPane.addWindow(new ToggleHudsTab(huds));
		Map<String, Category> categories = Category.getAllCategories();
		float xOffset = 50;

		for (Category category : categories.values()) {
			Window tab = new Window(category.getName(), xOffset, 75.0f);
			tab.setSizeToContent(SizeToContent.Height);
			StackPanelComponent stackPanel = new StackPanelComponent();
			stackPanel.setSpacing(0f);

			GridComponent gridComponent = new GridComponent();
			gridComponent.setProperty(GridComponent.HorizontalSpacingProperty, 8f);
			gridComponent.setProperty(UIElement.IsHitTestVisibleProperty, false);
			gridComponent.addColumnDefinition(new GridDefinition(1, RelativeUnit.Relative));
			gridComponent.setProperty(UIElement.MarginProperty, new Thickness(2f, 0f, 0f, 8f));

			StringComponent title = new StringComponent();
			title.setProperty(StringComponent.TextProperty, category.getName());
			title.setProperty(UIElement.FontWeightProperty, FontManager.WEIGHT_SEMI_BOLD);
			title.setProperty(UIElement.VerticalAlignmentProperty, VerticalAlignment.Center);
			title.setProperty(UIElement.IsHitTestVisibleProperty, false);
			title.bindProperty(UIElement.ForegroundProperty, GuiManager.foregroundHeaderColor);
			gridComponent.addChild(title);

			stackPanel.addChild(gridComponent);

			// Loop through modules and add them to the correct category
			for (Module module : Tw9ine.getInstance().moduleManager.modules) {
				if (module.getCategory().equals(category)) {
					ModuleComponent button = new ModuleComponent();
					button.setProperty(ModuleComponent.ModuleProperty, module);
					stackPanel.addChild(button);
				}
			}

			tab.setContent(stackPanel);
			tab.setProperty(UIElement.MinWidthProperty, 200f);
			modulesPane.addWindow(tab);

			xOffset += tab.getProperty(UIElement.MinWidthProperty) + 10;
		}

		SearchWindow searchWindow = new SearchWindow();
		searchWindow.position.setX(xOffset);
		searchWindow.position.setY(73f);
		modulesPane.addWindow(searchWindow);
		modulesPane.addWindow(new SettingsWindow());
		modulesPane.addWindow(new AntiCheatWindow());
		modulesPane.addWindow(new FriendsWindow());

		pages.add(modulesPane);
		pages.add(toolsPane);
		pages.add(hudPane);

		clickGuiTabControl.addChild(new TabItemComponent("Modules"));
		clickGuiTabControl.addChild(new TabItemComponent("Tools"));
		clickGuiTabControl.addChild(new TabItemComponent("Hud"));
		clickGuiTabControl.setOnSelectionChanged(idx -> {
			for (int i = 0; i < pages.size(); i++)
				pages.get(i).setVisible(i == idx);
		});
		clickGuiTabControl.initialize();

		modulesPane.initialize();
		toolsPane.initialize();
		hudPane.initialize();

		clickGuiTabControl.setProperty(TabComponent.SelectedIndexProperty, 0);
		modulesPane.setVisible(true);
	}

	public static CursorStyle getCursor() {
		return currentCursor;
	}

	public static void setCursor(CursorStyle cursor) {
		currentCursor = cursor;
		Input.setCursorStyle(currentCursor);
	}

	public static String getTooltip() {
		return tooltip;
	}

	public static void setTooltip(String tt) {
		if (!Objects.equals(tooltip, tt))
			tooltip = tt;
	}

	public static boolean isKeyboardInputActive() {
		return isKeyboardInputActive;
	}

	public static void setKeyboardInputActive(boolean state) {
		isKeyboardInputActive = state;
	}

	public static UIElement getFocusedElement() {
		return focusedElement;
	}

	public static void requestFocus(UIElement element) {
		if (focusedElement == element)
			return;

		if (focusedElement != null) {
			focusedElement.onLostFocus();
		}

		focusedElement = element;
		isKeyboardInputActive = element != null;

		if (focusedElement != null) {
			focusedElement.onGotFocus();
		}
	}

	public static void clearFocus(UIElement element) {
		if (focusedElement == element) {
			focusedElement = null;
			isKeyboardInputActive = false;

			if (element != null) {
				element.onLostFocus();
			}
		}
	}

	public void addWindow(Window hud, String pageName) {
		for (Page page : pages) {
			if (page.getTitle().equals(pageName)) {
				page.addWindow(hud);
				page.moveToFront(hud);
				hud.initialize();
				break;
			}
		}
	}

	public void removeWindow(Window hud, String pageName) {
		for (Page page : pages) {
			if (page.getTitle().equals(pageName)) {
				page.removeWindow(hud);
				break;
			}
		}
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		Keybind clickguiKeybind = clickGuiButton.getValue();
		if (clickguiKeybind.getKeyCode() == event.GetKey() && MC.gui.screen() == null && !isKeyboardInputActive) {
			setClickGuiOpen(!clickGuiOpen);
			toggleMouse();
		}
	}

	public void setHudActive(HudWindow hud, boolean state) {
		if (state) {
			pinnedHuds.put(hud.getClass(), hud);
			hud.activated.silentSetValue(true);
			hudPane.addWindow(hud);
			hud.setProperty(UIElement.IsVisibleProperty, true);
		} else {
			pinnedHuds.remove(hud.getClass());
			hud.activated.silentSetValue(false);
			hudPane.removeWindow(hud);
			hud.setProperty(UIElement.IsVisibleProperty, false);
		}
	}

	@Override
	public void onTick(TickEvent.Pre event) {

	}

	@Override
	public void onTick(TickEvent.Post event) {
		if (clickGuiOpen) {
			clickGuiTabControl.update();
			int selectedIdx = clickGuiTabControl.getSelectedIndex();
			if (selectedIdx >= 0 && selectedIdx < pages.size())
				pages.get(selectedIdx).update();
		}

		/**
		 * Updates each of the Tab GUIs that are currently on the screen.
		 */
		for (Window hud : pinnedHuds.values()) {
			hud.update();
		}

		if (esc.isDown() && clickGuiOpen) {
			clickGuiOpen = false;
			toggleMouse();
		}
	}

	@Override
	public void onRender(Render2DEvent event) {
		Renderer2D renderer = event.getRenderer();
		GuiGraphicsExtractor drawContext = renderer.getDrawContext();
		float tickDelta = renderer.getDeltaTracker().getGameTimeDeltaPartialTick(false);

		Matrix3x2fStack matrixStack = drawContext.pose();
		matrixStack.pushMatrix();

		com.mojang.blaze3d.platform.Window window = MC.getWindow();

		// The UI is laid out in window (screen) coordinates, which is the same space GLFW
		// reports the mouse in. On HiDPI displays (e.g. macOS Retina) the framebuffer is
		// larger than the window, so scale from screen coordinates to framebuffer pixels
		// before dividing out the GUI scale. Otherwise rendering and mouse input disagree.
		int guiScale = window.getGuiScale();
		float pixelRatio = window.getScreenWidth() > 0 ? (float) window.getWidth() / window.getScreenWidth() : 1.0f;
		float uiScale = pixelRatio / guiScale;
		matrixStack.scale(uiScale, uiScale);
		// Render ClickGUI and Topbar
		if (clickGuiOpen) {
			renderer.drawBox(0, 0, window.getScreenWidth(), window.getScreenHeight(), CLICKGUI_BACKDROP);

			// Measure and position TabComponent at top-center
			clickGuiTabControl.measureCore(new Size(window.getScreenWidth(), window.getScreenHeight()));
			Size tabSize = clickGuiTabControl.getPreferredSize();
			float tabX = (window.getScreenWidth() - tabSize.width()) / 2f;
			clickGuiTabControl.arrange(new Rectangle(tabX, 25, tabSize.width(), tabSize.height()));
			clickGuiTabControl.draw(renderer, tickDelta);

			// Draw selected page's windows
			int selectedIdx = clickGuiTabControl.getSelectedIndex();
			if (selectedIdx >= 0 && selectedIdx < pages.size())
				pages.get(selectedIdx).render(renderer, tickDelta);
		}

		// Render HUDS
		if (!clickGuiOpen) {
			for (Window hud : pinnedHuds.values()) {
				hud.draw(renderer, tickDelta);
			}
		}

		// Draw Popup on top of all UI elements
		if (popup != null) {
			popup.draw(renderer, tickDelta);
		}

		// Draw Tooltip on top of all UI elements
		if (tooltip != null && enableTooltips.getValue()) {
			int mouseX = (int) MC.mouseHandler.xpos();
			int mouseY = (int) MC.mouseHandler.ypos();
			Font tooltipFont = fontSetting.getValue().getRenderer();
			int tooltipWidth = Renderer2D.getStringWidth(tooltip, tooltipFont) + 2;
			int tooltipHeight = 10;

			renderer.drawRoundedBox(mouseX + 12, mouseY + 12, (tooltipWidth + 4) * 2, (tooltipHeight + 4) * 2,
					roundingRadius.getValue(), windowBackgroundColor.getValue());
			renderer.drawString(tooltip, mouseX + 18, mouseY + 18, foregroundColor.getValue(), tooltipFont);
		}
		matrixStack.popMatrix();
	}

	/**
	 * Gets whether or not the Click GUI is currently open.
	 *
	 * @return State of the Click GUI.
	 */
	public boolean isClickGuiOpen() {
		return clickGuiOpen;
	}

	public void setClickGuiOpen(boolean state) {
		clickGuiOpen = state;
		setTooltip(null);
		if (!state) {
			requestFocus(null);
			closePopup();
		}
	}

	public Popup getPopup() {
		return popup;
	}

	public void openPopup(UIElement target, UIElement content, Popup.PlacementMode mode) {
		closePopup();
		popup = new Popup();
		popup.open(target, content, mode);
	}

	public void openPopup(UIElement target, UIElement content) {
		openPopup(target, content, Popup.PlacementMode.Bottom);
	}

	public void closePopup() {
		if (popup != null) {
			popup.close();
			popup = null;
		}
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		if (popup != null) {
			popup.onMouseClick(event);
			if (event.isCancelled())
				return;
		}

		if (clickGuiOpen) {
			clickGuiTabControl.onMouseClick(event);
			if (!event.isCancelled()) {
				int selectedIdx = clickGuiTabControl.getSelectedIndex();
				if (selectedIdx >= 0 && selectedIdx < pages.size())
					pages.get(selectedIdx).onMouseClick(event);
			}
		}
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		if (popup != null) {
			popup.onMouseMove(event);
		}

		if (clickGuiOpen) {
			clickGuiTabControl.onMouseMove(event);
			int selectedIdx = clickGuiTabControl.getSelectedIndex();
			if (selectedIdx >= 0 && selectedIdx < pages.size())
				pages.get(selectedIdx).onMouseMove(event);
		}
	}

	@Override
	public void onMouseScroll(MouseScrollEvent event) {
		if (popup != null) {
			popup.onMouseScroll(event);
			if (event.isCancelled())
				return;
		}

		if (clickGuiOpen) {
			int selectedIdx = clickGuiTabControl.getSelectedIndex();
			if (selectedIdx >= 0 && selectedIdx < pages.size())
				pages.get(selectedIdx).onMouseScroll(event);
		}
	}

	/**
	 * Locks and unlocks the Mouse.
	 */
	public void toggleMouse() {
		if (MC.mouseHandler.isMouseGrabbed()) {
			MC.mouseHandler.releaseMouse();
		} else {
			MC.mouseHandler.grabMouse();
		}
	}
}
