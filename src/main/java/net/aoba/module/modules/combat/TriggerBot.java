/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.module.modules.combat;

import java.util.Set;

import net.aoba.Aoba;
import net.aoba.event.events.SubtickEvent;
import net.aoba.event.events.TickEvent;
import net.aoba.event.listeners.SubtickListener;
import net.aoba.event.listeners.TickListener;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.settings.types.BooleanSetting;
import net.aoba.settings.types.EntitiesSetting;
import net.aoba.settings.types.FloatSetting;
import net.aoba.settings.types.RangeSetting;
import net.aoba.utils.entity.EntityUtils;
import net.aoba.utils.player.InteractionUtils;
import net.aoba.utils.types.Range;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class TriggerBot extends Module implements SubtickListener, TickListener {
	/**
	 * Upper bound for the attack timer so it never grows without limit (which
	 * would slowly lose float precision during long sessions).
	 */
	private static final float MAX_TIMER_MS = 60_000f;

	private final FloatSetting radius = FloatSetting.builder().id("triggerbot_radius").displayName("Radius")
			.description("Radius that TriggerBot will trigger.").defaultValue(3.00f).minValue(0.1f).maxValue(10f)
			.step(0.01f).build();

	private final EntitiesSetting targetEntities = EntitiesSetting.builder().id("triggerbot_target_entities")
			.displayName("Target Entities")
			.description("Entity types that TriggerBot will target.")
			.defaultValue(Set.of(EntityTypes.PLAYER)).build();

	private final BooleanSetting targetFriends = BooleanSetting.builder().id("triggerbot_target_friends")
			.displayName("Target Friends").description("Target Friends.").defaultValue(false).build();

	private final BooleanSetting ignoreDead = BooleanSetting.builder().id("triggerbot_ignore_dead")
			.displayName("Ignore Dead").description("Skip entities that are dead or dying.").defaultValue(true).build();

	private final BooleanSetting ignoreInvisible = BooleanSetting.builder().id("triggerbot_ignore_invisible")
			.displayName("Ignore Invisible").description("Skip entities that are invisible.").defaultValue(true)
			.build();

	private final BooleanSetting ignoreSleeping = BooleanSetting.builder().id("triggerbot_ignore_sleeping")
			.displayName("Ignore Sleeping").description("Skip players that are sleeping.").defaultValue(true).build();

	private final BooleanSetting ignoreNPCs = BooleanSetting.builder().id("triggerbot_ignore_npcs")
			.displayName("Ignore NPCs").description("Attempts to ignore NPCs based on the entity UUID.")
			.defaultValue(true).build();

	private final RangeSetting attackDelay = RangeSetting.builder().id("triggerbot_attack_delay")
			.displayName("Attack Delay").description("Random delay in milliseconds between attacks (min, max).")
			.defaultValue(new Range(50f, 150f)).minValue(0f).maxValue(1000f).step(10f).build();

	private final FloatSetting cooldownThreshold = FloatSetting.builder().id("triggerbot_cooldown_threshold")
			.displayName("Cooldown Threshold")
			.description("How charged the attack cooldown must be before attacking (1.0 = fully charged).")
			.defaultValue(1.0f).minValue(0.0f).maxValue(1.0f).step(0.01f).build();

	private final BooleanSetting triggerOnClick = BooleanSetting.builder().id("triggerbot_trigger_on_click")
			.displayName("Trigger On Click").description("Only attack while the attack key is held down.")
			.defaultValue(false).build();

	private final BooleanSetting weaponsOnly = BooleanSetting.builder().id("triggerbot_weapons_only")
			.displayName("Weapons Only")
			.description("Only attack while holding a sword, axe, mace or trident.").defaultValue(false).build();

	private final BooleanSetting onlyCriticals = BooleanSetting.builder().id("triggerbot_only_criticals")
			.displayName("Only Criticals")
			.description("Only attack when the hit would be a critical hit (while falling).").defaultValue(false)
			.build();

	private final BooleanSetting pauseWhileUsing = BooleanSetting.builder().id("triggerbot_pause_while_using")
			.displayName("Pause While Using")
			.description("Do not attack while using an item (eating, drinking, blocking, drawing a bow...).")
			.defaultValue(true).build();

	private final BooleanSetting pauseInScreens = BooleanSetting.builder().id("triggerbot_pause_in_screens")
			.displayName("Pause In Screens").description("Do not attack while a menu or inventory is open.")
			.defaultValue(true).build();

	private float timeSinceAttackMs;
	private float nextAttackDelayMs;
	private Entity pendingAttackTarget;

	public TriggerBot() {
		super("Triggerbot");

		setCategory(Category.of("Combat"));
		setDescription("Attacks anything you are looking at.");

		addSetting(attackDelay);
		addSetting(cooldownThreshold);
		addSetting(triggerOnClick);
		addSetting(radius);
		addSetting(targetEntities);
		addSetting(targetFriends);
		addSetting(ignoreDead);
		addSetting(ignoreInvisible);
		addSetting(ignoreSleeping);
		addSetting(ignoreNPCs);
		addSetting(weaponsOnly);
		addSetting(onlyCriticals);
		addSetting(pauseWhileUsing);
		addSetting(pauseInScreens);
	}

	@Override
	public void onDisable() {
		Aoba.getInstance().eventManager.RemoveListener(SubtickListener.class, this);
		Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);
		pendingAttackTarget = null;
	}

	@Override
	public void onEnable() {
		Aoba.getInstance().eventManager.AddListener(SubtickListener.class, this);
		Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
		pendingAttackTarget = null;
		timeSinceAttackMs = 0f;
		nextAttackDelayMs = attackDelay.randomValue();
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSubtick(SubtickEvent event) {
		timeSinceAttackMs = Math.min(timeSinceAttackMs + event.getDelta(), MAX_TIMER_MS);

		if (pendingAttackTarget != null)
			return;
		if (timeSinceAttackMs < nextAttackDelayMs)
			return;
		if (!canAttackNow())
			return;

		EntityHitResult entityResult = InteractionUtils.raycastEntity(MC.player.getViewVector(1.0F),
				e -> !e.isSpectator() && e.isPickable() && e != MC.player, radius.getValue());
		if (entityResult == null)
			return;

		Entity ent = entityResult.getEntity();
		if (isValidTarget(ent))
			pendingAttackTarget = ent;
	}

	@Override
	public void onTick(TickEvent.Pre event) {
		Entity target = pendingAttackTarget;
		pendingAttackTarget = null;
		if (target == null)
			return;

		if (triggerOnClick.getValue() && !MC.options.keyAttack.isDown())
			return;

		// Conditions may have changed between the subtick that picked the target
		// and this tick, so validate everything again before swinging.
		if (target.isRemoved() || !canAttackNow() || !isValidTarget(target))
			return;

		InteractionUtils.attack(target);
		timeSinceAttackMs = 0f;
		nextAttackDelayMs = attackDelay.randomValue();
	}

	@Override
	public void onTick(TickEvent.Post event) {
	}

	/**
	 * Checks the state of the local player (world loaded, cooldown, held item,
	 * menus, item use, critical requirements).
	 */
	private boolean canAttackNow() {
		LocalPlayer player = MC.player;
		if (player == null || MC.level == null || MC.gameMode == null)
			return false;
		if (player.isSpectator() || !player.isAlive())
			return false;
		if (pauseInScreens.getValue()
				&& (MC.gui.screen() != null || Aoba.getInstance().guiManager.isClickGuiOpen()))
			return false;
		if (pauseWhileUsing.getValue() && player.isUsingItem())
			return false;
		if (player.getAttackStrengthScale(0.5f) < cooldownThreshold.getValue())
			return false;
		if (weaponsOnly.getValue() && !isWeapon(player.getMainHandItem()))
			return false;
		if (onlyCriticals.getValue() && !canCrit(player))
			return false;
		return true;
	}

	/**
	 * Checks whether the entity passes all of the target filters and is in range.
	 */
	private boolean isValidTarget(Entity ent) {
		if (!(ent instanceof LivingEntity living))
			return false;
		if (!targetEntities.getValue().contains(ent.getType()))
			return false;
		if (ent instanceof Player player) {
			if (!targetFriends.getValue() && EntityUtils.isFriend(player))
				return false;
			if (ignoreNPCs.getValue() && EntityUtils.isNPC(player))
				return false;
		}
		if (ignoreDead.getValue() && (!living.isAlive() || living.isDeadOrDying()))
			return false;
		if (ignoreInvisible.getValue() && living.isInvisible())
			return false;
		if (ignoreSleeping.getValue() && living.isSleeping())
			return false;

		return distanceSqrToBox(MC.player.getEyePosition(), ent.getBoundingBox()) <= radius.getValueSqr();
	}

	/**
	 * Squared distance from a point to the closest point of an axis-aligned box.
	 * Returns 0 when the point is inside the box.
	 */
	static double distanceSqrToBox(Vec3 point, AABB box) {
		double dx = Math.max(box.minX, Math.min(point.x, box.maxX)) - point.x;
		double dy = Math.max(box.minY, Math.min(point.y, box.maxY)) - point.y;
		double dz = Math.max(box.minZ, Math.min(point.z, box.maxZ)) - point.z;
		return dx * dx + dy * dy + dz * dz;
	}

	private static boolean isWeapon(ItemStack stack) {
		return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(Items.MACE)
				|| stack.is(Items.TRIDENT);
	}

	/**
	 * Mirrors the vanilla conditions for a critical hit.
	 */
	private static boolean canCrit(LocalPlayer player) {
		return player.fallDistance > 0 && !player.onGround() && !player.onClimbable() && !player.isInWater()
				&& !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && !player.isSprinting();
	}
}
