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
public final class PlayerJoinListener {

    private final HestiaBagPlugin plugin;

    public PlayerJoinListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO — wire up the real PlayerJoinEvent equivalent:
     *
     * @EventHandler
     * public void onJoin(PlayerJoinEvent e) {
     *     UUID uuid = e.getPlayer().getUniqueId();
     *     plugin.bagSlots().refreshFor(uuid);
     *     plugin.skinUnlocks().ensureDefaultUnlocked(uuid);
     * }
     */
}
