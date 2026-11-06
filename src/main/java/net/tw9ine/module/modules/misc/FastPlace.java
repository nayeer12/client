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
import net.tw9ine.module.AntiCheat;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.FloatSetting;

public class FastPlace extends Module implements TickListener {

	private final FloatSetting speed = FloatSetting.builder().id("fastplace_delay").displayName("Delay")
			.description("Delay at which blocks are placed in ticks..").defaultValue(0f).minValue(0f).maxValue(5f)
			.step(1f).build();

	public FastPlace() {
		super("FastPlace");

		setCategory(Category.of("Misc"));
		setDescription("Places blocks exceptionally fast");

		addSetting(speed);

		setDetectable(
		    AntiCheat.NoCheatPlus,
		    AntiCheat.AdvancedAntiCheat,
		    AntiCheat.Matrix,
		    AntiCheat.Negativity
		);
	}

	@Override
	public void onDisable() {
		IMC.setItemUseCooldown(5);
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
		int currentItemCooldown = IMC.getItemUseCooldown();
		int speedValue = speed.getValue().intValue();
		if (currentItemCooldown == 0 || currentItemCooldown > speedValue)
			IMC.setItemUseCooldown(speedValue);
	}

	@Override
	public void onTick(Post event) {

	}
}
