/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.misc;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.TickEvent.Post;
import net.tw9ine.event.events.TickEvent.Pre;
import net.tw9ine.event.listeners.TickListener;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;

public class AutoWalk extends Module implements TickListener {
	private final BooleanSetting automaticJump = BooleanSetting.builder().id("autowalk_automatic_jump")
			.displayName("Automatically Jump").description("Automatically jumps when you hit a wall.")
			.defaultValue(true).build();

	public AutoWalk() {
		super("AutoWalk");

		setCategory(Category.of("Misc"));
		setDescription("Automatically forward walks for you.");

		addSetting(automaticJump);
	}

	@Override
	public void onDisable() {
		MC.options.keyUp.setDown(false);
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
		MC.options.keyUp.setDown(true);
		if (MC.player.horizontalCollision && MC.player.onGround())
			MC.player.jumpFromGround();
	}

	@Override
	public void onTick(Post event) {

	}
}
