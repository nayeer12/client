/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.module.modules.combat;

import java.util.HashMap;
import java.util.Map;

import net.aoba.Aoba;
import net.aoba.event.events.Render3DEvent;
import net.aoba.event.events.TickEvent;
import net.aoba.event.listeners.Render3DListener;
import net.aoba.event.listeners.TickListener;
import net.aoba.gui.colors.Color;
import net.aoba.managers.rotation.RotationMode;
import net.aoba.managers.rotation.goals.EasingFunction;
import net.aoba.managers.rotation.goals.Vec3dGoal;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.rendering.shaders.Shader;
import net.aoba.settings.types.BooleanSetting;
import net.aoba.settings.types.EnumSetting;
import net.aoba.settings.types.FloatSetting;
import net.aoba.settings.types.RangeSetting;
import net.aoba.settings.types.ShaderSetting;
import net.aoba.utils.entity.DamageUtils;
import net.aoba.utils.entity.TargetPriority;
import net.aoba.utils.player.InteractionUtils;
import net.aoba.utils.player.InventoryUtils;
import net.aoba.utils.types.Range;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoCrystal extends Module implements TickListener, Render3DListener {
	private final FloatSetting targetRange = FloatSetting.builder().id("autocrystal_target_range")
			.displayName("Target Range").description("Maximum distance an enemy can be to be considered a target.")
			.defaultValue(10f).minValue(1f).maxValue(20f).step(0.5f).build();

	private final FloatSetting placeRange = FloatSetting.builder().id("autocrystal_place_range")
			.displayName("Place Range").description("Maximum distance from your eyes to a crystal placement.")
			.defaultValue(4.5f).minValue(1f).maxValue(6f).step(0.1f).build();

	private final FloatSetting breakRange = FloatSetting.builder().id("autocrystal_break_range")
			.displayName("Break Range").description("Maximum distance from your eyes to a crystal you attack.")
			.defaultValue(4.5f).minValue(1f).maxValue(6f).step(0.1f).build();

	private final FloatSetting placeRadius = FloatSetting.builder().id("autocrystal_place_radius")
			.displayName("Search Radius").description("How many blocks around a target to search for placements.")
			.defaultValue(4f).minValue(1f).maxValue(8f).step(1f).build();

	private final RangeSetting attackDelay = RangeSetting.builder().id("autocrystal_break_delay")
			.displayName("Break Delay").description("Random delay in milliseconds between attacks (min, max).")
			.defaultValue(new Range(50f, 100f)).minValue(0f).maxValue(2000f).step(10f).build();

	private final RangeSetting placeDelay = RangeSetting.builder().id("autocrystal_place_delay_ms")
			.displayName("Place Delay").description("Random delay in milliseconds between placing crystals (min, max).")
			.defaultValue(new Range(50f, 100f)).minValue(0f).maxValue(2000f).step(10f).build();

	private final BooleanSetting lethalOverride = BooleanSetting.builder().id("autocrystal_lethal_override")
			.displayName("Lethal Override")
			.description("Place/break even below Min Damage when the hit would kill the target.").defaultValue(true)
			.build();

	private final BooleanSetting pauseWhileUsing = BooleanSetting.builder().id("autocrystal_pause_while_using")
			.displayName("Pause While Using").description("Pause while eating, drinking or blocking.")
			.defaultValue(true).build();

	private final BooleanSetting targetFriends = BooleanSetting.builder().id("autocrystal_target_friends")
			.displayName("Target Friends").description("Target friends.").defaultValue(false).build();

	private final BooleanSetting autoSwitch = BooleanSetting.builder().id("autocrystal_auto_switch")
			.displayName("Auto Switch").description("Automatically switch to End Crystal.").defaultValue(true).build();

	private final BooleanSetting antiSuicide = BooleanSetting.builder().id("autocrystal_anti_suicide")
			.displayName("AntiSuicide").description("Prevents attacking crystals if it would result in player's death.")
			.defaultValue(true).build();

	private final FloatSetting minDamage = FloatSetting.builder().id("autocrystal_min_damage").displayName("Min Damage")
			.description("Minimum damage a crystal must deal to be placed or attacked.").defaultValue(6f).minValue(0f)
			.maxValue(36f).step(0.5f).build();

	private final BooleanSetting ignoreWalls = BooleanSetting.builder().id("autocrystal_ignore_walls")
			.displayName("Ignore Walls").description("Ignore walls when targeting enemies.").defaultValue(true).build();

	private final EnumSetting<TargetPriority> targetPriority = EnumSetting.<TargetPriority>builder()
			.id("autocrystal_target_priority").displayName("Target Priority").description("Priority to target players.")
			.defaultValue(TargetPriority.CLOSEST).build();

	private final EnumSetting<RotationMode> rotationMode = EnumSetting.<RotationMode>builder()
			.id("autocrystal_rotation_mode").displayName("Rotation Mode")
			.description("Controls how the player's view rotates.").defaultValue(RotationMode.NONE).build();

	private final BooleanSetting useRaycast = BooleanSetting.builder().id("autocrystal_use_raycast").displayName("Use Raycast")
			.description(
					"Whether a raycast will be used to ensure that the player is aiming at the crystal before attacking it.")
			.defaultValue(true).build();

	private final BooleanSetting attackOnClick = BooleanSetting.builder().id("autocrystal_attack_on_click")
			.displayName("Attack On Click").description("Only attack crystals while left-click is held.")
			.defaultValue(false).build();

	private final BooleanSetting placeOnClick = BooleanSetting.builder().id("autocrystal_place_on_click")
			.displayName("Place On Click").description("Only place crystals while right-click is held.")
			.defaultValue(false).build();

	private final FloatSetting maxRotation = FloatSetting.builder().id("autocrystal_max_rotation")
			.displayName("Max Rotation").description("The max speed that Aimbot will rotate").defaultValue(10.0f)
			.minValue(1.0f).maxValue(360.0f).build();

	private final EnumSetting<EasingFunction> easingFunction = EnumSetting.<EasingFunction>builder()
			.id("autocrystal_easing").displayName("Easing")
			.description("Easing curve applied to the rotation speed as it approaches the target.")
			.defaultValue(EasingFunction.SineEaseInOut).build();

	private final FloatSetting yawRandomness = FloatSetting.builder().id("autocrystal_yaw_randomness")
			.displayName("Yaw Rotation Jitter").description("The randomness of the player's yaw").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final FloatSetting pitchRandomness = FloatSetting.builder().id("autocrystal_pitch_randomness")
			.displayName("Pitch Rotation Jitter").description("The randomness of the player's pitch").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final BooleanSetting fakeRotation = BooleanSetting.builder().id("autocrystal_fake_rotation")
			.displayName("Fake Rotation")
			.description("Spoofs the client's rotation so that the player appears rotated on the server")
			.defaultValue(false).build();

	private final BooleanSetting moveFix = BooleanSetting.builder().id("autocrystal_move_fix").displayName("Move Fix")
			.description("Corrects movement to match spoofed rotation by using the server yaw for velocity.")
			.defaultValue(false).build();

	private final FloatSetting maxSelfDamage = FloatSetting.builder().id("autocrystal_max_self_damage")
			.displayName("Max Self Damage")
			.description("Maximum self-damage the player can take from a single crystal.").defaultValue(4f).minValue(0f)
			.maxValue(20f).step(0.5f).build();

	private final ShaderSetting color = ShaderSetting.builder().id("autocrystal_color").displayName("Color")
			.description("Color").defaultValue(Shader.solid(new Color(0, 1f, 1f))).build();

	private static final long BOX_DISPLAY_TIME_MS = 1500;

	private long lastAttackTime;
	private long lastPlaceTime;
	private long nextAttackDelayMs;
	private long nextPlaceDelayMs;
	private final Map<BlockPos, Long> displayedBoxes = new HashMap<>();

	/** A candidate crystal placement and the damage it would deal. */
	private record Placement(BlockPos base, double targetDamage, double selfDamage) {
	}

	public AutoCrystal() {
		super("AutoCrystal");

		setCategory(Category.of("Combat"));
		setDescription("Places and breaks End Crystals near enemies.");

		addSetting(targetRange);
		addSetting(placeRange);
		addSetting(breakRange);
		addSetting(placeRadius);
		addSetting(attackDelay);
		addSetting(placeDelay);
		addSetting(minDamage);
		addSetting(maxSelfDamage);
		addSetting(antiSuicide);
		addSetting(lethalOverride);
		addSetting(ignoreWalls);
		addSetting(targetFriends);
		addSetting(targetPriority);
		addSetting(autoSwitch);
		addSetting(pauseWhileUsing);
		addSetting(attackOnClick);
		addSetting(placeOnClick);
		addSetting(useRaycast);
		addSetting(rotationMode);
		addSetting(maxRotation);
		addSetting(easingFunction);
		addSetting(yawRandomness);
		addSetting(pitchRandomness);
		addSetting(fakeRotation);
		addSetting(moveFix);
		addSetting(color);
	}

	@Override
	public void onDisable() {
		Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Aoba.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Aoba.getInstance().rotationManager.setGoal(null);
		displayedBoxes.clear();
	}

	@Override
	public void onEnable() {
		Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
		Aoba.getInstance().eventManager.AddListener(Render3DListener.class, this);
		lastAttackTime = 0;
		lastPlaceTime = 0;
		nextAttackDelayMs = (long) attackDelay.randomValue();
		nextPlaceDelayMs = (long) placeDelay.randomValue();
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {
		LocalPlayer player = MC.player;
		if (player == null || MC.level == null || MC.gameMode == null || !player.isAlive())
			return;
		if (pauseWhileUsing.getValue() && player.isUsingItem()) {
			Aoba.getInstance().rotationManager.setGoal(null);
			return;
		}

		long currentTime = System.currentTimeMillis();

		// Breaking an existing crystal always takes priority over placing a new one.
		EndCrystal attackTarget = pickAttackTarget();
		Placement placeTarget = (attackTarget == null) ? pickPlaceTarget() : null;

		Vec3 rotationGoal = null;
		if (attackTarget != null)
			rotationGoal = attackTarget.getBoundingBox().getCenter();
		else if (placeTarget != null)
			rotationGoal = topCenter(placeTarget.base());

		if (rotationGoal != null) {
			Aoba.getInstance().rotationManager.setGoal(Vec3dGoal.builder().goal(rotationGoal)
					.mode(rotationMode.getValue()).maxRotation(maxRotation.getValue())
					.pitchRandomness(pitchRandomness.getValue()).yawRandomness(yawRandomness.getValue())
					.fakeRotation(fakeRotation.getValue()).moveFix(moveFix.getValue())
					.easingFunction(easingFunction.getValue()).build());
		} else {
			Aoba.getInstance().rotationManager.setGoal(null);
		}

		boolean canAttack = !attackOnClick.getValue() || MC.options.keyAttack.isDown();
		if (canAttack && attackTarget != null && currentTime - lastAttackTime >= nextAttackDelayMs) {
			if (attackCrystal(attackTarget)) {
				lastAttackTime = currentTime;
				nextAttackDelayMs = (long) attackDelay.randomValue();
			}
			return;
		}

		boolean canPlace = !placeOnClick.getValue() || MC.options.keyUse.isDown();
		if (canPlace && placeTarget != null && currentTime - lastPlaceTime >= nextPlaceDelayMs) {
			if (placeCrystal(placeTarget.base())) {
				lastPlaceTime = currentTime;
				nextPlaceDelayMs = (long) placeDelay.randomValue();
			}
		}
	}

	@Override
	public void onTick(TickEvent.Post event) {

	}

	/**
	 * Whether a target is valid for crystal combat.
	 */
	private boolean isValidTarget(Player target) {
		if (target == MC.player || target.isRemoved() || !target.isAlive() || target.isDeadOrDying()
				|| target.isSpectator())
			return false;
		if (!targetFriends.getValue() && Aoba.getInstance().friendsList.contains(target))
			return false;
		return MC.player.distanceToSqr(target) <= targetRange.getValueSqr();
	}

	/**
	 * Whether dealing a certain amount of damage to a target is worth it.
	 */
	private boolean isWorthIt(LivingEntity target, double damage) {
		if (damage >= minDamage.getValue())
			return true;
		return lethalOverride.getValue() && damage >= target.getHealth() + target.getAbsorptionAmount();
	}

	/**
	 * Whether the self damage of an explosion is acceptable.
	 */
	private boolean isSafe(double selfDamage) {
		if (!antiSuicide.getValue())
			return true;
		if (selfDamage > maxSelfDamage.getValue())
			return false;
		// Never take a hit that would kill us, regardless of the max self damage.
		return selfDamage < MC.player.getHealth() + MC.player.getAbsorptionAmount();
	}

	/**
	 * Picks the best placement across all targets.
	 */
	private Placement pickPlaceTarget() {
		if (findCrystalHand() == null && findCrystalHotbarSlot() == -1)
			return null;

		Placement chosen = null;
		double chosenScore = Double.MAX_VALUE;
		for (Player target : Aoba.getInstance().entityManager.getPlayers()) {
			if (!isValidTarget(target))
				continue;

			Placement placement = findPlacement(target);
			if (placement == null)
				continue;

			double score = targetPriority.getValue().score(target, MC.player.distanceToSqr(target),
					placement.targetDamage());
			if (score < chosenScore) {
				chosenScore = score;
				chosen = placement;
			}
		}
		return chosen;
	}

	/**
	 * Finds the placement that deals the most damage to a target while staying
	 * within the self damage limits.
	 */
	private Placement findPlacement(Player target) {
		BlockPos center = target.blockPosition();
		Vec3 eyePos = MC.player.getEyePosition();
		double placeRangeSqr = placeRange.getValueSqr();
		int extent = placeRadius.getValue().intValue();

		Placement best = null;
		for (int x = -extent; x <= extent; x++) {
			for (int y = -extent; y <= extent; y++) {
				for (int z = -extent; z <= extent; z++) {
					BlockPos base = center.offset(x, y, z);
					if (eyePos.distanceToSqr(topCenter(base)) > placeRangeSqr)
						continue;
					if (!canPlaceCrystal(base))
						continue;
					if (!ignoreWalls.getValue() && InteractionUtils.isBehindWall(base))
						continue;

					Vec3 explosion = topCenter(base);
					double damage = DamageUtils.crystalDamage(target, explosion);
					if (!isWorthIt(target, damage))
						continue;

					double selfDamage = DamageUtils.crystalDamage(MC.player, explosion);
					if (!isSafe(selfDamage))
						continue;

					if (best == null || damage > best.targetDamage()
							|| (damage == best.targetDamage() && selfDamage < best.selfDamage()))
						best = new Placement(base, damage, selfDamage);
				}
			}
		}
		return best;
	}

	/**
	 * Mirrors the vanilla rules for placing an end crystal on a block.
	 */
	static boolean canPlaceCrystal(BlockPos base) {
		BlockState baseState = MC.level.getBlockState(base);
		if (!baseState.is(Blocks.OBSIDIAN) && !baseState.is(Blocks.BEDROCK))
			return false;

		BlockPos above = base.above();
		if (!MC.level.isEmptyBlock(above))
			return false;

		AABB crystalBox = new AABB(above.getX(), above.getY(), above.getZ(), above.getX() + 1, above.getY() + 2,
				above.getZ() + 1);
		return MC.level.getEntities((Entity) null, crystalBox).isEmpty();
	}

	/** Point where a crystal placed on a block sits (and explodes). */
	static Vec3 topCenter(BlockPos base) {
		return new Vec3(base.getX() + 0.5, base.getY() + 1.0, base.getZ() + 0.5);
	}

	/**
	 * Places an end crystal on a block.
	 *
	 * @return True if the placement was sent.
	 */
	private boolean placeCrystal(BlockPos base) {
		InteractionHand hand = findCrystalHand();
		if (hand == null) {
			if (!autoSwitch.getValue())
				return false;
			int slot = findCrystalHotbarSlot();
			if (slot == -1)
				return false;
			swap(slot, false);
			hand = InteractionHand.MAIN_HAND;
		}

		BlockHitResult hit;
		if (useRaycast.getValue()) {
			hit = InteractionUtils.raycastBlock(base);
		} else {
			hit = new BlockHitResult(topCenter(base), Direction.UP, base, false);
		}
		if (hit == null)
			return false;

		ItemStack held = MC.player.getItemInHand(hand);
		SwingAnimation swingAnimation = held.getInteractAnimation();
		InteractionResult result = MC.gameMode.useItemOn(MC.player, hand, hit);
		if (result instanceof InteractionResult.Success success
				&& success.swingSource() == InteractionResult.SwingSource.PREDICTED)
			MC.player.swing(hand, swingAnimation, false);

		displayedBoxes.put(base, System.currentTimeMillis());
		return result.consumesAction();
	}

	/** Hand currently holding end crystals, preferring the off-hand. */
	private InteractionHand findCrystalHand() {
		if (MC.player.getOffhandItem().is(Items.END_CRYSTAL))
			return InteractionHand.OFF_HAND;
		if (MC.player.getMainHandItem().is(Items.END_CRYSTAL))
			return InteractionHand.MAIN_HAND;
		return null;
	}

	private int findCrystalHotbarSlot() {
		return InventoryUtils.findHotbarSlot(stack -> stack.is(Items.END_CRYSTAL));
	}

	/**
	 * Picks the best crystal to break.
	 */
	private EndCrystal pickAttackTarget() {
		Vec3 eyePos = MC.player.getEyePosition();
		double breakRangeSqr = breakRange.getValueSqr();

		EndCrystal chosen = null;
		double chosenScore = Double.MAX_VALUE;

		for (Entity entity : Aoba.getInstance().entityManager.getEntities()) {
			if (!(entity instanceof EndCrystal crystal) || crystal.isRemoved())
				continue;
			if (TriggerBot.distanceSqrToBox(eyePos, crystal.getBoundingBox()) > breakRangeSqr)
				continue;
			if (!ignoreWalls.getValue() && InteractionUtils.isBehindWall(crystal))
				continue;

			double selfDamage = DamageUtils.crystalDamage(MC.player, crystal.position());
			if (!isSafe(selfDamage))
				continue;

			for (Player target : Aoba.getInstance().entityManager.getPlayers()) {
				if (!isValidTarget(target))
					continue;

				double damage = DamageUtils.crystalDamage(target, crystal.position());
				if (!isWorthIt(target, damage))
					continue;

				double score = targetPriority.getValue().score(target, MC.player.distanceToSqr(target), damage);
				if (score < chosenScore) {
					chosenScore = score;
					chosen = crystal;
				}
			}
		}
		return chosen;
	}

	/**
	 * Attacks an end crystal.
	 *
	 * @return True if the attack was sent.
	 */
	private boolean attackCrystal(EndCrystal target) {
		if (useRaycast.getValue()) {
			double reach = Math.max(breakRange.getValue(), MC.player.entityInteractionRange());
			var hit = InteractionUtils.raycastEntity(target, reach);
			if (hit == null || hit.getEntity() != target)
				return false;
		}

		InteractionUtils.attack(target);
		return true;
	}

	@Override
	public void onRender(Render3DEvent event) {
		long currentTime = System.currentTimeMillis();
		displayedBoxes.entrySet().removeIf(entry -> currentTime - entry.getValue() >= BOX_DISPLAY_TIME_MS);
		for (Map.Entry<BlockPos, Long> entry : displayedBoxes.entrySet())
			event.getRenderer().drawBox(new AABB(entry.getKey()), color.getValue(), 1.0f);
	}
}
