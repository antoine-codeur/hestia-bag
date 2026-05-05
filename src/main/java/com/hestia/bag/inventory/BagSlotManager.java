package com.hestia.bag.inventory;

import com.hestia.bag.HestiaBagPlugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages a <i>virtual</i> "Hestia Bag slot" for each player.
 *
 * <p>Why virtual? Hytale's inventory API (as documented at the time of writing)
 * does not expose a clean hook for mods to inject native equipment slots. A few
 * approaches exist:
 * <ul>
 *   <li><b>True native slot</b> — would require client-side mods, which Hytale
 *       does not allow (server-first architecture, see hytale.com modding
 *       strategy doc).</li>
 *   <li><b>Curio-like attachment slot</b> — possible if Hytale ships an
 *       equipment-slot extension API later. Watch for {@code AttachmentSlots}
 *       or similar in the changelogs.</li>
 *   <li><b>Virtual slot via UI overlay</b> — the approach taken here. We track
 *       per-player which bag is "equipped" in our own data structure and
 *       render an overlay UI element for the slot. The bag never physically
 *       lives in vanilla inventory.</li>
 * </ul>
 *
 * <p>The bag stays tracked by entity in two cases:
 * <ol>
 *   <li><b>Equipped</b> — the player has an inventory item ItemStack tagged as
 *       their bag, and we treat that as "in the slot". Picking it up from
 *       crafting auto-equips.</li>
 *   <li><b>Placed</b> — the bag has been placed in the world and the
 *       {@link com.hestia.bag.storage.PlacedBagRegistry} owns the reference.</li>
 * </ol>
 *
 * <p>This manager handles the equipped state. The placed state is owned by the
 * placed-bag registry; the two are mutually exclusive for a given owner UUID
 * (you can't both wear and place the same bag at the same time).
 */
public final class BagSlotManager {

    private final HestiaBagPlugin plugin;

    /**
     * playerUuid → true if they currently have a bag equipped (in inventory
     * with our owner-NBT). We don't store the full ItemStack here because the
     * stack lives in the player's inventory and is the source of truth — this
     * map is just a fast "do they have one?" cache rebuilt on join.
     */
    private final ConcurrentMap<UUID, Boolean> equippedFlag = new ConcurrentHashMap<>();

    public BagSlotManager(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Recompute the equipped flag for a player by scanning their inventory.
     * Called on join, on inventory close, and after any inventory mutation
     * we care about.
     *
     * <p>⚠️ IMPLEMENTATION STATUS: Awaiting full Hytale inventory iteration API.
     *
     * Expected implementation once Player API is available:
     * <pre>
     *   Player player = plugin.getServer().getPlayer(playerUuid);
     *   if (player == null) return; // player offline
     *
     *   for (ItemStack stack : player.getInventory().contents()) {
     *       if (stack != null
     *               &amp;&amp; HestiaItems.HESTIA_BAG_ID.equals(stack.getItemId())
     *               &amp;&amp; playerUuid.toString().equals(stack.getNbtString(HestiaItems.OWNER_NBT_KEY))) {
     *           equippedFlag.put(playerUuid, true);
     *           System.out.println("[HestiaBag] " + playerUuid + " equipped bag");
     *           return;
     *       }
     *   }
     *   equippedFlag.remove(playerUuid);
     *   System.out.println("[HestiaBag] " + playerUuid + " no bag equipped");
     * </pre>
     *
     * For now: placeholder implementation. Equipment state is not tracked
     * until the inventory API is available.
     */
    public void refreshFor(Object playerOrUuid) {
        System.out.println("[HestiaBag] refreshFor(" + playerOrUuid + ") — awaiting inventory iteration API");
        // Temporary: assume no bag equipped until inventory API available
        if (playerOrUuid instanceof UUID) {
            equippedFlag.remove(playerOrUuid);
        }
    }

    /** Quick check used by interaction handlers. */
    public boolean hasBagEquipped(UUID playerUuid) {
        return equippedFlag.getOrDefault(playerUuid, false);
    }

    /**
     * Hook called when the player picks up or crafts a bag. If they already
     * have one equipped (or placed), we silently void the new one — design
     * spec says "one player, one dimension" extends to "one bag in flight".
     *
     * <p>⚠️ IMPLEMENTATION STATUS: Awaiting inventory mutation and placed-bag APIs.
     *
     * Expected implementation:
     * <pre>
     *   // Check if player already has a bag equipped or placed
     *   if (hasBagEquipped(playerUuid) || plugin.placedBags().hasPlaced(playerUuid)) {
     *       // Remove the duplicate stack
     *       Player player = plugin.getServer().getPlayer(playerUuid);
     *       if (player != null) {
     *           // Iterate inventory, find the new bag, and remove it
     *           for (ItemStack stack : player.getInventory().contents()) {
     *               if (stack != null &amp;&amp; HestiaItems.HESTIA_BAG_ID.equals(stack.getItemId())) {
     *                   stack.setAmount(0);
     *                   player.sendMessage("You already have a bag equipped or placed!");
     *                   return;
     *               }
     *           }
     *       }
     *   }
     *   refreshFor(playerUuid);
     * </pre>
     */
    public void onBagAcquired(UUID playerUuid) {
        System.out.println("[HestiaBag] onBagAcquired(" + playerUuid + ") — awaiting inventory/dedup API");
        refreshFor(playerUuid);
    }
}
