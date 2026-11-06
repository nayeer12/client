/*
 * tw9ine
 * Copyright (C) 2026 nayeer12
 * Portions Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.tw9ine.module.modules.combat;

import net.tw9ine.Tw9ine;
import net.tw9ine.event.events.ReceivePacketEvent;
import net.tw9ine.event.events.TickEvent;
import net.tw9ine.event.listeners.ReceivePacketListener;
import net.tw9ine.event.listeners.TickListener;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.settings.types.FloatSetting;
import net.tw9ine.utils.player.InventoryUtils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoTotem extends Module implements TickListener, ReceivePacketListener {
	public enum Mode {
		/** Always keep a totem in the off-hand. */
		Always,
		/** Only equip a totem when in danger. */
		Danger
	}

	private final EnumSetting<Mode> mode = EnumSetting.<Mode>builder().id("autototem_mode").displayName("Mode")
			.description("Always: keep a totem in the off-hand at all times. Danger: only when health is low or a threat is near.")
			.defaultValue(Mode.Always).build();

	private final FloatSetting healthTrigger = FloatSetting.builder().id("autototem_health").displayName("Health")
			.description("Health (including absorption) at or below which you are considered in danger.")
			.defaultValue(10.0f).minValue(1.0f).maxValue(36.0f).step(1.0f).build();

	private final FloatSetting crystalRadiusTrigger = FloatSetting.builder().id("autototem_crystal_radius")
			.displayName("Crystal Radius")
			.description("An end crystal spawning within this radius counts as danger.").defaultValue(6.0f)
			.minValue(1.0f).maxValue(12.0f).step(1.0f).build();

	private final BooleanSetting fallCheck = BooleanSetting.builder().id("autototem_fall_check")
			.displayName("Fall Check").description("A fall that would kill you counts as danger.").defaultValue(true)
			.build();

	private final BooleanSetting mainHand = BooleanSetting.builder().id("autototem_mainhand").displayName("Main Hand")
			.description("When in danger, also select a totem from the hotbar for your main hand.")
			.defaultValue(false).build();

	private final FloatSetting delay = FloatSetting.builder().id("autototem_delay").displayName("Delay")
			.description("Ticks to wait between inventory moves. Higher is safer on laggy servers.").defaultValue(0f)
			.minValue(0f).maxValue(20f).step(1f).build();

	private int ticksSinceMove;
	private volatile boolean crystalThreat;

	public AutoTotem() {
		super("AutoTotem");

		setCategory(Category.of("Combat"));
		setDescription("Keeps a Totem of Undying in your off-hand.");

		addSetting(mode);
		addSetting(healthTrigger);
		addSetting(crystalRadiusTrigger);
		addSetting(fallCheck);
		addSetting(mainHand);
		addSetting(delay);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(TickListener.class, this);
		Tw9ine.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(TickListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
		ticksSinceMove = Integer.MAX_VALUE / 2;
		crystalThreat = false;
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onTick(TickEvent.Pre event) {
		LocalPlayer player = MC.player;
		if (player == null || !player.isAlive())
			return;

		ticksSinceMove++;
		boolean danger = isInDanger(player);
		crystalThreat = false;

		if (mode.getValue() == Mode.Danger && !danger)
			return;

		if (mainHand.getValue() && danger && !isTotem(player.getMainHandItem())) {
			int hotbarTotem = InventoryUtils.findHotbarSlot(AutoTotem::isTotem);
			if (hotbarTotem != -1)
				swap(hotbarTotem, false);
		}

		equipOffhandTotem(player);
	}

	@Override
	public void onTick(TickEvent.Post event) {
	}

	@Override
	public void onReceivePacket(ReceivePacketEvent event) {
		if (MC.player == null)
			return;

		// React the moment a crystal spawns nearby instead of waiting for damage.
		if (event.GetPacket() instanceof ClientboundAddEntityPacket spawn && spawn.getType() == EntityTypes.END_CRYSTAL) {
			double distSqr = MC.player.distanceToSqr(spawn.getX(), spawn.getY(), spawn.getZ());
			if (distSqr <= crystalRadiusTrigger.getValueSqr())
				crystalThreat = true;
		}
	}

	private void equipOffhandTotem(LocalPlayer player) {
		if (isTotem(player.getOffhandItem()))
			return;
		if (ticksSinceMove < delay.getValue())
			return;

		// Prefer the main inventory so hotbar totems remain available for the main hand.
		int slot = InventoryUtils.findSlot(AutoTotem::isTotem, true);
		if (slot == -1)
			return;

		if (InventoryUtils.swapWithOffhand(slot))
			ticksSinceMove = 0;
	}

	private boolean isInDanger(LocalPlayer player) {
		float health = player.getHealth() + player.getAbsorptionAmount();
		if (health <= healthTrigger.getValue())
			return true;
		if (crystalThreat)
			return true;
		if (fallCheck.getValue() && !player.onGround() && !player.isFallFlying() && !player.isInWater()
				&& player.fallDistance - 3.0 >= health)
			return true;
		return false;
	}

	private static boolean isTotem(ItemStack stack) {
		return stack.is(Items.TOTEM_OF_UNDYING);
	}

	/** Number of totems left, including the off-hand. */
	public int getTotemCount() {
		return MC.player == null ? 0 : InventoryUtils.count(AutoTotem::isTotem);
	}
}
