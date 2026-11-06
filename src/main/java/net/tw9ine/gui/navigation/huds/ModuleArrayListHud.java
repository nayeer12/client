/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.huds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.Font;
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.ResizeMode;
import net.tw9ine.gui.types.TextAlign;
import net.tw9ine.managers.SettingManager;
import net.tw9ine.module.Module;
import net.tw9ine.rendering.Renderer2D;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.utils.types.MouseAction;
import net.tw9ine.utils.types.MouseButton;

/**
 * List of enabled modules, longest name first, with a slide animation.
 */
public class ModuleArrayListHud extends HudWindow {
	private static final float PAD_X = 6f;
	private static final float PAD_Y = 2f;
	private static final float BAR_WIDTH = 2f;
	private static final float EDGE_MARGIN = 6f;
	/** Higher is faster. */
	private static final float ANIMATION_SPEED = 14f;

	private final EnumSetting<TextAlign> textAlign = EnumSetting.<TextAlign>builder()
			.id("ModuleArrayListHud_Side").displayName("Side").description("Which side the list is aligned to.")
			.defaultValue(TextAlign.Right).build();

	private final BooleanSetting snapToEdge = BooleanSetting.builder().id("ModuleArrayListHud_SnapToEdge")
			.displayName("Snap To Edge").description("Keep the list against the edge of the screen.")
			.defaultValue(true).build();

	private final BooleanSetting showBackground = BooleanSetting.builder().id("ModuleArrayListHud_Background")
			.displayName("Background").description("Draw a dark background behind each entry.").defaultValue(true)
			.build();

	private final Map<Module, Float> animation = new HashMap<>();
	private final Map<Module, Float> widthCache = new HashMap<>();
	private final List<Module> visible = new ArrayList<>();
	private Font cachedFont;
	private long lastFrameNanos;

	public ModuleArrayListHud(int x, int y) {
		super("ModuleArrayListHud", x, y);
		resizeMode = ResizeMode.None;
		setProperty(UIElement.MinWidthProperty, 20f);
		setProperty(UIElement.MinHeightProperty, 10f);

		SettingManager.registerSetting(textAlign);
		SettingManager.registerSetting(snapToEdge);
		SettingManager.registerSetting(showBackground);
	}

	@Override
	public void onMouseClick(MouseClickEvent event) {
		super.onMouseClick(event);

		// Right-click the list while the ClickGUI is open to flip sides.
		if (getProperty(UIElement.IsHoveredProperty) && event.button == MouseButton.RIGHT
				&& event.action == MouseAction.DOWN) {
			textAlign.setValue(textAlign.getValue() == TextAlign.Right ? TextAlign.Left : TextAlign.Right);
		}
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		if (getProperty(UIElement.IsVisibleProperty) && MC.player != null)
			drawList(renderer);

		super.draw(renderer, partialTicks);
	}

	private void drawList(Renderer2D renderer) {
		Font font = HudStyle.font();
		if (font != cachedFont) {
			widthCache.clear();
			cachedFont = font;
		}

		float dt = frameDeltaSeconds();
		float step = Math.min(1f, dt * ANIMATION_SPEED);

		visible.clear();
		for (Module module : enabledCandidates()) {
			boolean enabled = module.state.getValue();
			float current = animation.getOrDefault(module, 0f);
			float target = enabled ? 1f : 0f;
			current += (target - current) * step;
			if (Math.abs(target - current) < 0.01f)
				current = target;

			if (current <= 0f) {
				animation.remove(module);
				continue;
			}
			animation.put(module, current);
			visible.add(module);
		}
		visible.sort((a, b) -> Float.compare(nameWidth(b), nameWidth(a)));

		boolean right = textAlign.getValue() == TextAlign.Right;
		float screenWidth = MC.getWindow().getScreenWidth();
		float lineHeight = HudStyle.lineHeight();
		float rowHeight = lineHeight + PAD_Y * 2;

		Rectangle pos = position.getValue();
		if (pos.x() == 0 && pos.y() == 0) {
			position.setY(EDGE_MARGIN);
			pos = position.getValue();
		}

		float maxWidth = 0;
		for (Module module : visible)
			maxWidth = Math.max(maxWidth, nameWidth(module) + PAD_X * 2 + BAR_WIDTH);

		// Anchor x: either the screen edge or the window position.
		float anchorX;
		if (snapToEdge.getValue())
			anchorX = right ? screenWidth - EDGE_MARGIN : EDGE_MARGIN;
		else
			anchorX = right ? pos.x() + pos.width() : pos.x();

		float y = pos.y();
		for (Module module : visible) {
			float anim = animation.get(module);
			float rowWidth = nameWidth(module) + PAD_X * 2 + BAR_WIDTH;
			// Slide in from the anchored edge.
			float slide = (1f - anim) * (rowWidth + EDGE_MARGIN);
			float rowX = right ? anchorX - rowWidth + slide : anchorX - slide;
			float rowH = rowHeight * anim;

			if (showBackground.getValue())
				renderer.drawBox(rowX, y, rowWidth, rowH, HudStyle.background());

			float barX = right ? rowX + rowWidth - BAR_WIDTH : rowX;
			renderer.drawBox(barX, y, BAR_WIDTH, rowH, HudStyle.accent());

			if (anim > 0.6f) {
				float textX = right ? rowX + PAD_X : rowX + BAR_WIDTH + PAD_X;
				HudStyle.draw(renderer, module.getName(), textX, y + PAD_Y, HudStyle.text());
			}
			y += rowH;
		}

		// Keep the drag box matching what is drawn.
		float boxWidth = Math.max(maxWidth, 40f);
		float boxHeight = Math.max(y - pos.y(), rowHeight);
		float boxX = right ? anchorX - boxWidth : anchorX;
		if (snapToEdge.getValue() && Math.abs(pos.x() - boxX) > 0.5f)
			position.setX(boxX);
		if (Math.abs(position.getWidth() - boxWidth) > 0.5f)
			position.setWidth(boxWidth);
		if (Math.abs(position.getHeight() - boxHeight) > 0.5f)
			position.setHeight(boxHeight);
	}

	private Iterable<Module> enabledCandidates() {
		return TW9INE.moduleManager.modules;
	}

	private float nameWidth(Module module) {
		return widthCache.computeIfAbsent(module, m -> HudStyle.width(m.getName()));
	}

	private float frameDeltaSeconds() {
		long now = System.nanoTime();
		float dt = lastFrameNanos == 0 ? 0f : (now - lastFrameNanos) / 1_000_000_000f;
		lastFrameNanos = now;
		return Math.min(dt, 0.1f);
	}
}
