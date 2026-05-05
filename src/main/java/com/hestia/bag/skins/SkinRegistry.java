package com.hestia.bag.skins;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Catalog of {@link BagSkin}s registered in the plugin.
 *
 * <p>Mirrors the shape of {@link com.hestia.bag.upgrades.UpgradeRegistry}
 * deliberately — both are "named entity" registries with default catalogs and
 * an extension point for addons. Keeping the API symmetric helps anyone
 * navigating the codebase.
 */
public final class SkinRegistry {

    public static final String DEFAULT_SKIN_ID = "default";

    private final Map<String, BagSkin> byId = new LinkedHashMap<>();

    public void register(BagSkin skin) {
        if (byId.containsKey(skin.id())) {
            throw new IllegalStateException(
                    "Skin already registered: " + skin.id());
        }
        byId.put(skin.id(), skin);
    }

    public Optional<BagSkin> get(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public BagSkin getOrDefault(String id) {
        if (id == null) return byId.get(DEFAULT_SKIN_ID);
        return byId.getOrDefault(id, byId.get(DEFAULT_SKIN_ID));
    }

    public Collection<BagSkin> all() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public int size() {
        return byId.size();
    }

    /**
     * Default catalog. The asset ids ({@code hestia:skin/...}) are placeholders
     * that match the convention {@link com.hestia.bag.items.HestiaItems} uses
     * for its own asset references — actual model files come later from the
     * Blockbench artist or the Hytale Asset Editor.
     */
    public void registerDefaults() {
        register(new BagSkin(DEFAULT_SKIN_ID,
                "Wanderer's Bag",
                "hestia:skin/wanderer.bag",
                "hestia:decor/wanderer",
                BagSkin.UnlockSource.DEFAULT));

        register(new BagSkin("verdant",
                "Verdant Sanctuary",
                "hestia:skin/verdant.bag",
                "hestia:decor/verdant",
                BagSkin.UnlockSource.WORLD_LOOT));

        register(new BagSkin("ember",
                "Ember Forge",
                "hestia:skin/ember.bag",
                "hestia:decor/ember",
                BagSkin.UnlockSource.WORLD_LOOT));

        register(new BagSkin("celestial",
                "Celestial Chamber",
                "hestia:skin/celestial.bag",
                "hestia:decor/celestial",
                BagSkin.UnlockSource.ACHIEVEMENT));

        register(new BagSkin("void_walker",
                "Void Walker",
                "hestia:skin/void.bag",
                "hestia:decor/void",
                BagSkin.UnlockSource.WORLD_LOOT));
    }
}
