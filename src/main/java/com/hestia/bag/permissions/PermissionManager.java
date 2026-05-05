package com.hestia.bag.permissions;

import com.hestia.bag.HestiaBagPlugin;
import com.hestia.bag.storage.PlayerHome;
import com.hestia.bag.storage.PlayerHomeStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centralized access control for all Hestia Bags.
 *
 * <p>The manager has three responsibilities:
 * <ol>
 *   <li><b>Decision</b>: given a guest UUID and a bag-owner UUID, return an
 *       {@link AccessDecision} telling the caller whether to allow, deny, or
 *       queue a knock.</li>
 *   <li><b>Knock state</b>: track pending knock requests with TTL-based
 *       expiration so abandoned knocks don't pile up.</li>
 *   <li><b>List management</b>: thin wrappers around {@link PlayerHome}'s
 *       whitelist and blacklist sets. The home itself owns the data; the
 *       manager owns the rules.</li>
 * </ol>
 *
 * <p>Decision precedence (top wins):
 * <ol>
 *   <li>Owner accessing their own home → ALLOW.</li>
 *   <li>Guest is on the blacklist → DENY (regardless of mode).</li>
 *   <li>Owner is offline → DENY (per design spec: "blocked when owner offline").</li>
 *   <li>Mode-specific evaluation (PUBLIC / WHITELIST / KNOCK / PRIVATE).</li>
 * </ol>
 */
public final class PermissionManager {

    private final HestiaBagPlugin plugin;
    private final PlayerHomeStorage homeStorage;

    /**
     * Pending knocks, keyed by (ownerUuid → (requesterUuid → request)).
     * Two-level map gives O(1) lookup of a specific knock and O(n) listing of
     * all knocks for one owner. ConcurrentHashMap because event handlers may
     * fire on different threads.
     */
    private final Map<UUID, Map<UUID, KnockRequest>> pendingKnocks = new ConcurrentHashMap<>();

    public PermissionManager(HestiaBagPlugin plugin, PlayerHomeStorage homeStorage) {
        this.plugin = plugin;
        this.homeStorage = homeStorage;
    }

    // -----------------------------------------------------------------------
    // Decision
    // -----------------------------------------------------------------------

    /**
     * Evaluate whether {@code guestUuid} is allowed to enter the home owned by
     * {@code ownerUuid}. This is the only decision API callers should use —
     * the rest of the manager is implementation detail.
     */
    public AccessDecision check(UUID guestUuid, UUID ownerUuid) {
        // Rule 1: owner accessing their own home — always allow.
        if (guestUuid.equals(ownerUuid)) {
            return AccessDecision.allow();
        }

        PlayerHome home = homeStorage.getOrCreate(ownerUuid);

        // Rule 2: blacklist always wins.
        if (home.blacklist().contains(guestUuid)) {
            return AccessDecision.deny("You are not welcome here.");
        }

        // Rule 3: owner must be online for any guest access.
        if (!isOwnerOnline(ownerUuid)) {
            return AccessDecision.deny("Owner is offline.");
        }

        // Rule 4: mode-based evaluation.
        return evaluateMode(home, guestUuid, ownerUuid);
    }

    private AccessDecision evaluateMode(PlayerHome home, UUID guest, UUID owner) {
        BagPermissionLevel mode = home.permissionLevel();
        return switch (mode) {
            case PUBLIC    -> AccessDecision.allow();

            case WHITELIST -> home.whitelist().contains(guest)
                    ? AccessDecision.allow()
                    : AccessDecision.deny("This home is invitation-only.");

            case KNOCK     -> handleKnock(guest, owner);

            case PRIVATE   -> AccessDecision.deny("This home is private.");
        };
    }

    /** Either queue a new knock or report that one is already pending. */
    private AccessDecision handleKnock(UUID guest, UUID owner) {
        var ownerKnocks = pendingKnocks.computeIfAbsent(owner, k -> new ConcurrentHashMap<>());

        // Drop expired knocks under this owner before checking — keeps memory bounded
        // and lets the same guest re-knock after their previous attempt timed out.
        ownerKnocks.values().removeIf(KnockRequest::isExpired);

        if (ownerKnocks.containsKey(guest)) {
            return AccessDecision.knockPending();
        }

        long ttl = plugin.config().knockRequestTtlSeconds;
        ownerKnocks.put(guest, KnockRequest.forNow(guest, owner, ttl));

        // TODO: send the owner an in-game notification with [Accept] [Deny] buttons.
        // The notification system will likely be:
        //   plugin.getServer().getPlayer(owner).sendMessage(...);
        //   plugin.getServer().getPlayer(owner).showActionBar(...);
        plugin.getLogger().info("[TODO] Notify owner {} about knock from {}.", owner, guest);

        return AccessDecision.knockQueued();
    }

    // -----------------------------------------------------------------------
    // Knock lifecycle (called from owner's accept/deny UI)
    // -----------------------------------------------------------------------

    /**
     * Owner accepts a pending knock. If the knock is still valid, returns the
     * request so the caller can teleport the guest in.
     */
    public KnockRequest acceptKnock(UUID owner, UUID requester) {
        var ownerKnocks = pendingKnocks.get(owner);
        if (ownerKnocks == null) return null;

        KnockRequest req = ownerKnocks.remove(requester);
        if (req == null || req.isExpired()) return null;
        return req;
    }

    /** Owner denies a pending knock. Idempotent: silently no-ops if not found. */
    public void denyKnock(UUID owner, UUID requester) {
        var ownerKnocks = pendingKnocks.get(owner);
        if (ownerKnocks != null) {
            ownerKnocks.remove(requester);
        }
    }

    /** Snapshot of all pending knocks for an owner — used by the bag UI. */
    public Map<UUID, KnockRequest> snapshotKnocks(UUID owner) {
        var ownerKnocks = pendingKnocks.get(owner);
        if (ownerKnocks == null) return Map.of();
        // Snapshot copy so callers can iterate without ConcurrentModificationException.
        return new HashMap<>(ownerKnocks);
    }

    // -----------------------------------------------------------------------
    // Mode + list mutators (delegate to PlayerHome for persistence)
    // -----------------------------------------------------------------------

    public void setLevel(UUID owner, BagPermissionLevel level) {
        homeStorage.getOrCreate(owner).setPermissionLevel(level);
    }

    public void addToWhitelist(UUID owner, UUID guest) {
        homeStorage.getOrCreate(owner).whitelist().add(guest);
        // Adding to whitelist should remove from blacklist — they are mutually exclusive.
        homeStorage.getOrCreate(owner).blacklist().remove(guest);
    }

    public void removeFromWhitelist(UUID owner, UUID guest) {
        homeStorage.getOrCreate(owner).whitelist().remove(guest);
    }

    public void addToBlacklist(UUID owner, UUID guest) {
        homeStorage.getOrCreate(owner).blacklist().add(guest);
        homeStorage.getOrCreate(owner).whitelist().remove(guest);
        // Kick the guest out if they are currently inside the home.
        // TODO: plugin.dimensions().kickIfInside(owner, guest);
    }

    public void removeFromBlacklist(UUID owner, UUID guest) {
        homeStorage.getOrCreate(owner).blacklist().remove(guest);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * True if the owning player is currently connected to the server.
     *
     * <p>⚠️ TODO — replace with the real Hytale presence API. Likely candidates:
     * <ul>
     *   <li>{@code plugin.getServer().getPlayer(uuid) != null}</li>
     *   <li>{@code plugin.getServer().getOnlinePlayers().stream().anyMatch(...)}</li>
     * </ul>
     */
    private boolean isOwnerOnline(UUID ownerUuid) {
        // Default to "online" until the API is wired so the plugin remains functional
        // for testing. Once we know the real call, flip the default to false-on-error.
        return true;
    }
}
