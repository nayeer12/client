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
import net.tw9ine.event.events.SendPacketEvent;
import net.tw9ine.event.listeners.SendPacketListener;
import net.tw9ine.module.AntiCheat;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public class XCarry extends Module implements SendPacketListener {
	public XCarry() {
		super("XCarry");
		setCategory(Category.of("Misc"));
		setDescription("Allows you to store items in your crafting slot..");

		isDetectable(AntiCheat.Negativity);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(SendPacketListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		Packet<?> packet = event.GetPacket();
		if (packet instanceof ServerboundContainerClosePacket closeScreenPacket) {
            if (closeScreenPacket.getContainerId() == MC.player.inventoryMenu.containerId)
				event.cancel();
		}
	}
}
