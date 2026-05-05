package com.hestia.bag.items;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Registers all custom items and blocks introduced by HestiaBag.
 *
 * <p>Item inventory:
 * <ul>
 *   <li><b>{@code hestia:bag}</b> — the placeable Hestia Bag, equipped in the
 *       custom slot. NBT-tagged with its owner's UUID at craft time.</li>
 *   <li><b>{@code hestia:socket}</b> — block placed inside the dimension that
 *       opens the upgrade/skin/permissions UI when interacted with.</li>
 *   <li><b>{@code hestia:soulstone}</b> — rare crafting reagent, dropped from
 *       Forgotten Temple bosses or generated as world loot.</li>
 *   <li><b>{@code hestia:upgrade.<id>}</b> — one item id per upgrade in the
 *       registry (we generate them at registration time). Right-clicking on
 *       the socket consumes the item and applies the upgrade.</li>
 *   <li><b>{@code hestia:skin_token.<id>}</b> — one item id per non-default
 *       skin. Right-clicking it from inventory unlocks the skin.</li>
 * </ul>
 *
 * <p>Why one item per upgrade rather than a single generic item with NBT?
 * Hytale's loot table system is JSON-driven and works most cleanly with
 * concrete item ids. Keeping the loot tables readable for mod authors is
 * more valuable than the slight schema duplication.
 */
public final class HestiaItems {

    public static final String HESTIA_BAG_ID         = "hestia:bag";
    public static final String HESTIA_SOCKET_ID      = "hestia:socket";
    public static final String SOULSTONE_ID          = "hestia:soulstone";
    public static final String UPGRADE_ITEM_PREFIX   = "hestia:upgrade.";
    public static final String SKIN_TOKEN_PREFIX     = "hestia:skin_token.";

    /** NBT key under which the bag stores its owner's UUID after crafting. */
    public static final String OWNER_NBT_KEY = "hestia.owner";

    private HestiaItems() {}

    public static void registerAll(HestiaBagPlugin plugin) {
        registerBag(plugin);
        registerSocket(plugin);
        registerSoulstone(plugin);
        registerUpgradeItems(plugin);
        registerSkinTokens(plugin);
    }

    /** Builds a deterministic item id for an upgrade given its registry id. */
    public static String upgradeItemId(String upgradeId) {
        return UPGRADE_ITEM_PREFIX + upgradeId;
    }

    /** Builds a deterministic item id for a skin token given its registry id. */
    public static String skinTokenItemId(String skinId) {
        return SKIN_TOKEN_PREFIX + skinId;
    }

    // -----------------------------------------------------------------------
    // Per-item registration stubs — each one gets fleshed out once the Hytale
    // ItemRegistry API surface is confirmed.
    // -----------------------------------------------------------------------

    private static void registerBag(HestiaBagPlugin plugin) {
        // ⚠️ TODO:
        //   ItemRegistry.register(Item.builder(HESTIA_BAG_ID)
        //       .displayName("Hestia Bag")
        //       .stackSize(1)                       // bags don't stack — each is owner-tagged
        //       .model("hestia:item/bag.bbmodel")
        //       .tooltipLine("Place to access your private home.")
        //       .build());
        // TODO: plugin.getLogger().info("[TODO] Register item {}.", HESTIA_BAG_ID);
    }

    private static void registerSocket(HestiaBagPlugin plugin) {
        // ⚠️ TODO: register as a placeable BLOCK, not a regular item.
        //   BlockRegistry.register(Block.builder(HESTIA_SOCKET_ID)
        //       .model("hestia:block/socket.bbmodel")
        //       .hardness(5.0f)
        //       .interactionHandler(...)
        //       .build());
        // TODO: plugin.getLogger().info("[TODO] Register block {}.", HESTIA_SOCKET_ID);
    }

    private static void registerSoulstone(HestiaBagPlugin plugin) {
        // ⚠️ TODO: rare reagent for the bag recipe.
        // TODO: plugin.getLogger().info("[TODO] Register item {}.", SOULSTONE_ID);
    }

    private static void registerUpgradeItems(HestiaBagPlugin plugin) {
        // Generate one item per registered upgrade. Done after upgrades are
        // registered so we can introspect their display name and rarity for
        // the tooltip.
        plugin.upgrades().all().forEach(upgrade -> {
            String itemId = upgradeItemId(upgrade.id());
            // ⚠️ TODO: ItemRegistry.register(Item.builder(itemId)... .build());
            // TODO: plugin.getLogger().debug("[TODO] Register upgrade item {}.", itemId);
        });
    }

    private static void registerSkinTokens(HestiaBagPlugin plugin) {
        // Generate one token item per non-default skin so loot tables can drop them.
        plugin.skins().all().stream()
                .filter(skin -> skin.unlockSource() == com.hestia.bag.skins.BagSkin.UnlockSource.WORLD_LOOT)
                .forEach(skin -> {
                    String itemId = skinTokenItemId(skin.id());
                    // ⚠️ TODO: ItemRegistry.register(...)
                    // TODO: plugin.getLogger().debug("[TODO] Register skin token {}.", itemId);
                });
    }
}
