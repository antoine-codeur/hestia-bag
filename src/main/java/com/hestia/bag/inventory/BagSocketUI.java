package com.hestia.bag.inventory;

import com.hestia.bag.HestiaBagPlugin;

import java.util.UUID;

/**
 * Builds and opens the in-game UI shown when the owner interacts with their
 * Hestia Socket block (the central altar inside their dimension).
 *
 * <p>Tabs:
 * <ol>
 *   <li><b>Upgrades</b> — list of installed + owned, one-click install/uninstall.</li>
 *   <li><b>Skins</b> — list of unlocked skins with preview, equip button.</li>
 *   <li><b>Permissions</b> — radio for the four permission levels, plus
 *       whitelist and blacklist editors.</li>
 *   <li><b>Knock requests</b> — list of pending guests; accept / deny buttons.</li>
 *   <li><b>Reset</b> — danger zone, double-confirmation; calls
 *       {@link com.hestia.bag.dimension.DimensionResetService#reset(UUID)}.</li>
 * </ol>
 *
 * <p>The whole class is a stub until we know what UI primitives Hytale
 * exposes — official docs hint at a JSON-driven UI system but the API surface
 * for plugins isn't published yet.
 */
public final class BagSocketUI {

    private final HestiaBagPlugin plugin;

    public BagSocketUI(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the UI for {@code viewer}. Only the owner of the socket should be
     * able to open this — guests get a read-only "this is X's home" message
     * (handled at the call site, not here).
     */
    public void openFor(UUID viewer) {
        // ⚠️ TODO — when Hytale UI API is available:
        //   1. Build a tabbed window using the gui builder.
        //   2. For each tab, render the appropriate widgets and wire the
        //      button callbacks to the relevant service methods.
        //   3. Show the window to the player.
        plugin.getLogger().info("[TODO] Open bag socket UI for player {}.", viewer);
    }

    /**
     * Display a small modal asking another player to accept or deny a knock
     * request. Called from {@code PermissionManager} when a knock is queued.
     */
    public void showKnockNotification(UUID owner, UUID requester) {
        // ⚠️ TODO — minimal HUD popup with [Accept] [Deny] buttons.
        plugin.getLogger().info("[TODO] Show knock popup for owner {} from {}.",
                owner, requester);
    }
}
