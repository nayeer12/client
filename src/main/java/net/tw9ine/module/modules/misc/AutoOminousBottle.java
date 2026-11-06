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
import net.tw9ine.event.events.ItemUsedEvent;
import net.tw9ine.event.events.ReceivePacketEvent;
import net.tw9ine.event.listeners.ItemUsedListener;
import net.tw9ine.event.listeners.ReceivePacketListener;
import net.tw9ine.mixin.interfaces.IClientboundBossEventPacket;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;
import net.tw9ine.utils.FindItemResult;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AutoOminousBottle extends Module implements ReceivePacketListener, ItemUsedListener {
	private ItemStack lastUsedItemStack = null;
	private int previousSlot = -1;

	private final BooleanSetting swapBack = BooleanSetting.builder().id("auto_ominous_bottle_swap_back")
			.displayName("Swap Back")
			.description(
					"Whether the player's slot will be switched back to their previous slot after drinking the potion.")
			.defaultValue(true).build();

	public AutoOminousBottle() {
		super("AutoOminous");

		setCategory(Category.of("Misc"));
		setDescription("Automatically drinks a ominous potion when a raid ends.");

		addSetting(swapBack);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(ReceivePacketListener.class, this);
		Tw9ine.getInstance().eventManager.RemoveListener(ItemUsedListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(ReceivePacketListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(ItemUsedListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onReceivePacket(ReceivePacketEvent e) {
		// TODO: This will trigger on any boss bar...
		// Figure out a way to make it only occur on Raids

		Packet<?> packet = e.GetPacket();
		if (packet instanceof ClientboundBossEventPacket bossPacket) {
			IClientboundBossEventPacket iPacket = (IClientboundBossEventPacket) bossPacket;
			ClientboundBossEventPacket.Operation action = iPacket.getAction();
			if (action.getType() == ClientboundBossEventPacket.OperationType.REMOVE) {
				FindItemResult result = findInHotbar(Items.OMINOUS_BOTTLE);
				if (result.found()) {
					if (swapBack.getValue()) {
						previousSlot = MC.player.getInventory().getSelectedSlot();
					}

					int slot = result.slot();
					MC.player.getInventory().setSelectedSlot(slot);
					lastUsedItemStack = MC.player.getInventory().getItem(slot);
					MC.options.keyUse.setDown(true);
				}
			}
		}
	}

	@Override
	public void onItemUsed(ItemUsedEvent.Pre event) {

	}

	@Override
	public void onItemUsed(ItemUsedEvent.Post event) {
		if (lastUsedItemStack != null) {
			if (lastUsedItemStack == event.getItemStack()) {
				MC.options.keyUse.setDown(false);
				lastUsedItemStack = null;

				if (swapBack.getValue() && previousSlot != -1) {
					MC.player.getInventory().setSelectedSlot(previousSlot);
					previousSlot = -1;
				}
			}
		}
	}
}