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
import net.tw9ine.mixin.interfaces.IEntity;
import net.tw9ine.module.AntiCheat;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.FloatSetting;
import net.minecraft.world.phys.Vec3;

public class NoSlowdown extends Module implements TickListener {

	private final FloatSetting slowdownMultiplier = FloatSetting.builder().id("noslowdown_multiplier")
			.displayName("Multiplier").description("NoSlowdown walk speed multiplier.").defaultValue(0f).minValue(0f)
			.maxValue(1f).step(0.1f).build();

	public NoSlowdown() {
		super("NoSlowdown");
		setCategory(Category.of("Movement"));
		setDescription("Prevents the player from being slowed down by blocks.");

		addSetting(slowdownMultiplier);

		setDetectable(
		    AntiCheat.NoCheatPlus,
		    AntiCheat.Vulcan,
		    AntiCheat.AdvancedAntiCheat,
		    AntiCheat.Grim,
		    AntiCheat.Matrix,
		    AntiCheat.Karhu
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

	@Override
	public void onTick(Pre event) {
		IEntity playerEntity = (IEntity) MC.player;

		if (!playerEntity.getMovementMultiplier().equals(Vec3.ZERO)) {
			float multiplier = slowdownMultiplier.getValue();
			if (multiplier == 0.0f) {
				playerEntity.setMovementMultiplier(Vec3.ZERO);
			} else {
				playerEntity.setMovementMultiplier(Vec3.ZERO.add(1, 1, 1).scale(1 / multiplier));
			}
		}
	}

	@Override
	public void onTick(Post event) {

	}
}
