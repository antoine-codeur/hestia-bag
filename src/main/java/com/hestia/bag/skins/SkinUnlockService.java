package com.hestia.bag.skins;

import com.hestia.bag.HestiaBagPlugin;
import com.hestia.bag.storage.PlayerHome;
import com.hestia.bag.storage.PlayerHomeStorage;

import java.util.Optional;
import java.util.UUID;

/**
 * Service mediating the unlock and equip operations for {@link BagSkin}s.
 *
 * <p>Splitting this out from {@link SkinRegistry}:
 * <ul>
 *   <li>The registry is read-mostly catalog data.</li>
 *   <li>This service is write-side, talking to {@link PlayerHome} state.</li>
 * </ul>
 *
 * <p>Methods return booleans rather than throwing on the common failure cases
 * (skin doesn't exist, player hasn't unlocked it). The reasoning: equip/unlock
 * happen from UI handlers that already need to render an error message, and
 * try/catch around expected outcomes is awkward.
 */
public final class SkinUnlockService {

    private final HestiaBagPlugin plugin;
    private final PlayerHomeStorage homeStorage;

    public SkinUnlockService(HestiaBagPlugin plugin, PlayerHomeStorage homeStorage) {
        this.plugin = plugin;
        this.homeStorage = homeStorage;
    }

    /**
     * Unlock a skin for a player.
     *
     * @return true if newly unlocked, false if the player already owned it
     *         or the skin id is unknown
     */
    public boolean unlock(UUID playerUuid, String skinId) {
        Optional<BagSkin> skin = plugin.skins().get(skinId);
        if (skin.isEmpty()) {
            // TODO: plugin.getLogger().warn(
            //         "Refused to unlock unknown skin '{}' for player {}", skinId, playerUuid);
            return false;
        }
        PlayerHome home = homeStorage.getOrCreate(playerUuid);
        boolean added = home.unlockedSkins().add(skinId);
        if (added) {
            homeStorage.save(home);
            // TODO: plugin.getLogger().info(
            //         "Player {} unlocked skin '{}'.", playerUuid, skinId);
        }
        return added;
    }

    /**
     * Equip a previously-unlocked skin. The default skin is always equippable.
     *
     * @return true if the skin was equipped, false if the player has not
     *         unlocked it or the skin id is unknown
     */
    public boolean equip(UUID playerUuid, String skinId) {
        Optional<BagSkin> skin = plugin.skins().get(skinId);
        if (skin.isEmpty()) return false;

        PlayerHome home = homeStorage.getOrCreate(playerUuid);

        boolean isDefault = SkinRegistry.DEFAULT_SKIN_ID.equals(skinId);
        if (!isDefault && !home.unlockedSkins().contains(skinId)) {
            return false;
        }

        home.setActiveSkinId(skinId);
        homeStorage.save(home);

        // TODO: if the player has a placed bag in the world, re-skin its
        // entity model live, and re-decorate the dimension spawn pad.
        // plugin.dimensions().applyDecoration(playerUuid, skin.get());
        // plugin.placedBags().getByOwner(playerUuid).ifPresent(bag -> ...);

        return true;
    }

    /** Convenience: ensure the default skin is "owned" so it can always be equipped. */
    public void ensureDefaultUnlocked(UUID playerUuid) {
        unlock(playerUuid, SkinRegistry.DEFAULT_SKIN_ID);
    }
}
