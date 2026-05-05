package com.hestia.bag.permissions;

/**
 * Access modes for a player's Hestia Bag.
 *
 * <p>The owner picks one mode at a time through the bag socket UI. The mode
 * controls the default behavior for non-owner players who interact with the
 * placed bag. <b>The blacklist (managed separately by {@link PermissionManager})
 * always overrides the mode</b> — a blacklisted player is denied access even
 * if the mode is PUBLIC.
 *
 * <p>The owner is unaffected by the mode and can always enter their own home.
 */
public enum BagPermissionLevel {

    /**
     * Anyone can enter the dimension by interacting with the placed bag.
     * Useful for community hubs, shops, public showcases.
     */
    PUBLIC,

    /**
     * Only players on the bag's whitelist can enter directly.
     * Non-whitelisted players see a "private" message and cannot interact.
     */
    WHITELIST,

    /**
     * Anyone can <i>request</i> entry, but the request is queued until the
     * owner accepts it. The owner receives a notification with accept/deny
     * actions; the requester sees a "knocking..." indicator.
     * Pending requests expire after a configurable timeout.
     */
    KNOCK,

    /**
     * Only the owner can enter. The bag entity still exists in the world
     * (other players can see it), but interaction is rejected.
     */
    PRIVATE
}
