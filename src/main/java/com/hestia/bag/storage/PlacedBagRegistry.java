package com.hestia.bag.storage;

import com.hestia.bag.HestiaBagPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of all currently-placed bag entities across the server.
 *
 * <p>Two indices for fast lookup from either direction:
 * <ul>
 *   <li>By entity UUID — when an interact event fires, we get the entity uuid.</li>
 *   <li>By owner UUID — when an owner picks up "their" bag, or when we need to
 *       check "does this player already have a bag placed somewhere?".</li>
 * </ul>
 *
 * <p>One owner = one placed bag at a time. If the player crafts a second bag
 * and tries to place it, we either reject or auto-pickup the previous one
 * (configurable in {@code HestiaConfig}).
 */
public final class PlacedBagRegistry {

    private final HestiaBagPlugin plugin;

    private final Map<UUID, PlacedBag> byEntity = new ConcurrentHashMap<>();
    private final Map<UUID, UUID>      byOwner  = new ConcurrentHashMap<>(); // owner → entity

    public PlacedBagRegistry(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Register a newly-placed bag. If the owner already had a bag placed
     * elsewhere, the previous one is silently replaced — callers should have
     * already removed/picked-up the previous entity before calling this.
     */
    public void register(PlacedBag bag) {
        // Drop any previous registration for this owner.
        UUID previousEntity = byOwner.put(bag.ownerUuid(), bag.entityUuid());
        if (previousEntity != null) {
            byEntity.remove(previousEntity);
        }
        byEntity.put(bag.entityUuid(), bag);
    }

    /** Remove a bag from the registry (called on pickup or entity despawn). */
    public void unregister(UUID entityUuid) {
        PlacedBag removed = byEntity.remove(entityUuid);
        if (removed != null) {
            // Only clear the owner→entity link if it still points at the bag we
            // just removed. Otherwise we'd nuke a newer placement made between
            // the despawn event and this call.
            byOwner.remove(removed.ownerUuid(), entityUuid);
        }
    }

    public Optional<PlacedBag> getByEntity(UUID entityUuid) {
        return Optional.ofNullable(byEntity.get(entityUuid));
    }

    public Optional<PlacedBag> getByOwner(UUID ownerUuid) {
        UUID entity = byOwner.get(ownerUuid);
        return entity == null ? Optional.empty() : getByEntity(entity);
    }

    public boolean hasPlacedBag(UUID ownerUuid) {
        return byOwner.containsKey(ownerUuid);
    }

    /**
     * Bulk save called at shutdown. The registry is reconstructed at startup by
     * scanning the world for our entity type, so this serialization is a backup
     * and quick-recovery aid rather than the source of truth.
     *
     * <p>⚠️ TODO — implement persistence. Lowest-effort plan: write
     * {@code placed_bags.json} alongside the homes directory.
     */
    public void saveAll() {
        Map<UUID, PlacedBag> snapshot = new HashMap<>(byEntity);
        plugin.getLogger().info("[TODO] Persist {} placed bags to disk.", snapshot.size());
    }
}
