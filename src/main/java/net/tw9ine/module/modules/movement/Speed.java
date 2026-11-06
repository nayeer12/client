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
import net.tw9ine.settings.types.FloatSetting;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class Speed extends Module implements TickListener {

	private final FloatSetting speedSetting = FloatSetting.builder().id("speed_setting").displayName("Speed")
			.description("Speed.").defaultValue(0.1f).minValue(0.1f).maxValue(6f).step(0.1f).build();

	public Speed() {
		super("Speed");
		setCategory(Category.of("Movement"));
		setDescription("Modifies the Movement-Speed of the Player");

		speedSetting.addOnUpdate((i) -> {
			if (state.getValue()) {
				AttributeInstance attribute = MC.player.getAttribute(Attributes.MOVEMENT_SPEED);
				attribute.setBaseValue(speedSetting.getValue());
			}
		});

		addSetting(speedSetting);

		setDetectable(
		    AntiCheat.NoCheatPlus,
		    AntiCheat.Vulcan,
		    AntiCheat.AdvancedAntiCheat,
		    AntiCheat.Verus,
		    AntiCheat.Grim,
		    AntiCheat.Matrix,
		    AntiCheat.Negativity,
		    AntiCheat.Karhu
		);
	}

	@Override
	public void onDisable() {
		MC.options.fovEffectScale().set(Math.min(1.0, Math.max(0.0, 1.0)));
		if (MC.player != null) {
			AttributeInstance attribute = MC.player.getAttribute(Attributes.MOVEMENT_SPEED);
			attribute.setBaseValue(0.1);
		}
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
	}

	@Override
	public void onEnable() {
		MC.options.fovEffectScale().set(Math.min(1.0, Math.max(0.0, 0.0)));
		AttributeInstance attribute = MC.player.getAttribute(Attributes.MOVEMENT_SPEED);
		attribute.setBaseValue(speedSetting.getValue());
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(Pre event) {
		AttributeInstance attribute = MC.player.getAttribute(Attributes.MOVEMENT_SPEED);
		attribute.setBaseValue(speedSetting.getValue());
	}

	@Override
	public void onTick(Post event) {

	}
}
