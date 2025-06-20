package io.github.n1ck145.redhook.manager.menu;

import io.github.n1ck145.redhook.api.inventory.InteractiveMenu;
import io.github.n1ck145.redhook.constants.PlayerState;
import io.github.n1ck145.redhook.manager.state.PlayerStateManager;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuManager {
	private static final Map<UUID, InteractiveMenu> openMenus = new HashMap<>();

	/**
	 * Registers and opens a paginated menu for the given player.
	 */
	public static void openMenu(Player player, InteractiveMenu menu) {
		openMenus.put(player.getUniqueId(), menu);
		menu.open();
	}

	/**
	 * Handles clicks in a menu if one is registered for the player.
	 */
	public static void handleClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player))
			return;

		InteractiveMenu menu = openMenus.get(player.getUniqueId());
		if (menu != null) {
			menu.handleClick(event);
		}
	}

	/**
	 * Unregisters the menu when the inventory is closed.
	 */
	public static void handleClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player player))
			return;

		Boolean persistInventoryState =
				PlayerStateManager.getPlayerState(player, PlayerState.REDHOOK_PERSIST_INVENTORY_STATE, false);

		if (persistInventoryState)
			return;

		openMenus.remove(player.getUniqueId());
	}

	/**
	 * (Optional) Check if a menu is open for a player.
	 */
	public static boolean hasMenu(Player player) {
		return openMenus.containsKey(player.getUniqueId());
	}

	/**
	 * Gets the current menu for a player.
	 */
	public static InteractiveMenu getCurrentMenu(Player player) {
		return openMenus.get(player.getUniqueId());
	}
}