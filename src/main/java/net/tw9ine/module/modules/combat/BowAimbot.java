/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.combat;

import java.util.Set;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.SubtickEvent;
import net.tw9ine.event.listeners.SubtickListener;
import net.tw9ine.managers.rotation.Rotation;
import net.tw9ine.managers.rotation.RotationMode;
import net.tw9ine.managers.rotation.goals.RotationGoal;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.EntitiesSetting;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.settings.types.FloatSetting;
import net.tw9ine.utils.entity.BodyPart;
import net.tw9ine.utils.entity.EntityUtils;
import net.tw9ine.utils.entity.TargetPriority;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BowAimbot extends Module implements SubtickListener {
	private final EntitiesSetting targetEntities = EntitiesSetting.builder().id("bowaimbot_target_entities")
			.displayName("Target Entities")
			.description("Entity types that BowAimbot will target.")
			.defaultValue(Set.of(EntityTypes.PLAYER)).build();

	private final BooleanSetting targetFriends = BooleanSetting.builder().id("bowaimbot_target_friends")
			.displayName("Target Friends").description("Target Friends.").defaultValue(false).build();

	private final EnumSetting<TargetPriority> targetPriority = EnumSetting.<TargetPriority>builder()
			.id("bowaimbot_target_priority").displayName("Target Priority")
			.description("The priority used to pick which target to aim towards.")
			.defaultValue(TargetPriority.CROSSHAIR).build();

	private final EnumSetting<BodyPart> bodyPart = EnumSetting.<BodyPart>builder().id("bowaimbot_body_part")
			.displayName("Body Part").description("The part of the target's body to aim at.")
			.defaultValue(BodyPart.CHEST).build();

	private final BooleanSetting ignoreDead = BooleanSetting.builder().id("bowaimbot_ignore_dead")
			.displayName("Ignore Dead").description("Skip entities that are dead or dying.").defaultValue(true)
			.build();

	private final BooleanSetting ignoreInvisible = BooleanSetting.builder().id("bowaimbot_ignore_invisible")
			.displayName("Ignore Invisible").description("Skip entities that are invisible.").defaultValue(true)
			.build();

	private final BooleanSetting ignoreSleeping = BooleanSetting.builder().id("bowaimbot_ignore_sleeping")
			.displayName("Ignore Sleeping").description("Skip players that are sleeping.").defaultValue(true)
			.build();

	private final BooleanSetting ignoreNPCs = BooleanSetting.builder().id("bowaimbot_ignore_npcs")
			.displayName("Ignore NPCs")
			.description("Attempts to ignore NPCs based on the entity UUID.").defaultValue(true).build();

	private final FloatSetting frequency = FloatSetting.builder().id("bowaimbot_frequency").displayName("Ticks")
			.description("How frequent the aimbot updates (Lower = Laggier)").defaultValue(1.0f).minValue(1.0f)
			.maxValue(20.0f).step(1.0f).build();

	private final FloatSetting predictMovement = FloatSetting.builder().id("bowaimbot_prediction_strength")
			.displayName("Prediction")
			.description("How far ahead of a moving target to aim. 1.0 leads by the arrow's flight time.")
			.defaultValue(1f).minValue(0f).maxValue(2f).step(0.05f).build();

	private final FloatSetting range = FloatSetting.builder().id("bowaimbot_range").displayName("Range")
			.description("Maximum distance to a target.").defaultValue(80f).minValue(5f).maxValue(200f).step(1f)
			.build();

	private final FloatSetting fov = FloatSetting.builder().id("bowaimbot_fov").displayName("FOV")
			.description("Angular cone in front of the player that targets must fall within.").defaultValue(120f)
			.minValue(1f).maxValue(360f).step(1f).build();

	private final BooleanSetting useRaycast = BooleanSetting.builder().id("bowaimbot_use_raycast")
			.displayName("Use Raycast").description("Skips targets that are not in the line of sight of the player.")
			.defaultValue(true).build();

	private final BooleanSetting stickyTarget = BooleanSetting.builder().id("bowaimbot_sticky_target")
			.displayName("Sticky Target")
			.description("Keep aiming at the current target while it stays valid instead of switching targets.")
			.defaultValue(true).build();

	private final BooleanSetting showTrajectory = BooleanSetting.builder().id("bowaimbot_show_trajectory")
			.displayName("Show Trajectory").description("Turns on the Trajectory module while BowAimbot is enabled.")
			.defaultValue(true).build();

	private final EnumSetting<RotationMode> rotationMode = EnumSetting.<RotationMode>builder()
			.id("bowaimbot_rotation_mode").displayName("Rotation Mode")
			.description("Controls how the player's view rotates.").defaultValue(RotationMode.INSTANT).build();

	private final FloatSetting maxRotation = FloatSetting.builder().id("bowaimbot_max_rotation")
			.displayName("Max Rotation").description("The max speed that BowAimbot will rotate").defaultValue(180.0f)
			.minValue(1.0f).maxValue(360.0f).build();

	private final FloatSetting yawRandomness = FloatSetting.builder().id("bowaimbot_yaw_randomness")
			.displayName("Yaw Rotation Jitter").description("The randomness of the player's yaw").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final FloatSetting pitchRandomness = FloatSetting.builder().id("bowaimbot_pitch_randomness")
			.displayName("Pitch Rotation Jitter").description("The randomness of the player's pitch").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final BooleanSetting fakeRotation = BooleanSetting.builder().id("bowaimbot_fake_rotation")
			.displayName("Fake Rotation")
			.description("Spoofs the client's rotation so that the player appears rotated on the server")
			.defaultValue(false).build();

	private final BooleanSetting moveFix = BooleanSetting.builder().id("bowaimbot_move_fix").displayName("Move Fix")
			.description("Corrects movement to match spoofed rotation by using the server yaw for velocity.")
			.defaultValue(false).build();

	/** Arrow gravity per tick, normalised so that a full-power shot has speed 1. */
	private static final double NORMALISED_GRAVITY = 0.006;
	/** Speed of a fully drawn bow arrow in blocks per tick. */
	private static final double FULL_POWER_SPEED = 3.0;
	/** Speed of a crossbow arrow in blocks per tick. */
	private static final double CROSSBOW_SPEED = 3.15;

	private int currentTick = 0;
	private LivingEntity currentTarget;
	private boolean enabledTrajectory;

	public BowAimbot() {
		super("BowAimbot");

		setCategory(Category.of("Combat"));
		setDescription("Calculates the location the crosshair must be to hit an arrow shot.");

		addSetting(targetPriority);
		addSetting(bodyPart);
		addSetting(targetEntities);
		addSetting(targetFriends);
		addSetting(ignoreDead);
		addSetting(ignoreInvisible);
		addSetting(ignoreSleeping);
		addSetting(ignoreNPCs);
		addSetting(frequency);
		addSetting(predictMovement);
		addSetting(range);
		addSetting(fov);
		addSetting(useRaycast);
		addSetting(stickyTarget);
		addSetting(showTrajectory);
		addSetting(rotationMode);
		addSetting(maxRotation);
		addSetting(yawRandomness);
		addSetting(pitchRandomness);
		addSetting(fakeRotation);
		addSetting(moveFix);
	}

	@Override
	public void onDisable() {
		// Only turn Trajectory off if BowAimbot was the one that turned it on.
		if (enabledTrajectory && Tw9ine.getInstance().moduleManager.trajectory.state.getValue())
			Tw9ine.getInstance().moduleManager.trajectory.toggle();
		enabledTrajectory = false;
		Tw9ine.getInstance().eventManager.RemoveListener(SubtickListener.class, this);
		Tw9ine.getInstance().rotationManager.setGoal(null);
		currentTarget = null;
	}

	@Override
	public void onEnable() {
		enabledTrajectory = false;
		if (showTrajectory.getValue() && !Tw9ine.getInstance().moduleManager.trajectory.state.getValue()) {
			Tw9ine.getInstance().moduleManager.trajectory.toggle();
			enabledTrajectory = true;
		}
		Tw9ine.getInstance().eventManager.AddListener(SubtickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSubtick(SubtickEvent event) {
		if (MC.player == null || MC.level == null)
			return;

		currentTick++;

		ItemStack stack = MC.player.getInventory().getSelectedItem();
		Item item = stack.getItem();

		// Work out the normalised launch speed of the arrow we are about to fire.
		double velocity;
		if (item instanceof BowItem) {
			if (!MC.player.isUsingItem()) {
				clearTarget();
				return;
			}
			velocity = BowItem.getPowerForTime(MC.player.getTicksUsingItem());
		} else if (item instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
			velocity = CROSSBOW_SPEED / FULL_POWER_SPEED;
		} else {
			clearTarget();
			return;
		}

		// A barely drawn bow cannot reach anything meaningful.
		if (velocity < 0.1) {
			clearTarget();
			return;
		}

		if (currentTick < frequency.getValue())
			return;
		currentTick = 0;

		float partialTick = MC.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Vec3 eyePos = MC.player.getEyePosition(partialTick);

		LivingEntity target = null;
		if (stickyTarget.getValue() && currentTarget != null && isValidTarget(currentTarget, eyePos, partialTick))
			target = currentTarget;

		if (target == null) {
			double bestScore = Double.MAX_VALUE;
			for (Entity entity : Tw9ine.getInstance().entityManager.getEntities()) {
				if (!(entity instanceof LivingEntity living) || !isValidTarget(living, eyePos, partialTick))
					continue;
				double score = targetPriority.getValue().score(living, eyePos.distanceToSqr(living.position()), 0);
				if (score < bestScore) {
					bestScore = score;
					target = living;
				}
			}
		}

		if (target == null) {
			clearTarget();
			return;
		}
		currentTarget = target;

		// Lead the target by its current velocity multiplied by the arrow's flight time.
		Vec3 aimPos = EntityUtils.getBodyPartPosition(target, bodyPart.getValue(), partialTick);
		Vec3 targetVelocity = new Vec3(target.getX() - target.xOld, target.getY() - target.yOld,
				target.getZ() - target.zOld);
		double flatDistance = Math.hypot(aimPos.x - eyePos.x, aimPos.z - eyePos.z);
		double flightTicks = flatDistance / (velocity * FULL_POWER_SPEED);
		aimPos = aimPos.add(targetVelocity.scale(flightTicks * predictMovement.getValue()));

		double dx = aimPos.x - eyePos.x;
		double dy = aimPos.y - eyePos.y;
		double dz = aimPos.z - eyePos.z;
		double horizontal = Math.hypot(dx, dz);

		float neededPitch = solvePitch(horizontal, dy, velocity);
		if (Float.isNaN(neededPitch)) {
			// Out of range: aim at the angle with the longest reach.
			neededPitch = -45f;
		}
		float neededYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;

		RotationGoal goal = RotationGoal.builder().goal(new Rotation(neededYaw, neededPitch))
				.mode(rotationMode.getValue()).maxRotation(maxRotation.getValue())
				.yawRandomness(yawRandomness.getValue()).pitchRandomness(pitchRandomness.getValue())
				.fakeRotation(fakeRotation.getValue()).moveFix(moveFix.getValue()).build();
		Tw9ine.getInstance().rotationManager.setGoal(goal);
	}

	/**
	 * Solves for the low-arc launch pitch needed to hit a point.
	 *
	 * @param horizontal Horizontal distance to the point.
	 * @param dy         Height of the point relative to the shooter's eyes.
	 * @param velocity   Normalised launch speed (1.0 = fully drawn bow).
	 * @return Minecraft pitch in degrees (negative is up), or NaN if the point is
	 *         out of range.
	 */
	static float solvePitch(double horizontal, double dy, double velocity) {
		double g = NORMALISED_GRAVITY;
		double v2 = velocity * velocity;
		if (horizontal < 1.0E-6)
			return dy >= 0 ? -90f : 90f;
		double discriminant = v2 * v2 - g * (g * horizontal * horizontal + 2 * dy * v2);
		if (discriminant < 0)
			return Float.NaN;
		double angle = Math.atan((v2 - Math.sqrt(discriminant)) / (g * horizontal));
		return (float) -Math.toDegrees(angle);
	}

	private void clearTarget() {
		if (currentTarget != null)
			Tw9ine.getInstance().rotationManager.setGoal(null);
		currentTarget = null;
	}

	private boolean isValidTarget(LivingEntity entity, Vec3 eyePos, float partialTick) {
		if (entity == MC.player || entity.isRemoved())
			return false;
		if (!targetEntities.getValue().contains(entity.getType()))
			return false;
		if (entity.getEyePosition(partialTick).distanceToSqr(eyePos) > range.getValueSqr())
			return false;
		if (!EntityUtils.isInFOV(entity, bodyPart.getValue(), fov.getValue(), partialTick))
			return false;
		if (useRaycast.getValue() && !canSee(eyePos, entity.getEyePosition(partialTick))
				&& !canSee(eyePos, entity.getBoundingBox().getCenter()))
			return false;
		return shouldTarget(entity);
	}

	private static boolean canSee(Vec3 from, Vec3 to) {
		BlockHitResult hit = MC.level
				.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, MC.player));
		return hit.getType() == HitResult.Type.MISS;
	}

	private boolean shouldTarget(LivingEntity entity) {
		if (ignoreDead.getValue() && (!entity.isAlive() || entity.isDeadOrDying()))
			return false;

		if (ignoreInvisible.getValue() && entity.isInvisible())
			return false;

		if (ignoreSleeping.getValue() && entity.isSleeping())
			return false;

		if (entity instanceof Player player) {
			if (!targetFriends.getValue() && EntityUtils.isFriend(player))
				return false;
			if (ignoreNPCs.getValue() && EntityUtils.isNPC(player))
				return false;
		}
		return true;
	}
}
