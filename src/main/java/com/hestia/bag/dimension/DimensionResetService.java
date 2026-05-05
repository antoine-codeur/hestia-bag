package com.hestia.bag.dimension;

import com.hestia.bag.HestiaBagPlugin;
import com.hestia.bag.storage.PlacedBag;
import com.hestia.bag.storage.PlayerHome;

import java.util.Optional;
import java.util.UUID;

/**
 * Destructive operation: wipe a player's home back to a fresh state, dropping
 * everything currently inside the dimension into the outer world.
 *
 * <p>Drop strategy:
 * <ul>
 *   <li>If the player has a placed bag, drops happen at the bag's position.</li>
 *   <li>If not, drops happen at the player's current location.</li>
 *   <li>If neither is available (player offline, no placed bag), drops are
 *       held in a "pending recovery" stack on the home — picked up next time
 *       they place a bag.</li>
 * </ul>
 *
 * <p>What gets dropped:
 * <ul>
 *   <li>All container contents inside the dimension slot (chests, vault items,
 *       crafting station outputs).</li>
 *   <li>Mature crops from farm upgrades (carrots, herbs, hive products).</li>
 *   <li>Installed upgrade modules (revert from {@code installedUpgrades} to
 *       a fresh dropped item — does NOT preserve which were installed).</li>
 * </ul>
 *
 * <p>What survives:
 * <ul>
 *   <li>Unlocked skins (cosmetic progression is precious).</li>
 *   <li>The home's permission level, whitelist, and blacklist.</li>
 * </ul>
 */
public final class DimensionResetService {

    private final HestiaBagPlugin plugin;

    public DimensionResetService(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Reset a player's home. The caller (typically the socket UI's "Reset"
     * button after a confirmation modal) is responsible for ensuring the
     * owner consented — we do no further confirmation here.
     */
    public void reset(UUID ownerUuid) {
        PlayerHome home = plugin.homeStorage().getOrCreate(ownerUuid);

        // 1. Snapshot the dimension contents BEFORE wiping anything.
        //    The snapshot lists every item we need to drop and where.
        DimensionContentSnapshot snapshot = takeSnapshot(home);

        // 2. Determine the drop location — bag position preferred, else player loc,
        //    else stash on the home for later recovery.
        var dropTarget = chooseDropTarget(ownerUuid);

        // 3. Drop everything at the chosen target. Each method is best-effort —
        //    if a chest drop fails we still want the others to land.
        applyDrops(snapshot, dropTarget);

        // 4. Wipe the dimension blocks and entities so the next entry sees a
        //    clean slate.
        wipeDimensionContent(home);

        // 5. Reset the home's persistent state. We keep skins, permissions, and lists.
        home.resetToFresh();
        plugin.homeStorage().save(home);

        // TODO: plugin.getLogger().info("Home of {} has been reset.", ownerUuid);
    }

    // -----------------------------------------------------------------------
    // Snapshot
    // -----------------------------------------------------------------------

    /**
     * Walk the dimension volume and collect every dropable item.
     *
     * <p>⚠️ TODO — the actual scan needs the Hytale block + entity iteration API.
     * Pseudo-code:
     * <pre>
     *   World w = plugin.getServer().getWorld(HESTIA_WORLD_NAME);
     *   DimensionSlot slot = plugin.dimensions().slotFor(home.ownerUuid());
     *   int size = home.activeChunks() * 16;
     *   for (int dx = 0; dx < size; dx++) {
     *       for (int dz = 0; dz < size; dz++) {
     *           // Walk a vertical column above the floor
     *           for (int y = slot.y(); y < slot.y() + 32; y++) {
     *               Block b = w.getBlockAt(slot.x() + dx, y, slot.z() + dz);
     *               if (b.hasInventory()) snapshot.addContainer(b.inventory());
     *               if (b.isCrop() &amp;&amp; b.isMature()) snapshot.addCrop(b);
     *           }
     *       }
     *   }
     *   for (Entity e : w.getEntitiesIn(slot, size)) {
     *       if (e instanceof ItemDrop drop) snapshot.addItem(drop);
     *   }
     * </pre>
     */
    private DimensionContentSnapshot takeSnapshot(PlayerHome home) {
        // TODO: plugin.getLogger().info("[TODO] Snapshot dimension contents for {}.", home.ownerUuid());
        return new DimensionContentSnapshot();
    }

    // -----------------------------------------------------------------------
    // Drop targeting
    // -----------------------------------------------------------------------

    private DropTarget chooseDropTarget(UUID ownerUuid) {
        // Preference 1: placed bag in the world.
        Optional<PlacedBag> placed = plugin.placedBags().getByOwner(ownerUuid);
        if (placed.isPresent()) {
            return new DropTarget.AtPosition(placed.get().position());
        }

        // Preference 2: player's current location (only if online).
        // ⚠️ TODO — query online presence and capture the player's location:
        //   Player p = plugin.getServer().getPlayer(ownerUuid);
        //   if (p != null) return new DropTarget.AtPosition(toWorldPosition(p.getLocation()));

        // Fallback: stash for recovery on next bag placement.
        return new DropTarget.Pending();
    }

    private void applyDrops(DimensionContentSnapshot snapshot, DropTarget target) {
        switch (target) {
            case DropTarget.AtPosition at -> {
                // ⚠️ TODO — for each item in snapshot, spawn an item drop at `at.position`.
                // TODO: plugin.getLogger().info("[TODO] Spawn {} drops at {}.", snapshot.size(), at.position());
            }
            case DropTarget.Pending ignored -> {
                // ⚠️ TODO — store the snapshot on the home as pendingRecovery
                // so it gets dropped the next time the owner places a bag.
                // TODO: plugin.getLogger().info("[TODO] Stash {} drops as pending recovery.", snapshot.size());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Wipe
    // -----------------------------------------------------------------------

    private void wipeDimensionContent(PlayerHome home) {
        // ⚠️ TODO — clear the dimension volume:
        //   - Replace every non-floor block with air.
        //   - Despawn all non-player entities inside the slot.
        //   - The floor itself is regenerated by ensurePlatform() on next entry.
        // TODO: plugin.getLogger().info("[TODO] Wipe dimension contents for {}.", home.ownerUuid());
    }

    // -----------------------------------------------------------------------
    // Drop target ADT
    // -----------------------------------------------------------------------

    /** Where reset drops should go. Sealed = exhaustive switch in {@link #applyDrops}. */
    private sealed interface DropTarget {
        record AtPosition(PlayerHome.WorldPosition position) implements DropTarget {}
        record Pending() implements DropTarget {}
    }
}
