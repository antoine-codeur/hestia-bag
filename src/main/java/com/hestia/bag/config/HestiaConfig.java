package com.hestia.bag.config;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Global plugin configuration loaded from {@code <data-folder>/config.json}.
 *
 * <p>Fields are public-final by design — this is configuration data, not
 * mutable state, so plain field access is preferable to getter ceremony.
 *
 * <p>Default values are tuned conservatively. Server admins are expected to
 * adjust them based on their server's pace and difficulty.
 */
public final class HestiaConfig {

    /** Number of 16x16 chunks added per area-expanding upgrade install. */
    public final int expansionPerUpgrade;

    /**
     * Hard cap on dimension footprint to prevent abuse. With the default
     * stride of 1024 blocks between slots, 64 chunks (1024 blocks) means
     * adjacent slots could touch — keep below this.
     */
    public final int maxActiveChunks;

    /** If false, players take fall damage normally inside the void (debug only). */
    public final boolean voidFallEnabled;

    /** Probability per loot-table-eligible chest to roll a Hestia upgrade. */
    public final double lootChestUpgradeRate;

    /** Probability per loot-table-eligible chest to roll a Hestia skin token. */
    public final double lootChestSkinRate;

    /** Time-to-live in seconds for a pending knock request before auto-deny. */
    public final long knockRequestTtlSeconds;

    /** If true, placing a second bag silently picks up the first one. */
    public final boolean autoPickupOnReplacement;

    private HestiaConfig(
            int expansionPerUpgrade,
            int maxActiveChunks,
            boolean voidFallEnabled,
            double lootChestUpgradeRate,
            double lootChestSkinRate,
            long knockRequestTtlSeconds,
            boolean autoPickupOnReplacement) {
        this.expansionPerUpgrade   = expansionPerUpgrade;
        this.maxActiveChunks       = maxActiveChunks;
        this.voidFallEnabled       = voidFallEnabled;
        this.lootChestUpgradeRate  = lootChestUpgradeRate;
        this.lootChestSkinRate     = lootChestSkinRate;
        this.knockRequestTtlSeconds= knockRequestTtlSeconds;
        this.autoPickupOnReplacement = autoPickupOnReplacement;
    }

    /**
     * Load config from disk, falling back to defaults if the file is missing
     * or malformed. The fallback path also writes a default file so admins
     * can edit it on next startup.
     *
     * <p>⚠️ TODO — actual JSON parsing requires Hytale's Codec system or a
     * bundled JSON library. Until then we always return defaults.
     */
    public static HestiaConfig loadOrDefault(HestiaBagPlugin plugin) {
        plugin.getLogger().info("HestiaConfig: using built-in defaults (file parsing TODO).");
        return defaults();
    }

    private static HestiaConfig defaults() {
        return new HestiaConfig(
                /* expansionPerUpgrade     = */ 1,
                /* maxActiveChunks         = */ 32,
                /* voidFallEnabled         = */ true,
                /* lootChestUpgradeRate    = */ 0.05,  // 5%
                /* lootChestSkinRate       = */ 0.01,  // 1%
                /* knockRequestTtlSeconds  = */ 90,    // 1m30
                /* autoPickupOnReplacement = */ false
        );
    }
}
