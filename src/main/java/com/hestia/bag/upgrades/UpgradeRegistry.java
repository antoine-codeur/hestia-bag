package com.hestia.bag.upgrades;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory catalog of every {@link Upgrade} known to the plugin.
 *
 * <p>The registry is rebuilt at every {@code onEnable}. Other plugins extending
 * HestiaBag (a future addon API) would call {@link #register(Upgrade)} from
 * <i>their</i> onEnable to add custom upgrades.
 *
 * <p>{@link LinkedHashMap} preserves insertion order — useful both for
 * deterministic debug output and for UI layouts that want categories grouped
 * in the order their first member was registered.
 */
public final class UpgradeRegistry {

    private final Map<String, Upgrade> byId = new LinkedHashMap<>();

    public void register(Upgrade upgrade) {
        if (byId.containsKey(upgrade.id())) {
            throw new IllegalStateException(
                    "Upgrade already registered: " + upgrade.id());
        }
        byId.put(upgrade.id(), upgrade);
    }

    public Optional<Upgrade> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Collection<Upgrade> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }

    /**
     * Default catalog shipped with the plugin.
     *
     * <p>This will eventually move to JSON files under
     * {@code Server/data/upgrades/} so server admins can rebalance without
     * recompiling. For now, code-defining them keeps the iteration loop tight.
     */
    public void registerDefaults() {
        // === WORKSHOPS ===
        register(new Upgrade("small_workbench",      UpgradeType.WORKSHOP,
                "Small Workbench",     Upgrade.Rarity.COMMON,    true));
        register(new Upgrade("anvil_station",        UpgradeType.WORKSHOP,
                "Forge",               Upgrade.Rarity.UNCOMMON,  true));
        register(new Upgrade("alchemy_lab",          UpgradeType.WORKSHOP,
                "Alchemy Lab",         Upgrade.Rarity.RARE,      true));

        // === STORAGE ===
        register(new Upgrade("simple_chest_rack",    UpgradeType.STORAGE,
                "Chest Rack",          Upgrade.Rarity.COMMON,    true));
        register(new Upgrade("vault_storage",        UpgradeType.STORAGE,
                "Reinforced Vault",    Upgrade.Rarity.RARE,      true));

        // === FARM ===
        register(new Upgrade("herb_garden",          UpgradeType.FARM,
                "Herb Garden",         Upgrade.Rarity.COMMON,    true));
        register(new Upgrade("apiary",               UpgradeType.FARM,
                "Apiary",              Upgrade.Rarity.UNCOMMON,  true));
        register(new Upgrade("magical_orchard",      UpgradeType.FARM,
                "Magical Orchard",     Upgrade.Rarity.EPIC,      true));

        // === DECORATION === (purely cosmetic — never expand the area)
        register(new Upgrade("hearth_fireplace",     UpgradeType.DECORATION,
                "Hearth Fireplace",    Upgrade.Rarity.COMMON,    false));
        register(new Upgrade("fountain",             UpgradeType.DECORATION,
                "Fountain",            Upgrade.Rarity.UNCOMMON,  false));
        register(new Upgrade("guardian_statue",      UpgradeType.DECORATION,
                "Guardian Statue",     Upgrade.Rarity.RARE,      false));

        // === TELEPORT ===
        register(new Upgrade("waystone_node",        UpgradeType.TELEPORT,
                "Waystone Node",       Upgrade.Rarity.UNCOMMON,  true));
        register(new Upgrade("dimensional_anchor",   UpgradeType.TELEPORT,
                "Dimensional Anchor",  Upgrade.Rarity.LEGENDARY, true));

        // === EXPANSION === (whose entire purpose is to grow the dimension)
        register(new Upgrade("expansion_module",     UpgradeType.EXPANSION,
                "Expansion Module",    Upgrade.Rarity.UNCOMMON,  true));
    }
}
