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
import net.tw9ine.event.events.MouseClickEvent;
import net.tw9ine.event.listeners.MouseClickListener;
import net.tw9ine.module.Category;
import net.tw9ine.module.Module;
import net.tw9ine.settings.types.EnumSetting;
import net.tw9ine.utils.FindItemResult;
import net.tw9ine.utils.types.MouseAction;
import net.tw9ine.utils.types.MouseButton;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public class MCA extends Module implements MouseClickListener {
	public enum Mode {
		FRIEND, PEARL, FIREWORK
	}

	private final EnumSetting<Mode> mode = EnumSetting.<Mode>builder().id("mca_mode").displayName("Mode")
			.description("The mode for the action to run when the middle mouse button is clicked.")
			.defaultValue(Mode.FRIEND).build();

	private int previousSlot = -1;

	public MCA() {
		super("MCA");

		setCategory(Category.of("misc"));
		setDescription("Middle Click Action");

		addSetting(mode);
	}

	@Override
	public void onDisable() {
		Tw9ine.getInstance().eventManager.RemoveListener(MouseClickListener.class, this);
	}

	@Override
	public void onEnable() {
		Tw9ine.getInstance().eventManager.AddListener(MouseClickListener.class, this);
	}

	@Override
	public void onToggle() {

	}

	@Override
	public void onMouseClick(MouseClickEvent mouseClickEvent) {
		if (mouseClickEvent.button != MouseButton.MIDDLE) return;

		if (mouseClickEvent.action == MouseAction.DOWN) {
			switch (mode.getValue()) {
				case FRIEND -> handleFriend(mouseClickEvent);
				case PEARL -> handleItem(mouseClickEvent, Items.ENDER_PEARL);
				case FIREWORK -> handleItem(mouseClickEvent, Items.FIREWORK_ROCKET);
			}
		} else if (mouseClickEvent.action == MouseAction.UP) {
			if ((mode.getValue() == Mode.PEARL || mode.getValue() == Mode.FIREWORK) && previousSlot != -1) {
				swap(previousSlot, false);
				previousSlot = -1;
				mouseClickEvent.cancel();
			}
		}
	}

	private void handleFriend(MouseClickEvent event) {
		if (!(MC.crosshairPickEntity instanceof Player player)) return;

		String playerName = player.getName().getString();

		if (Tw9ine.getInstance().friendsList.contains(player)) {
			Tw9ine.getInstance().friendsList.removeFriend(player.getUUID());
			sendChatMessage("Removed " + playerName + " from friends list.");
		} else {
			Tw9ine.getInstance().friendsList.addFriend(player);
			sendChatMessage("Added " + playerName + " to friends list.");
		}

		event.cancel();
	}

	private void handleItem(MouseClickEvent event, Item item) {
		FindItemResult itemResult = find(item);
		if (!itemResult.found() || !itemResult.isHotbar()) return;

		previousSlot = MC.player.getInventory().getSelectedSlot();

		if (!itemResult.isMainHand()) {
			swap(itemResult.slot(), false);
			MC.gameMode.useItem(MC.player, InteractionHand.MAIN_HAND);
			event.cancel();
		}
	}
}
