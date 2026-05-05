package com.hestia.bag.crafting;

import com.hestia.bag.HestiaBagPlugin;
import com.hestia.bag.items.HestiaItems;

/**
 * Registers the crafting recipe for the Hestia Bag item.
 *
 * <p>Design choices:
 * <ul>
 *   <li>The recipe is moderately expensive (rare materials) so the bag feels
 *       earned, not handed out. Tuning lives in {@link com.hestia.bag.config.HestiaConfig}.</li>
 *   <li>Crafting outputs a fresh bag <i>tagged with the crafter's UUID</i>.
 *       This is what makes "second bag = same dimension" work — every bag a
 *       player crafts inherits their UUID and points to their existing home.</li>
 *   <li>Bags crafted by player A cannot be used by player B. If B picks up A's
 *       dropped bag, it sits in their inventory but won't equip.</li>
 * </ul>
 *
 * <p>The recipe shape is deliberately a stub — Hytale's recipe API is one of
 * the systems most actively documented in the official progression update,
 * and the exact builder method names will likely change before 1.0.
 */
public final class HestiaBagRecipe {

    private HestiaBagRecipe() {}

    public static void register(HestiaBagPlugin plugin) {
        /*
         * ⚠️ TODO — replace with the real Hytale recipe API.
         *
         * Pseudo-code:
         *   RecipeRegistry.register(
         *       Recipe.builder(HestiaItems.HESTIA_BAG_ID)
         *           .station("hestia:workbench")          // or vanilla workbench
         *           .shape(
         *               "LWL",
         *               "WSW",                            // S = soulstone (loot drop), W = wool, L = leather
         *               "LWL"
         *           )
         *           .ingredient('L', "vanilla:leather")
         *           .ingredient('W', "vanilla:wool")
         *           .ingredient('S', "hestia:soulstone")
         *           .resultModifier((player, stack) -> {
         *               // Tag the crafted bag with the player's UUID so we know
         *               // which dimension to portal to when they equip + interact.
         *               stack.setNbt("owner", player.getUniqueId().toString());
         *               return stack;
         *           })
         *           .build()
         *   );
         */
        // TODO: plugin.getLogger().info("[TODO] Register Hestia Bag crafting recipe.");
    }
}
