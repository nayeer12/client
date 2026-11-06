/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.gui.navigation.huds;

import java.util.Locale;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.tw9ine.gui.UIElement;
import net.tw9ine.gui.navigation.HudWindow;
import net.tw9ine.gui.types.Rectangle;
import net.tw9ine.gui.types.ResizeMode;
import net.tw9ine.rendering.Renderer2D;

/**
 * Shows the entity you are fighting: name, health bar, health, distance and
 * the health difference with you.
 */
public class TargetHud extends HudWindow {
	private static final float WIDTH = 230f;
	private static final float PAD = 8f;
	private static final float BAR_HEIGHT = 6f;
	/** How long a target stays on screen after you stop looking at it. */
	private static final long KEEP_MS = 3000;
	private static final double MAX_DISTANCE = 12.0;

	private LivingEntity target;
	private long lastSeen;
	private float displayedHealth = -1f;
	private long lastFrameNanos;

	public TargetHud(int x, int y) {
		super("TargetHud", x, y, WIDTH, 50f);
		resizeMode = ResizeMode.None;
		setProperty(UIElement.MinWidthProperty, 20f);
		setProperty(UIElement.MinHeightProperty, 10f);
	}

	@Override
	public void draw(Renderer2D renderer, float partialTicks) {
		if (getProperty(UIElement.IsVisibleProperty) && MC.player != null)
			drawTarget(renderer);

		super.draw(renderer, partialTicks);
	}

	private void updateTarget() {
		Entity looked = MC.crosshairPickEntity;
		long now = System.currentTimeMillis();
		if (looked instanceof LivingEntity living && living != MC.player && living.isAlive()) {
			if (living != target)
				displayedHealth = -1f;
			target = living;
			lastSeen = now;
		}

		if (target != null && (target.isRemoved() || !target.isAlive() || now - lastSeen > KEEP_MS
				|| MC.player.distanceTo(target) > MAX_DISTANCE))
			target = null;
	}

	private void drawTarget(Renderer2D renderer) {
		updateTarget();

		float lineHeight = HudStyle.lineHeight();
		float height = PAD * 3 + lineHeight * 2 + BAR_HEIGHT;

		Rectangle pos = position.getValue();
		if (pos.x() == 0 && pos.y() == 0) {
			position.setX(MC.getWindow().getScreenWidth() / 2f + 30f);
			position.setY(MC.getWindow().getScreenHeight() / 2f + 30f);
			pos = position.getValue();
		}
		if (Math.abs(position.getWidth() - WIDTH) > 0.5f)
			position.setWidth(WIDTH);
		if (Math.abs(position.getHeight() - height) > 0.5f)
			position.setHeight(height);

		LivingEntity shown = target;
		boolean preview = shown == null && TW9INE.guiManager.isClickGuiOpen();
		if (shown == null && !preview)
			return;

		String name = preview ? "Target" : shown.getName().getString();
		float health = preview ? 14f : shown.getHealth() + shown.getAbsorptionAmount();
		float maxHealth = preview ? 20f : Math.max(1f, shown.getMaxHealth());

		// Smoothly animate the bar towards the real value.
		float dt = frameDeltaSeconds();
		if (displayedHealth < 0f)
			displayedHealth = health;
		displayedHealth += (health - displayedHealth) * Math.min(1f, dt * 10f);

		float x = pos.x();
		float y = pos.y();
		renderer.drawRoundedBox(x, y, WIDTH, height, 6f, HudStyle.background());

		// Name and distance.
		float textY = y + PAD;
		HudStyle.draw(renderer, trim(name, WIDTH - PAD * 2 - HudStyle.width(" 00.0m")), x + PAD, textY,
				HudStyle.text());
		String distance = preview ? "3.0m" : String.format(Locale.ROOT, "%.1fm", MC.player.distanceTo(shown));
		HudStyle.draw(renderer, distance, x + WIDTH - PAD - HudStyle.width(distance), textY, HudStyle.mutedText());

		// Health bar.
		float barY = textY + lineHeight + PAD / 2f;
		float barWidth = WIDTH - PAD * 2;
		float fraction = Math.max(0f, Math.min(1f, displayedHealth / maxHealth));
		renderer.drawRoundedBox(x + PAD, barY, barWidth, BAR_HEIGHT, 3f, HudStyle.barTrack());
		if (fraction > 0f)
			renderer.drawRoundedBox(x + PAD, barY, Math.max(BAR_HEIGHT, barWidth * fraction), BAR_HEIGHT, 3f,
					fraction <= 0.3f ? HudStyle.danger() : HudStyle.accent());

		// Health and difference with our own health.
		float infoY = barY + BAR_HEIGHT + PAD / 2f;
		String hp = String.format(Locale.ROOT, "%.1f HP", health);
		HudStyle.draw(renderer, hp, x + PAD, infoY, HudStyle.text());

		float own = MC.player.getHealth() + MC.player.getAbsorptionAmount();
		float diff = own - health;
		String diffText = String.format(Locale.ROOT, "%+.1f", diff);
		HudStyle.draw(renderer, diffText, x + WIDTH - PAD - HudStyle.width(diffText), infoY,
				diff >= 0 ? HudStyle.accent() : HudStyle.danger());

		if (!preview && shown instanceof Player player && player.isBlocking())
			HudStyle.draw(renderer, "  blocking", x + PAD + HudStyle.width(hp), infoY, HudStyle.mutedText());
	}

	private static String trim(String text, float maxWidth) {
		if (HudStyle.width(text) <= maxWidth)
			return text;
		String ellipsis = "...";
		int end = text.length();
		while (end > 0 && HudStyle.width(text.substring(0, end) + ellipsis) > maxWidth)
			end--;
		return text.substring(0, end) + ellipsis;
	}

	private float frameDeltaSeconds() {
		long now = System.nanoTime();
		float dt = lastFrameNanos == 0 ? 0f : (now - lastFrameNanos) / 1_000_000_000f;
		lastFrameNanos = now;
		return Math.min(dt, 0.1f);
	}
}
