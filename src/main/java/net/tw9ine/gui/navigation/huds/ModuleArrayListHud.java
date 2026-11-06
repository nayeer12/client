/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.huds;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.gui.GuiManager;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.ResizeMode;
import net.tw9ine.gui.types.TextAlign;
import net.tw9ine.module.Module;
import net.tw9ine.managers.SettingManager;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.rendering.Renderer2D;
import net.minecraft.client.gui.Font;
import net.tw9ine.utils.types.MouseAction;
import net.tw9ine.utils.types.MouseButton;

public class ModuleArrayListHud extends HudWindow {
	private final EnumSetting<TextAlign> textAlign = EnumSetting.<TextAlign>builder().id("ModuleArrayListHudText_TextAlign")
			.displayName("Text Align").description("Text Alignment").defaultValue(TextAlign.Right).build();

	public ModuleArrayListHud(int x, int y) {
		super("ModuleArrayListHud", x, y);
		resizeMode = ResizeMode.None;

		SettingManager.registerSetting(textAlign);

		// Calculate max possible width.
		float newWidth = 0;
		for (Module mod : TW9INE.moduleManager.modules) {
			float nameWidth = Renderer2D.getStringWidth(mod.getName(), GuiManager.fontSetting.getValue().getRenderer());
			if (nameWidth > newWidth)
				newWidth = nameWidth;
		}
		
		setProperty(UIElement.WidthProperty, newWidth);
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);

		if (getProperty(UIElement.IsHoveredProperty) && event.button == MouseButton.RIGHT && event.action == MouseAction.DOWN) {
			TextAlign currentValue = textAlign.getValue();
			TextAlign[] enumConstants = currentValue.getDeclaringClass().getEnumConstants();
			int currentIndex = Arrays.asList(enumConstants).indexOf(currentValue);
			int enumCount = enumConstants.length;
			currentIndex = (currentIndex + 1) % enumCount;

			textAlign.setValue(enumConstants[currentIndex]);
		}
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		boolean isVisible = getProperty(UIElement.IsVisibleProperty);
		if (isVisible) {
			Rectangle pos = position.getValue();

			AtomicInteger iteration = new AtomicInteger(0);
				Font font = GuiManager.fontSetting.getValue().getRenderer();
				Stream<Module> moduleStream = TW9INE.moduleManager.modules.stream().filter(s -> s.state.getValue())
						.sorted(Comparator.comparing((mod) -> mod.getName()));

				switch (textAlign.getValue()) {
				case Left:
					moduleStream.forEachOrdered(mod -> {
						float yPosition = pos.y() + 10 + (iteration.get() * 18);
						renderer.drawString(mod.getName(), pos.x(), yPosition,
								GuiManager.accentColor.getValue(), font);
						iteration.incrementAndGet();
					});
					break;
				case Center:
					moduleStream.forEachOrdered(mod -> {
						float yPosition = pos.y() + 10 + (iteration.get() * 18);
						float centerTextWidth = Renderer2D.getStringWidth(mod.getName(), font) / 2.0f;
						renderer.drawString(mod.getName(),
								pos.x() + (pos.width() / 2.0f) - centerTextWidth, yPosition,
								GuiManager.accentColor.getValue(), font);
						iteration.incrementAndGet();
					});
					break;
				case Right:
					moduleStream.forEachOrdered(mod -> {
						float yPosition = pos.y() + 10 + (iteration.get() * 18);
						float rightTextWidth = Renderer2D.getStringWidth(mod.getName(), font);
						renderer.drawString(mod.getName(), pos.x() + pos.width() - rightTextWidth,
								yPosition, GuiManager.accentColor.getValue(), font);
						iteration.incrementAndGet();
					});
					break;
				}
		}

		super.draw(renderer, partialTicks);
	}
}
