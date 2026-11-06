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
import net.tw9ine.event.events.SendMovementPacketEvent;
import net.tw9ine.event.events.SendPacketEvent;
import net.tw9ine.event.listeners.SendMovementPacketListener;
import net.tw9ine.event.listeners.SendPacketListener;
import net.tw9ine.mixin.interfaces.IServerboundMovePlayerPacket;
import net.tw9ine.module.AntiCheat;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.BooleanSetting;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class AntiHunger extends Module implements SendPacketListener, SendMovementPacketListener {
	private boolean lastOnGround, ignorePacket;

	private final BooleanSetting sprint = BooleanSetting.builder().id("antihunger_sprint").displayName("Sprint")
			.description("Change sprint packets.").defaultValue(true).build();

	private final BooleanSetting onGround = BooleanSetting.builder().id("antihunger_onground").displayName("On Ground")
			.description("Fakes onGround.").defaultValue(true).build();

	public AntiHunger() {
		super("AntiHunger");
		setCategory(Category.of("Misc"));
		setDescription("Reduces the amount of hunger that is consumed.");

		addSetting(sprint);
		addSetting(onGround);

		setDetectable(
				AntiCheat.Vulcan,
				AntiCheat.AdvancedAntiCheat,
				AntiCheat.Verus,
				AntiCheat.Grim,
				AntiCheat.Matrix,
				AntiCheat.Negativity,
				AntiCheat.Karhu
		);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(SendPacketListener.class, this);
		Tw9ine.getInstance().eventManager.RemoveListener(SendMovementPacketListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(SendPacketListener.class, this);
		Tw9ine.getInstance().eventManager.AddListener(SendMovementPacketListener.class, this);

		if(MC.player != null) {
			lastOnGround = MC.player.onGround();
		}else {
			lastOnGround = false;
		}
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onSendPacket(SendPacketEvent event) {
		if (ignorePacket && event.GetPacket() instanceof ServerboundMovePlayerPacket) {
			ignorePacket = false;
			return;
		}

		if (MC.player != null) {
			if (MC.player.isPassenger() || MC.player.isInWater() || MC.player.isUnderWater())
				return;

			if (event.GetPacket() instanceof ServerboundMovePlayerPacket packet && onGround.getValue() && MC.player.onGround()
					&& MC.player.fallDistance <= 0.0 && !MC.gameMode.isDestroying()) {
				((IServerboundMovePlayerPacket) packet).setOnGround(false);
			}
		}

		if (event.GetPacket() instanceof ServerboundPlayerCommandPacket packet && sprint.getValue()) {
			if (packet.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING)
				event.cancel();
		}
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Pre event) {
		if (MC.player.onGround() && !lastOnGround && onGround.getValue()) {
			ignorePacket = true;
		}

		lastOnGround = MC.player.onGround();
	}

	@Override
	public void onSendMovementPacket(SendMovementPacketEvent.Post event) {

	}
}
