package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Handles any player interacting with a placed bag entity (other than pickup,
 * which is its own listener).
 *
 * <p>Routes the interaction through {@link com.hestia.bag.permissions.PermissionManager}
 * to decide ALLOW / DENY / KNOCK_QUEUED / KNOCK_PENDING, and acts on the
 * decision:
 * <ul>
 *   <li>ALLOW → teleport the player into the owner's dimension.</li>
 *   <li>DENY → show the rejection message in chat.</li>
 *   <li>KNOCK_QUEUED → show "knocking..." HUD; owner gets a popup notification.</li>
 *   <li>KNOCK_PENDING → show "still knocking..." HUD.</li>
 * </ul>
 *
 * <p>Owner self-interaction is the most common case and is fast-pathed by the
 * permission manager (it returns ALLOW unconditionally for owner-on-self).
 */
public final class BagInteractListener {

    private final HestiaBagPlugin plugin;

    public BagInteractListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO:
     *
     * @EventHandler
     * public void onInteract(EntityInteractEvent e) {
     *     if (!isHestiaBagEntity(e.getEntity())) return;
     *
     *     PlacedBag bag = plugin.placedBags().getByEntity(e.getEntity().getUniqueId())
     *                           .orElse(null);
     *     if (bag == null) return;  // ghost entity? unregister-and-bail.
     *
     *     UUID actor = e.getPlayer().getUniqueId();
     *     AccessDecision decision = plugin.dimensions().tryEnter(actor, bag.ownerUuid());
     *
     *     switch (decision.outcome()) {
     *         case ALLOW         -> {} // already teleported by tryEnter
     *         case DENY          -> e.getPlayer().sendMessage(decision.reason());
     *         case KNOCK_QUEUED  -> e.getPlayer().sendActionBar(decision.reason());
     *         case KNOCK_PENDING -> e.getPlayer().sendActionBar(decision.reason());
     *     }
     *
     *     e.setCancelled(true);  // we handled the interaction
     * }
     */
}
