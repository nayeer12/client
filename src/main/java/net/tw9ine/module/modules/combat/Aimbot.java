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
import net.tw9ine.event.events.Render3DEvent;
import net.tw9ine.event.events.SubtickEvent;
import net.tw9ine.event.listeners.Render3DListener;
import net.tw9ine.event.listeners.SubtickListener;
import net.tw9ine.gui.colors.Color;
import net.tw9ine.managers.rotation.RotationMode;
import net.tw9ine.managers.rotation.goals.EasingFunction;
import net.tw9ine.managers.rotation.goals.EntityGoal;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.rendering.shaders.Shader;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.EntitiesSetting;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.settings.types.FloatSetting;
import net.tw9ine.settings.types.ShaderSetting;
import net.tw9ine.utils.entity.BodyPart;
import net.tw9ine.utils.entity.EntityUtils;
import net.tw9ine.utils.entity.TargetPriority;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class Aimbot extends Module implements SubtickListener, Render3DListener {

	private LivingEntity currentTarget = null;

	private final EnumSetting<TargetPriority> targetPriority = EnumSetting.<TargetPriority>builder()
			.id("aimbot_target_priority").displayName("Target Priority")
			.description("The priority used to pick which target to aim towards.").defaultValue(TargetPriority.CROSSHAIR)
			.build();

	private final EnumSetting<BodyPart> bodyPart = EnumSetting.<BodyPart>builder().id("aimbot_body_part")
			.displayName("Body Part").description("The part of the target's body to aim at.")
			.defaultValue(BodyPart.HEAD).build();

	private final EntitiesSetting targetEntities = EntitiesSetting.builder().id("aimbot_target_entities")
			.displayName("Target Entities")
			.description("Entity types that Aimbot will target.")
			.defaultValue(Set.of(EntityTypes.PLAYER)).build();

	private final BooleanSetting targetFriends = BooleanSetting.builder().id("aimbot_target_friends")
			.displayName("Target Friends").description("Target Friends.").defaultValue(false).build();

	private final BooleanSetting ignoreDead = BooleanSetting.builder().id("aimbot_ignore_dead")
			.displayName("Ignore Dead").description("Skip entities that are dead or dying.").defaultValue(true).build();

	private final BooleanSetting ignoreInvisible = BooleanSetting.builder().id("aimbot_ignore_invisible")
			.displayName("Ignore Invisible").description("Skip entities that are invisible.").defaultValue(true)
			.build();

	private final BooleanSetting ignoreSleeping = BooleanSetting.builder().id("aimbot_ignore_sleeping")
			.displayName("Ignore Sleeping").description("Skip players that are sleeping.").defaultValue(true).build();

	private final BooleanSetting ignoreNPCs = BooleanSetting.builder().id("aimbot_ignore_npcs")
			.displayName("Ignore NPCs").description("Attempts to ignore NPCs based on the entity UUID.")
			.defaultValue(true).build();

	private final BooleanSetting useRaycast = BooleanSetting.builder().id("aimbot_use_raycast").displayName("Use Raycast")
			.description("Skips targets that are not in the line of sight of the player.").defaultValue(true).build();

	private final FloatSetting radius = FloatSetting.builder().id("aimbot_radius").displayName("Radius")
			.description("Radius that the aimbot will lock onto a target.").defaultValue(64.0f).minValue(1.0f)
			.maxValue(256.0f).step(1.0f).build();

	private final FloatSetting fov = FloatSetting.builder().id("aimbot_fov").displayName("FOV")
			.description("Angular cone in front of the player that targets must fall within.").defaultValue(90.0f)
			.minValue(1.0f).maxValue(360.0f).step(1.0f).build();

	private final EnumSetting<RotationMode> rotationMode = EnumSetting.<RotationMode>builder()
			.id("aimbot_rotation_mode").displayName("Rotation Mode")
			.description("Controls how the player's view rotates.").defaultValue(RotationMode.NONE).build();

	private final FloatSetting maxRotation = FloatSetting.builder().id("aimbot_max_rotation")
			.displayName("Max Rotation").description("The max speed that Aimbot will rotate").defaultValue(10.0f)
			.minValue(1.0f).maxValue(360.0f).build();

	private final EnumSetting<EasingFunction> easingFunction = EnumSetting.<EasingFunction>builder()
			.id("aimbot_easing").displayName("Easing")
			.description("Easing curve applied to the rotation speed as it approaches the target.")
			.defaultValue(EasingFunction.SineEaseInOut).build();

	private final FloatSetting yawRandomness = FloatSetting.builder().id("aimbot_yaw_randomness")
			.displayName("Yaw Rotation Jitter").description("The randomness of the player's yaw").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final FloatSetting pitchRandomness = FloatSetting.builder().id("aimbot_pitch_randomness")
			.displayName("Pitch Rotation Jitter").description("The randomness of the player's pitch").defaultValue(0.0f)
			.minValue(0.0f).maxValue(10.0f).step(0.1f).build();

	private final BooleanSetting fakeRotation = BooleanSetting.builder().id("aimbot_fake_rotation")
			.displayName("Fake Rotation")
			.description("Spoofs the client's rotation so that the player appears rotated on the server")
			.defaultValue(false).build();

	private final BooleanSetting moveFix = BooleanSetting.builder().id("aimbot_move_fix").displayName("Move Fix")
			.description("Corrects movement to match spoofed rotation by using the server yaw for velocity.")
			.defaultValue(false).build();

	private final BooleanSetting stickyTarget = BooleanSetting.builder().id("aimbot_sticky_target")
			.displayName("Sticky Target")
			.description("Keep aiming at the current target while it stays valid instead of switching targets.")
			.defaultValue(true).build();

	private final BooleanSetting aimOnClick = BooleanSetting.builder().id("aimbot_aim_on_click")
			.displayName("Aim On Click").description("Only aim while the attack key is held down.")
			.defaultValue(false).build();

	private final BooleanSetting pauseInScreens = BooleanSetting.builder().id("aimbot_pause_in_screens")
			.displayName("Pause In Screens").description("Do not aim while a menu or inventory is open.")
			.defaultValue(true).build();

	private final ShaderSetting targetColor = ShaderSetting.builder().id("aimbot_target_color")
			.displayName("Target Color").description("Color of the box drawn at the target position.")
			.defaultValue(Shader.solid(new Color(255, 0, 0))).build();

	public Aimbot() {
		super("Aimbot");
		setCategory(Category.of("Combat"));
		setDescription("Locks your crosshair towards a desired player or entity.");

		addSetting(targetPriority);
		addSetting(bodyPart);
		addSetting(targetEntities);
		addSetting(targetFriends);
		addSetting(ignoreDead);
		addSetting(ignoreInvisible);
		addSetting(ignoreSleeping);
		addSetting(ignoreNPCs);
		addSetting(useRaycast);
		addSetting(radius);
		addSetting(fov);
		addSetting(rotationMode);
		addSetting(maxRotation);
		addSetting(easingFunction);
		addSetting(yawRandomness);
		addSetting(pitchRandomness);
		addSetting(fakeRotation);
		addSetting(moveFix);
		addSetting(stickyTarget);
		addSetting(aimOnClick);
		addSetting(pauseInScreens);
		addSetting(targetColor);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(SubtickListener.class, this);
		Tw9ine.getInstance().eventManager.RemoveListener(Render3DListener.class, this);
		Tw9ine.getInstance().rotationManager.setGoal(null);
		currentTarget = null;
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(SubtickListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(Render3DListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSubtick(SubtickEvent event) {
		if (MC.player == null || MC.level == null || !canAim()) {
			clearTarget();
			return;
		}

		float partialTick = MC.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Vec3 playerPos = MC.player.getPosition(partialTick);

		// Keep the current target while it is still valid so the aim does not jump
		// between entities.
		LivingEntity entityFound = null;
		if (stickyTarget.getValue() && currentTarget != null && isValidTarget(currentTarget, playerPos, partialTick))
			entityFound = currentTarget;

		if (entityFound == null) {
			double bestScore = Double.MAX_VALUE;
			for (Entity entity : Tw9ine.getInstance().entityManager.getEntities()) {
				if (!(entity instanceof LivingEntity living))
					continue;
				if (!isValidTarget(living, playerPos, partialTick))
					continue;

				double distSqr = playerPos.distanceToSqr(living.getPosition(partialTick));
				double score = targetPriority.getValue().score(living, distSqr, 0);
				if (score < bestScore) {
					bestScore = score;
					entityFound = living;
				}
			}
		}

		if (entityFound != null) {
			EntityGoal rotation = EntityGoal.builder().goal(entityFound).mode(rotationMode.getValue())
					.maxRotation(maxRotation.getValue()).pitchRandomness(pitchRandomness.getValue())
					.yawRandomness(yawRandomness.getValue()).fakeRotation(fakeRotation.getValue())
					.moveFix(moveFix.getValue()).bodyPart(bodyPart.getValue())
					.easingFunction(easingFunction.getValue()).build();
			Tw9ine.getInstance().rotationManager.setGoal(rotation);
			currentTarget = entityFound;
		} else {
			clearTarget();
		}
	}

	private boolean canAim() {
		if (pauseInScreens.getValue()
				&& (MC.gui.screen() != null || Tw9ine.getInstance().guiManager.isClickGuiOpen()))
			return false;
		if (aimOnClick.getValue() && !MC.options.keyAttack.isDown())
			return false;
		return MC.player.isAlive() && !MC.player.isSpectator();
	}

	private void clearTarget() {
		if (currentTarget != null)
			Tw9ine.getInstance().rotationManager.setGoal(null);
		currentTarget = null;
	}

	private boolean isValidTarget(LivingEntity entity, Vec3 playerPos, float partialTick) {
		if (entity == MC.player || entity.isRemoved())
			return false;
		if (!targetEntities.getValue().contains(entity.getType()))
			return false;
		if (entity.getPosition(partialTick).distanceToSqr(playerPos) >= radius.getValueSqr())
			return false;
		return shouldTarget(entity, partialTick);
	}

	@Override
	public void onRender(Render3DEvent event) {
		if (currentTarget == null || !currentTarget.isAlive())
			return;

		float tickDelta = event.getRenderer().getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Vec3 targetPos = EntityUtils.getBodyPartPosition(currentTarget, bodyPart.getValue(), tickDelta);
		AABB targetBox = new AABB(targetPos.x - 0.1, targetPos.y - 0.1, targetPos.z - 0.1, targetPos.x + 0.1,
				targetPos.y + 0.1, targetPos.z + 0.1);
		event.getRenderer().drawBox(targetBox, targetColor.getValue(), 1.0f);
	}

	private boolean shouldTarget(LivingEntity entity, float partialTick) {
		if (!EntityUtils.isInFOV(entity, bodyPart.getValue(), fov.getValue(), partialTick))
			return false;

		if (ignoreDead.getValue() && (!entity.isAlive() || entity.isDeadOrDying()))
			return false;

		if (ignoreInvisible.getValue() && entity.isInvisible())
			return false;

		if (ignoreSleeping.getValue() && entity.isSleeping())
			return false;

		// Attempt to skips NPC and friends.
		if (entity instanceof Player player) {
			if (!targetFriends.getValue() && EntityUtils.isFriend(player))
				return false;
			if (ignoreNPCs.getValue() && EntityUtils.isNPC(player))
				return false;
		}

		// Skip targets that are not visible. A target counts as visible when either
		// its eyes or the aimed body part can be seen.
		if (useRaycast.getValue()) {
			Vec3 eyePos = MC.player.getEyePosition(partialTick);
			if (!canSee(eyePos, entity.getEyePosition(partialTick))
					&& !canSee(eyePos, EntityUtils.getBodyPartPosition(entity, bodyPart.getValue(), partialTick)))
				return false;
		}
		return true;
	}

	private static boolean canSee(Vec3 from, Vec3 to) {
		BlockHitResult hit = MC.level
				.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, MC.player));
		return hit.getType() == HitResult.Type.MISS;
	}
}