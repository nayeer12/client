/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.module.modules.combat;

import net.aoba.Aoba;
import net.aoba.event.events.SubtickEvent;
import net.aoba.event.events.TickEvent;
import net.aoba.event.listeners.SubtickListener;
import net.aoba.event.listeners.TickListener;
import net.aoba.module.AntiCheat;
import net.aoba.module.Category;
import net.aoba.module.Module;
import net.aoba.settings.types.BooleanSetting;
import net.aoba.settings.types.FloatSetting;
import net.aoba.utils.player.InteractionUtils;
import net.aoba.utils.player.InventoryUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class MaceAura extends Module implements SubtickListener, TickListener {
	private final FloatSetting radius = FloatSetting.builder().id("maceaura_radius").displayName("Radius")
			.description("Distance to the target's hitbox at which MaceAura will trigger.").defaultValue(3f).minValue(0.5f).maxValue(6f).step(0.1f)
			.build();

	private final FloatSetting height = FloatSetting.builder().id("maceaura_height").displayName("Height")
			.description("Determines how high MaceAura will jump. Higher distance = more damage.").defaultValue(100f)
			.minValue(1f).maxValue(255f).build();

	private final BooleanSetting targetAnimals = BooleanSetting.builder().id("maceaura_target_animals")
			.displayName("Target Animals").description("Target animals.").defaultValue(false).build();

	private final BooleanSetting targetMonsters = BooleanSetting.builder().id("maceaura_target_monsters")
			.displayName("Target Monsters").description("Target Monsters.").defaultValue(true).build();

	private final BooleanSetting targetPlayers = BooleanSetting.builder().id("maceaura_target_players")
			.displayName("Target Players").description("Target Players.").defaultValue(true).build();

	private final BooleanSetting targetFriends = BooleanSetting.builder().id("maceaura_target_friends")
			.displayName("Target Friends").description("Target Friends.").defaultValue(false).build();

	private final BooleanSetting triggerOnClick = BooleanSetting.builder().id("maceaura_trigger_on_click")
			.displayName("Trigger On Click").description("Only attack while the attack key is held down.")
			.defaultValue(false).build();

	private final BooleanSetting ignoreInvisible = BooleanSetting.builder().id("maceaura_ignore_invisible")
			.displayName("Ignore Invisible").description("Skip entities that are invisible.").defaultValue(false)
			.build();

	private final BooleanSetting autoSwitch = BooleanSetting.builder().id("maceaura_auto_switch")
			.displayName("Auto Switch").description("Switch to a mace in your hotbar before smashing.")
			.defaultValue(true).build();

	private final BooleanSetting swapBack = BooleanSetting.builder().id("maceaura_swap_back")
			.displayName("Swap Back").description("Switch back to your previous item after the smash.")
			.defaultValue(true).build();

	private LivingEntity selectedTarget;
	private boolean jumped;

	public MaceAura() {
		super("MaceAura");

		setCategory(Category.of("Combat"));
		setDescription(
				"Smashes players in your personal space with a Mace with extreme damage. Be sure to enable NoFall for best results.");

		addSetting(radius);
		addSetting(height);
		addSetting(targetAnimals);
		addSetting(targetMonsters);
		addSetting(targetPlayers);
		addSetting(targetFriends);
		addSetting(triggerOnClick);
		addSetting(ignoreInvisible);
		addSetting(autoSwitch);
		addSetting(swapBack);

		setDetectable(
				AntiCheat.NoCheatPlus,
				AntiCheat.Vulcan,
				AntiCheat.AdvancedAntiCheat,
				AntiCheat.Verus,
				AntiCheat.Grim,
				AntiCheat.Matrix,
				AntiCheat.Negativity,
				AntiCheat.Karhu,
				AntiCheat.Buzz
		);
	}

	@Override
	public void onDisable() {
		Aoba.getInstance().eventManager.RemoveListener(SubtickListener.class, this);
		Aoba.getInstance().eventManager.RemoveListener(TickListener.class, this);

		// Never leave the server thinking we are still in the air.
		if (jumped && MC.player != null)
			sendReturn();
		selectedTarget = null;
		jumped = false;
	}

	@Override
	public void onEnable() {
		Aoba.getInstance().eventManager.AddListener(SubtickListener.class, this);
		Aoba.getInstance().eventManager.AddListener(TickListener.class, this);
		selectedTarget = null;
		jumped = false;
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSubtick(SubtickEvent event) {
		if (MC.player == null || MC.level == null)
			return;

		Vec3 eyePos = MC.player.getEyePosition();
		double radiusSqr = radius.getValueSqr();

		LivingEntity best = null;
		double bestDistSqr = Double.MAX_VALUE;

		for (Entity entity : Aoba.getInstance().entityManager.getEntities()) {
			if (!(entity instanceof LivingEntity living) || !isValidTarget(living))
				continue;

			double distSqr = TriggerBot.distanceSqrToBox(eyePos, living.getBoundingBox());
			if (distSqr > radiusSqr)
				continue;

			if (distSqr < bestDistSqr) {
				best = living;
				bestDistSqr = distSqr;
			}
		}

		selectedTarget = best;
	}

	private boolean isValidTarget(LivingEntity entity) {
		if (entity == MC.player || entity.isRemoved() || !entity.isAlive() || entity.isDeadOrDying())
			return false;
		if (ignoreInvisible.getValue() && entity.isInvisible())
			return false;

		if (entity instanceof Player player) {
			if (!targetPlayers.getValue())
				return false;
			if (!targetFriends.getValue() && Aoba.getInstance().friendsList.contains(player))
				return false;
			return true;
		}
		if (entity instanceof Animal)
			return targetAnimals.getValue();
		if (entity instanceof Enemy)
			return targetMonsters.getValue();
		return false;
	}

	@Override
	public void onTick(TickEvent.Pre event) {

	}

	@Override
	public void onTick(TickEvent.Post event) {
		if (MC.player == null)
			return;

		// Second half: come back down and smash. Always finish once started.
		if (jumped) {
			sendReturn();
			jumped = false;

			LivingEntity target = selectedTarget;
			if (target != null && isValidTarget(target) && MC.player.getMainHandItem().is(Items.MACE))
				InteractionUtils.attack(target);

			if (swapBack.getValue())
				swapBack();
			return;
		}

		if (MC.player.getAttackStrengthScale(0) < 1)
			return;
		if (triggerOnClick.getValue() && !MC.options.keyAttack.isDown())
			return;

		LivingEntity target = selectedTarget;
		if (target == null || !isValidTarget(target))
			return;

		// Make sure a mace is in hand before going up.
		if (!MC.player.getMainHandItem().is(Items.MACE)) {
			if (!autoSwitch.getValue())
				return;
			int maceSlot = InventoryUtils.findHotbarSlot(stack -> stack.is(Items.MACE));
			if (maceSlot == -1)
				return;
			swap(maceSlot, swapBack.getValue());
		}

		sendFillerPackets();
		Vec3 newPos = MC.player.position().add(0, height.getValue(), 0);
		MC.player.connection.send(new ServerboundMovePlayerPacket.Pos(newPos.x, newPos.y, newPos.z, false, false));
		jumped = true;
	}

	private void sendReturn() {
		sendFillerPackets();
		Vec3 pos = MC.player.position();
		MC.player.connection.send(new ServerboundMovePlayerPacket.Pos(pos.x, pos.y, pos.z, false, false));
	}

	private void sendFillerPackets() {
		int packetsRequired = (int) Math.ceil(Math.abs(height.getValue() / 10.0f));
		for (int i = 0; i < packetsRequired; i++)
			MC.player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(false, false));
	}
}
