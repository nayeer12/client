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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class Spider extends Module implements TickListener {

	private final FloatSetting speed = FloatSetting.builder().id("spider_speed").displayName("Speed")
			.description("Speed that the player climbs up blocks.").defaultValue(0.1f).minValue(0.05f).maxValue(1f)
			.step(0.05f).build();

	public Spider() {
		super("Spider");
		setCategory(Category.of("Movement"));
		setDescription("Allows players to climb up blocks like a spider.");
		addSetting(speed);

		setDetectable(
		    AntiCheat.NoCheatPlus,
		    AntiCheat.Vulcan,
		    AntiCheat.AdvancedAntiCheat,
		    AntiCheat.Grim,
		    AntiCheat.Matrix,
		    AntiCheat.Negativity,
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
		LocalPlayer player = MC.player;

		if (player.horizontalCollision) {
			Vec3 playerVelocity = player.getDeltaMovement();
			MC.player.setDeltaMovement(new Vec3(playerVelocity.x(), speed.getValue(), playerVelocity.z()));
		}
	}

	@Override
	public void onTick(Post event) {

	}
}
