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
import net.tw9ine.module.AntiCheat;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;

public class Jesus extends Module implements TickListener {

	private final BooleanSetting legit = BooleanSetting.builder().id("jesus_legit").displayName("Legit")
			.description("Whether or not the player will swim as close to the surface as possible.").defaultValue(true)
			.build();

	public Jesus() {
		super("Jesus");
		setCategory(Category.of("Movement"));
		setDescription("Allows the player to walk on water.");
		addSetting(legit);

		setDetectable(
		    AntiCheat.NoCheatPlus,
		    AntiCheat.Vulcan,
		    AntiCheat.AdvancedAntiCheat,
		    AntiCheat.Verus,
		    AntiCheat.Grim,
		    AntiCheat.Matrix,
		    AntiCheat.Karhu,
		    AntiCheat.Buzz
		);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	public boolean getLegit() {
		return legit.getValue();
	}

	@Override
	public void onTick(Pre event) {
		// If Legit is enabled, simply swim.
		if (legit.getValue()) {
			if (MC.player.isInLava() || MC.player.isInWater()) {
				MC.options.keyJump.setDown(true);
			}
		}
	}

	@Override
	public void onTick(Post event) {

	}
}
