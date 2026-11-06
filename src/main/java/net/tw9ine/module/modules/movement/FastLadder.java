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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class FastLadder extends Module implements TickListener {

	private final FloatSetting ladderSpeed = FloatSetting.builder().id("fastladder_speed").displayName("Speed")
			.description("Speed for FastLadder Module.").defaultValue(0.2f).minValue(0.1f).maxValue(0.5f).step(0.1f)
			.build();

	private final FloatSetting accelerationBoost = FloatSetting.builder().id("acceleration_boost")
			.displayName("Acceleration Boost").description("Extra speed applied when moving upwards on ladders.")
			.defaultValue(0.08f).minValue(0.01f).maxValue(0.2f).step(0.01f).build();

	private final FloatSetting decelerationPenalty = FloatSetting.builder().id("deceleration_penalty")
			.displayName("Deceleration Penalty")
			.description("Speed reduction when moving sideways or not moving on ladders.").defaultValue(0.08f)
			.minValue(0.01f).maxValue(0.2f).step(0.01f).build();

	public FastLadder() {
		super("FastLadder");
		setCategory(Category.of("Movement"));
		setDescription("Allows players to climb up Ladders faster");

		addSetting(ladderSpeed);
		addSetting(accelerationBoost);
		addSetting(decelerationPenalty);

		setDetectable(AntiCheat.NoCheatPlus, AntiCheat.Vulcan, AntiCheat.AdvancedAntiCheat, AntiCheat.Verus,
				AntiCheat.Grim, AntiCheat.Matrix);

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

		if (!player.onClimbable() || !player.horizontalCollision)
			return;

		Vec2 playerInput = player.input.getMoveVector();
		if (playerInput.x == 0 && playerInput.y == 0)
			return;

		Vec3 velocity = player.getDeltaMovement();
		double yVelocity = ladderSpeed.getValue() + accelerationBoost.getValue();

		if (playerInput.x == 0 && playerInput.y != 0) {
			yVelocity -= decelerationPenalty.getValue();
		}

		player.setDeltaMovement(velocity.x, yVelocity, velocity.z);
	}

	@Override
	public void onTick(Post event) {

	}
}
