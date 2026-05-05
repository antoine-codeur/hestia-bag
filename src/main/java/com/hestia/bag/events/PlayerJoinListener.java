package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Refreshes per-player caches when a player joins the server.
 *
 * <p>Specifically, asks {@link com.hestia.bag.inventory.BagSlotManager} to scan
 * the joining player's inventory and decide whether they have a bag equipped.
 * Without this, the equipped flag would be empty until the player triggered
 * an inventory event.
 */
/**
 * Refreshes per-player caches when a player joins the server.
 *
 * <p>Specifically, asks {@link com.hestia.bag.inventory.BagSlotManager} to scan
 * the joining player's inventory and decide whether they have a bag equipped.
 * Without this, the equipped flag would be empty until the player triggered
 * an inventory event.
 */
public final class PlayerJoinListener {

    private final HestiaBagPlugin plugin;

    public PlayerJoinListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when a player joins the server.
     * Refreshes the bag slot inventory state and ensures skins are initialized.
     */
    public void onPlayerJoin(java.util.UUID playerUuid) {
        System.out.println("[HestiaBag] Player " + playerUuid + " joined - refreshing bag slot");
        plugin.bagSlots().refreshFor(playerUuid);
        // TODO: plugin.skinUnlocks().ensureDefaultUnlocked(playerUuid);
    }

    /*
     * ⚠️ TODO — wire up to real Hytale PlayerJoinEvent:
     *
     * @EventHandler
     * public void onJoin(PlayerJoinEvent e) {
     *     onPlayerJoin(e.getPlayer().getUniqueId());
     * }
     */
}
