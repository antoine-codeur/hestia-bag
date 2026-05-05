package com.hestia.bag.skins;

import java.util.Objects;

/**
 * A complete cosmetic theme covering both the placed bag entity and the
 * decoration of the dimension's spawn pad.
 *
 * <p>Each skin bundles:
 * <ul>
 *   <li>An entity model id used when spawning the placed bag.</li>
 *   <li>A "decoration set" id pointing to a structure or block palette applied
 *       to the dimension's spawn pad when this skin is equipped.</li>
 *   <li>How the player unlocks it (loot, achievement, command-only, etc.).</li>
 * </ul>
 *
 * <p>Records are perfect here: skins are pure data and should never mutate.
 *
 * @param id              unique identifier
 * @param displayName     name shown in the skin selector UI
 * @param entityModelId   asset id of the bag entity model for this skin
 * @param decorationSetId asset id of the dimension decoration palette
 * @param unlockSource    how the player gets this skin
 */
public record BagSkin(
        String id,
        String displayName,
        String entityModelId,
        String decorationSetId,
        UnlockSource unlockSource
) {

    public BagSkin {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(entityModelId, "entityModelId");
        Objects.requireNonNull(decorationSetId, "decorationSetId");
        Objects.requireNonNull(unlockSource, "unlockSource");
    }

    /**
     * Where a skin comes from. Drives both the unlock pipeline and the
     * tooltip shown in the skin selector ("Find this in Forgotten Temples").
     */
    public enum UnlockSource {
        /** Granted to every player on first home creation. The "vanilla" look. */
        DEFAULT,

        /** Found as loot in world chests, weighted by skin rarity. */
        WORLD_LOOT,

        /** Granted upon completing an achievement (e.g. "Install 10 upgrades"). */
        ACHIEVEMENT,

        /** Only obtainable through admin command. Reserved for events. */
        ADMIN_GRANT
    }
}
