package com.hestia.bag.storage;

import com.hestia.bag.permissions.BagPermissionLevel;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Mutable persistent state for a single player's Hestia Bag home.
 *
 * <p>One instance per player, identified by {@link #ownerUuid()}. Crafting a
 * second bag does <i>not</i> create a second home — both bags portal to this
 * same instance. The data lives in {@code <data-folder>/homes/<uuid>.json}.
 *
 * <p>This class is intentionally a "fat data class" rather than a record:
 * it has too many fields to construct positionally, and most fields are mutable
 * sets that we want to expose by reference for in-place updates.
 *
 * <p>Concurrency note: a single home is only ever accessed from event handlers
 * for its owner or guests currently inside the dimension. Hytale's event bus
 * is largely single-threaded for these cases, so we do <i>not</i> wrap the
 * collections in synchronized variants. Bulk save/load operations snapshot.
 */
public final class PlayerHome {

    private final UUID ownerUuid;

    // === Upgrade state ===

    /** IDs of upgrades currently mounted on the socket, in install order. */
    private final Set<String> installedUpgrades = new LinkedHashSet<>();

    /** IDs of upgrades the player has looted but not yet installed. */
    private final Set<String> ownedUpgrades = new LinkedHashSet<>();

    // === Skin state ===

    /** IDs of skins the player has unlocked. */
    private final Set<String> unlockedSkins = new LinkedHashSet<>();

    /** Currently-equipped skin id; null means "default". */
    private String activeSkinId;

    // === Dimension layout ===

    /**
     * Number of 16x16 chunks active in this home. Starts at 1 (the spawn pad);
     * each upgrade with {@code expandsArea = true} increments it by 1.
     */
    private int activeChunks = 1;

    // === Re-entry / exit ===

    /** Where the player was standing when they last entered the home. */
    private WorldPosition lastExitPosition;

    // === Permissions ===

    private BagPermissionLevel permissionLevel = BagPermissionLevel.PRIVATE;

    /** UUIDs explicitly allowed in WHITELIST mode. */
    private final Set<UUID> whitelist = new LinkedHashSet<>();

    /** UUIDs denied access regardless of mode. Always wins over whitelist. */
    private final Set<UUID> blacklist = new LinkedHashSet<>();

    public PlayerHome(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    // === Read-only accessors ===

    public UUID        ownerUuid()         { return ownerUuid;         }
    public Set<String> installedUpgrades() { return installedUpgrades; }
    public Set<String> ownedUpgrades()     { return ownedUpgrades;     }
    public Set<String> unlockedSkins()     { return unlockedSkins;     }
    public Set<UUID>   whitelist()         { return whitelist;         }
    public Set<UUID>   blacklist()         { return blacklist;         }
    public int         activeChunks()      { return activeChunks;      }

    public String             activeSkinId()    { return activeSkinId;    }
    public WorldPosition      lastExitPosition(){ return lastExitPosition;}
    public BagPermissionLevel permissionLevel() { return permissionLevel; }

    // === Mutators ===

    public void setActiveSkinId(String id)             { this.activeSkinId = id;       }
    public void setLastExitPosition(WorldPosition pos) { this.lastExitPosition = pos; }
    public void setPermissionLevel(BagPermissionLevel l){ this.permissionLevel = l;   }

    /**
     * Move an upgrade from the owned set to the installed set. If the upgrade
     * expands the area, also bumps {@link #activeChunks}.
     *
     * @return {@code true} if the install happened, {@code false} if the
     *         upgrade wasn't owned or was already installed.
     */
    public boolean install(String upgradeId, boolean expandsArea) {
        if (!ownedUpgrades.remove(upgradeId)) return false;
        if (!installedUpgrades.add(upgradeId)) {
            // Wasn't installed before but failed to add — race or duplicate id.
            // Roll back the remove() so state stays consistent.
            ownedUpgrades.add(upgradeId);
            return false;
        }
        if (expandsArea) activeChunks++;
        return true;
    }

    /** Reverse of {@link #install}. */
    public boolean uninstall(String upgradeId, boolean expandsArea) {
        if (!installedUpgrades.remove(upgradeId)) return false;
        ownedUpgrades.add(upgradeId);
        if (expandsArea && activeChunks > 1) activeChunks--;
        return true;
    }

    /**
     * Wipe all home state back to a fresh first-craft state.
     * <p>Called by {@code DimensionResetService} <i>after</i> it has snapshotted
     * the dimension contents and dropped them in the outer world.
     */
    public void resetToFresh() {
        installedUpgrades.clear();
        ownedUpgrades.clear();
        activeChunks = 1;
        // We keep: unlockedSkins (cosmetic progression is precious),
        //          activeSkinId, permissionLevel, whitelist, blacklist.
    }

    /**
     * Plain-data position record for the outer world. We deliberately don't
     * reference Hytale's Location class here so this stays trivially serializable.
     */
    public record WorldPosition(
            String worldName,
            double x, double y, double z,
            float yaw, float pitch
    ) {}
}
