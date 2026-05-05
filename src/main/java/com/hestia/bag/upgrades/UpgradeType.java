package com.hestia.bag.upgrades;

/**
 * Functional categories an {@link Upgrade} can belong to.
 *
 * <p>Categories are intentionally broad. Each individual upgrade
 * (e.g. {@code small_workbench}, {@code large_workbench}) tags itself with a
 * type so configurable per-category caps can be enforced later (max one
 * workshop, max four decorations, etc.) without enumerating every upgrade id.
 */
public enum UpgradeType {

    /** Crafting stations: workbenches, anvils, alchemy tables. */
    WORKSHOP("Workshop"),

    /** Containers: chest racks, vaults, personal banks. */
    STORAGE("Storage"),

    /** Cultivable plots: herb gardens, beehives, orchards. */
    FARM("Farm"),

    /** Cosmetics: statues, fountains, paintings. Do NOT expand the area. */
    DECORATION("Decoration"),

    /** Extra waystone nodes and dimensional anchors for fast travel. */
    TELEPORT("Teleporter"),

    /** Pure floor expansions — each adds 16x16 to the dimension footprint. */
    EXPANSION("Expansion");

    private final String displayName;

    UpgradeType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
