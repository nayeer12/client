/*
 * Aoba Hacked Client
 * Copyright (C) 2019-2024 coltonk9043
 *
 * Licensed under the GNU General Public License, Version 3 or later.
 * See <http://www.gnu.org/licenses/>.
 */

package net.aoba.utils.player;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/**
 * Server-synchronised inventory helpers. Every move goes through the container
 * click packet so the server and client inventories never disagree.
 */
public final class InventoryUtils {
	private static final Minecraft MC = Minecraft.getInstance();

	/** Button index the server uses for the off-hand when swapping slots. */
	private static final int OFFHAND_SWAP_BUTTON = 40;

	private InventoryUtils() {
	}

	/**
	 * Whether inventory clicks can be sent right now (no other container such as
	 * a chest is open).
	 */
	public static boolean canClickInventory() {
		LocalPlayer player = MC.player;
		return player != null && MC.gameMode != null && player.containerMenu == player.inventoryMenu;
	}

	/**
	 * Finds an inventory index (0-35) whose stack matches the predicate.
	 *
	 * @param preferMain When true, the main inventory (9-35) is searched before
	 *                   the hotbar so hotbar items stay where they are.
	 * @return Inventory index, or -1 if nothing matches.
	 */
	public static int findSlot(Predicate<ItemStack> predicate, boolean preferMain) {
		Inventory inventory = MC.player.getInventory();
		if (preferMain) {
			for (int i = 9; i < 36; i++)
				if (predicate.test(inventory.getItem(i)))
					return i;
			for (int i = 0; i < 9; i++)
				if (predicate.test(inventory.getItem(i)))
					return i;
		} else {
			for (int i = 0; i < 36; i++)
				if (predicate.test(inventory.getItem(i)))
					return i;
		}
		return -1;
	}

	/** Finds a hotbar slot (0-8) matching the predicate, or -1. */
	public static int findHotbarSlot(Predicate<ItemStack> predicate) {
		Inventory inventory = MC.player.getInventory();
		for (int i = 0; i < 9; i++)
			if (predicate.test(inventory.getItem(i)))
				return i;
		return -1;
	}

	/** Counts all items matching the predicate, including the off-hand. */
	public static int count(Predicate<ItemStack> predicate) {
		Inventory inventory = MC.player.getInventory();
		int total = 0;
		for (int i = 0; i < 36; i++) {
			ItemStack stack = inventory.getItem(i);
			if (predicate.test(stack))
				total += stack.getCount();
		}
		ItemStack offhand = MC.player.getOffhandItem();
		if (predicate.test(offhand))
			total += offhand.getCount();
		return total;
	}

	/**
	 * Converts a player inventory index (0-35) to its slot id inside the player
	 * inventory menu.
	 */
	public static int toMenuSlot(int inventoryIndex) {
		return inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
	}

	/**
	 * Swaps the given inventory index with the off-hand using a real container
	 * click, so the server agrees with the result.
	 *
	 * @return True if the click was sent.
	 */
	public static boolean swapWithOffhand(int inventoryIndex) {
		if (inventoryIndex < 0 || inventoryIndex > 35 || !canClickInventory())
			return false;

		LocalPlayer player = MC.player;
		MC.gameMode.handleContainerInput(player.inventoryMenu.containerId, toMenuSlot(inventoryIndex),
				OFFHAND_SWAP_BUTTON, ContainerInput.SWAP, player);
		return true;
	}

	/**
	 * Swaps the given inventory index into a hotbar slot using a real container
	 * click.
	 *
	 * @return True if the click was sent.
	 */
	public static boolean swapWithHotbar(int inventoryIndex, int hotbarSlot) {
		if (inventoryIndex < 0 || inventoryIndex > 35 || hotbarSlot < 0 || hotbarSlot > 8
				|| !canClickInventory())
			return false;
		if (inventoryIndex == hotbarSlot)
			return true;

		LocalPlayer player = MC.player;
		MC.gameMode.handleContainerInput(player.inventoryMenu.containerId, toMenuSlot(inventoryIndex), hotbarSlot,
				ContainerInput.SWAP, player);
		return true;
	}
}
