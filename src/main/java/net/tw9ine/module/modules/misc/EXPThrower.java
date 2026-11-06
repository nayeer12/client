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
import net.tw9ine.managers.rotation.Rotation;
import net.tw9ine.managers.rotation.RotationMode;
import net.tw9ine.managers.rotation.goals.EntityGoal;
import net.tw9ine.managers.rotation.goals.RotationGoal;
import net.tw9ine.managers.rotation.goals.Vec3dGoal;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.settings.types.FloatSetting;
import net.tw9ine.settings.types.RangeSetting;
import net.tw9ine.utils.FindItemResult;
import net.tw9ine.utils.types.Range;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;

public class EXPThrower extends Module implements TickListener {
	private RotationGoal currentGoal;

	private final FloatSetting pitchSetting = FloatSetting.builder().id("expthrower_pitch").displayName("Pitch")
			.description("The pitch angle for throwing XP bottles.").defaultValue(90.0f).minValue(0f).maxValue(90f)
			.step(1f).build();

	private final BooleanSetting autoSwapSetting = BooleanSetting.builder().id("expthrower_auto_swap")
			.displayName("Auto Swap").description("Automatically swap to XP bottles if not in hand.").defaultValue(true)
			.build();

	private final RangeSetting throwDelaySetting = RangeSetting.builder().id("expthrower_throw_delay")
			.displayName("Throw Delay").description("Delay between throws in ticks.").defaultValue(new Range(1f, 5f))
			.minValue(1f).maxValue(100f).step(1f).build();

	private final EnumSetting<RotationMode> rotationMode = EnumSetting.<RotationMode>builder()
			.id("expthrower_otation_mode").displayName("Rotation Mode")
			.description("Controls how the player's view rotates.").defaultValue(RotationMode.NONE).build();

	private final FloatSetting maxRotation = FloatSetting.builder().id("expthrower_max_rotation")
			.displayName("Max Rotation").description("The max speed that AutoBreed will rotate").defaultValue(10.0f)
			.minValue(1.0f).maxValue(360.0f).build();

	private final FloatSetting yawRandomness = FloatSetting.builder().id("expthrower_yaw_randomness")
			.displayName("Yaw Rotation Jitter").description("The randomness of the player's yaw").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final FloatSetting pitchRandomness = FloatSetting.builder().id("expthrower_pitch_randomness")
			.displayName("Pitch Rotation Jitter").description("The randomness of the player's pitch").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private int timeSinceThrow;
	private int nextThrow;
	private int previousInventorySlot = -1;
	private float previousPitch = Float.NaN;
	
	public EXPThrower() {
		super("EXPThrower");

		setCategory(Category.of("misc"));
		setDescription("Automatically uses XP bottles.");

		addSetting(pitchSetting);
		addSetting(autoSwapSetting);
		addSetting(throwDelaySetting);
		addSetting(rotationMode);
		addSetting(maxRotation);
		addSetting(yawRandomness);
		addSetting(pitchRandomness);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Tw9ine.getInstance().rotationManager.setGoal(null);
		timeSinceThrow = 0;

		// Swap back
		if (previousInventorySlot >= 0) {
			swap(previousInventorySlot, false);
			previousInventorySlot = -1;
			return;
		}
	}

	private void RotateBack() {
		if(!Float.isNaN(previousPitch)) {
			currentGoal = RotationGoal.builder()
					.goal(new Rotation(MC.player.getRotationVector().y, previousPitch))
					.mode(rotationMode.getValue()).maxRotation(maxRotation.getValue())
					.pitchRandomness(pitchRandomness.getValue()).yawRandomness(yawRandomness.getValue()).build();
			Tw9ine.getInstance().rotationManager.setGoal(currentGoal);
			previousPitch = Float.NaN;
		}
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

	}

	@Override
	public void onTick(Post event) {
		FindItemResult exp = findInHotbar(Items.EXPERIENCE_BOTTLE);

		timeSinceThrow++;

		// Swap back
		if (!exp.found() && previousInventorySlot >= 0) {
			swap(previousInventorySlot, false);
			previousInventorySlot = -1;
			return;
		}

		if (exp.found()) {
			// Swap to item if found.
			int currentSlot = MC.player.getInventory().getSelectedSlot();
			if (autoSwapSetting.getValue() && currentSlot != exp.slot()) {
				previousInventorySlot = currentSlot;
				swap(exp.slot(), false);
				return;
			}
			
			if (exp.getHand() != null) {
				// Record rotation
				if(Float.isNaN(previousPitch)) {
					previousPitch = MC.player.getRotationVector().x;
				}

				currentGoal = RotationGoal.builder()
						.goal(new Rotation(MC.player.getRotationVector().y, pitchSetting.getValue()))
						.mode(rotationMode.getValue()).maxRotation(maxRotation.getValue())
						.pitchRandomness(pitchRandomness.getValue()).yawRandomness(yawRandomness.getValue()).build();
				Tw9ine.getInstance().rotationManager.setGoal(currentGoal);

				if (timeSinceThrow < nextThrow) {
					return;
				}

				InteractionHand hand = exp.getHand();
				SwingAnimation swingAnimation = MC.player.getItemInHand(hand).getInteractAnimation();
				if (MC.gameMode.useItem(MC.player, hand) instanceof InteractionResult.Success success
						&& success.swingSource() == InteractionResult.SwingSource.PREDICTED) {
					MC.player.swing(hand, swingAnimation, false);
					timeSinceThrow = 0;
					nextThrow = (int) throwDelaySetting.randomValue();
				}
			} else {
				RotateBack();
			}
		} else {
			
			RotateBack();
		}
	}
}