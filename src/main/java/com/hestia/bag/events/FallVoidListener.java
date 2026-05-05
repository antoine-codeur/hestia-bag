package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Listens for player movement inside the {@code hestia_void} world and
 * teleports anyone who falls below {@link com.hestia.bag.dimension.HestiaDimensionManager#VOID_FALL_Y}
 * back to the spawn pad of whichever home they're currently inside.
 *
 * <p>Knowing "which home are they in" matters: a guest visiting Alice's home
 * who falls off should respawn at Alice's pad, not at their own. This requires
 * the dimension manager to track current-home presence, which it does via the
 * {@code performTeleportToHome} call path.
 */
public final class FallVoidListener {

    private final HestiaBagPlugin plugin;

    public FallVoidListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO:
     *
     * @EventHandler
     * public void onMove(PlayerMoveEvent e) {
     *     Player p = e.getPlayer();
     *     Location loc = p.getLocation();
     *     if (!loc.getWorld().getName().equals(HestiaDimensionManager.HESTIA_WORLD_NAME)) {
     *         return;  // not in our world, ignore
     *     }
     *     if (loc.getY() >= HestiaDimensionManager.VOID_FALL_Y) {
     *         return;
     *     }
     *
     *     plugin.dimensions().respawnFromVoid(p.getUniqueId());
     *     e.setCancelled(true);  // prevent any vanilla void-damage
     * }
     */
}
