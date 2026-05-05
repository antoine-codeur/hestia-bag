package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Handles a player attempting to place their equipped Hestia Bag in the world.
 *
 * <p>Gameplay rules enforced here:
 * <ul>
 *   <li>Only the bag's owner (NBT-tagged at craft time) can place it. If
 *       another player picked up a dropped bag, they see a "Not yours" message.</li>
 *   <li>One placed bag per owner — placing while a previous bag exists either
 *       picks up the previous one (configurable) or rejects the placement.</li>
 *   <li>Placement spawns a custom bag entity, registers it in
 *       {@link com.hestia.bag.storage.PlacedBagRegistry}, and consumes the
 *       inventory item.</li>
 * </ul>
 */
public final class BagPlacementListener {

    private final HestiaBagPlugin plugin;

    public BagPlacementListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO — concrete event hook:
     *
     * @EventHandler
     * public void onPlace(PlayerInteractEvent e) {
     *     // Only handle right-click-with-item on a block surface.
     *     if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
     *     ItemStack stack = e.getItem();
     *     if (!HestiaItems.isHestiaBag(stack)) return;
     *
     *     Player player = e.getPlayer();
     *     UUID actor = player.getUniqueId();
     *     UUID owner = readOwnerNbt(stack);
     *
     *     // Rule 1: only the owner can place.
     *     if (!actor.equals(owner)) {
     *         player.sendMessage("This bag belongs to someone else.");
     *         e.setCancelled(true);
     *         return;
     *     }
     *
     *     // Rule 2: one placed bag at a time.
     *     if (plugin.placedBags().hasPlacedBag(owner)) {
     *         player.sendMessage("Your bag is already placed elsewhere.");
     *         e.setCancelled(true);
     *         return;
     *     }
     *
     *     // Spawn the entity and register it.
     *     Location at = e.getClickedBlock().getLocation().add(0, 1, 0);
     *     UUID entityUuid = spawnBagEntity(at, owner);
     *     PlacedBag bag = new PlacedBag(
     *             entityUuid,
     *             owner,
     *             toWorldPosition(at),
     *             plugin.homeStorage().getOrCreate(owner).activeSkinId()
     *     );
     *     plugin.placedBags().register(bag);
     *
     *     // Consume the inventory stack.
     *     stack.setAmount(stack.getAmount() - 1);
     *
     *     e.setCancelled(true); // we handled it; don't let vanilla try anything
     * }
     */
}
