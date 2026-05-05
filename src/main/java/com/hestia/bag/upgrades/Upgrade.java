package com.hestia.bag.upgrades;

import java.util.Objects;

/**
 * Immutable descriptor for a single upgrade type that can be looted and installed.
 *
 * <p>Records give us free immutability, equality, and {@code toString} — exactly
 * what we want for catalog data that's compared by id throughout the codebase.
 *
 * @param id           unique identifier, used in serialization and loot tables
 * @param type         broad functional category (see {@link UpgradeType})
 * @param displayName  human-readable name shown in tooltips and the socket UI
 * @param rarity       affects loot weight (see {@link Rarity#lootWeight()})
 * @param expandsArea  if true, installing this upgrade adds a 16x16 chunk to the
 *                     dimension footprint and triggers a re-platform pass
 */
public record Upgrade(
        String id,
        UpgradeType type,
        String displayName,
        Rarity rarity,
        boolean expandsArea
) {

    public Upgrade {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(rarity, "rarity");
        if (id.isBlank()) {
            throw new IllegalArgumentException("Upgrade id must not be blank");
        }
    }

    /**
     * Drop rarity. The {@code lootWeight} is the relative weight used when
     * rolling on a chest's loot table — higher values are more common.
     * Total weights across the catalog should land roughly around 100 so
     * the loot rate config setting feels intuitive ("5% chance per chest").
     */
    public enum Rarity {
        COMMON(50),
        UNCOMMON(25),
        RARE(10),
        EPIC(4),
        LEGENDARY(1);

        private final int lootWeight;

        Rarity(int lootWeight) {
            this.lootWeight = lootWeight;
        }

        public int lootWeight() {
            return lootWeight;
        }
    }
}
