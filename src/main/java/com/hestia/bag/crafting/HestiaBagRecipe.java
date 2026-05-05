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
        // ⚠️ IMPLEMENTATION STATUS: Awaiting official RecipeRegistry API documentation.
        //
        // Design intent:
        // Shape: LWL/WSW/LWL (Leather-Wool + Wool-Soulstone-Wool + Leather-Wool)
        // Station: vanilla:workbench or similar
        // Result: Hestia Bag tagged with crafter's UUID via resultModifier
        //
        // Expected implementation once API is documented:
        //   plugin.getRecipeRegistry().register(
        //       Recipe.builder(HestiaItems.HESTIA_BAG_ID)
        //           .station("vanilla:workbench")
        //           .shape(
        //               "LWL",
        //               "WSW",
        //               "LWL"
        //           )
        //           .ingredient('L', "minecraft:leather")
        //           .ingredient('W', "minecraft:wool")
        //           .ingredient('S', HestiaItems.SOULSTONE_ID)
        //           .resultModifier((player, itemStack) -> {
        //               // Tag the crafted bag with the crafter's UUID
        //               itemStack.setNbtString(HestiaItems.OWNER_NBT_KEY, 
        //                   player.getUniqueId().toString());
        //               return itemStack;
        //           })
        //           .build()
        //   );
        //
        // For now: stub. The recipe will not be accessible until the RecipeRegistry API is finalized.
        System.out.println("[HestiaBag] Registered " + HestiaItems.HESTIA_BAG_ID + " recipe (RecipeRegistry API pending)");
    }
}
