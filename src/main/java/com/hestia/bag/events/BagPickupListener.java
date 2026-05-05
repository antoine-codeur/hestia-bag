package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Handles the owner picking their placed bag back up.
 *
 * <p>Only the owner can pick up. Guests trying to pick up the bag get a "not
 * yours" message — preventing griefers from stealing/displacing other players'
 * homes.
 *
 * <p>Pickup steps:
 * <ol>
 *   <li>Validate the actor is the owner.</li>
 *   <li>If anyone is currently inside the dimension (owner or guests), kick
 *       them out before despawning the bag — otherwise guests get stranded.</li>
 *   <li>Despawn the entity, unregister from the placed-bag registry.</li>
 *   <li>Add a fresh bag stack (NBT-tagged with their UUID) to the owner's
 *       inventory.</li>
 * </ol>
 */
public final class BagPickupListener {

    private final HestiaBagPlugin plugin;

    public BagPickupListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO:
     *
     * @EventHandler
     * public void onPickup(EntityInteractEvent e) {
     *     if (!isHestiaBagEntity(e.getEntity())) return;
     *     if (!e.isShiftClick()) return;   // shift-right-click = pickup
     *
     *     UUID actor = e.getPlayer().getUniqueId();
     *     PlacedBag bag = plugin.placedBags().getByEntity(e.getEntity().getUniqueId())
     *                           .orElse(null);
     *     if (bag == null) return;
     *
     *     if (!actor.equals(bag.ownerUuid())) {
     *         e.getPlayer().sendMessage("This isn't your bag.");
     *         e.setCancelled(true);
     *         return;
     *     }
     *
     *     // Evict everyone currently inside the dimension before despawning.
     *     plugin.dimensions().evictAllFrom(actor);
     *
     *     // Despawn entity + unregister.
     *     e.getEntity().remove();
     *     plugin.placedBags().unregister(e.getEntity().getUniqueId());
     *
     *     // Give the owner a fresh tagged bag.
     *     ItemStack stack = newOwnerTaggedBag(actor);
     *     e.getPlayer().getInventory().addItem(stack);
     *
     *     e.setCancelled(true);
     * }
     */
}
