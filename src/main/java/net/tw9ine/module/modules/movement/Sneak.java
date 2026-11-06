/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.movement;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.TickEvent.Post;
import net.tw9ine.event.events.TickEvent.Pre;
import net.tw9ine.event.listeners.TickListener;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.minecraft.client.player.LocalPlayer;

public class Sneak extends Module implements TickListener {
	public Sneak() {
		super("Sneak");
		setCategory(Category.of("Movement"));
		setDescription("Makes the player appear like they're sneaking.");
	}

	@Override
	public void onDisable() {
		LocalPlayer player = MC.player;
		if (player != null) {
			MC.options.keyShift.setDown(false);
		}
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		LocalPlayer player = MC.player;
		if (player != null) {
			MC.options.keyShift.setDown(true);
		}
	}

	@Override
	public void onTick(Post event) {

	}
}