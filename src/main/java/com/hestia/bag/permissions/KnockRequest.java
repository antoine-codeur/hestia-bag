package com.hestia.bag.permissions;

import java.time.Instant;
import java.util.UUID;

/**
 * A pending entry request from a guest to the owner of a Hestia Bag.
 *
 * <p>Created when a guest interacts with a bag whose permission level is
 * {@link BagPermissionLevel#KNOCK}. Lives in {@link PermissionManager}'s
 * pending-request map until the owner accepts/denies it or it expires.
 *
 * <p>Records are immutable — to "update" a request (e.g. extend its TTL) you
 * replace it with a new instance.
 *
 * @param requesterUuid the player asking to enter
 * @param ownerUuid     the player who owns the home being knocked on
 * @param requestedAt   server-time the knock was registered
 * @param expiresAt     server-time after which the request is auto-denied
 */
public record KnockRequest(
        UUID requesterUuid,
        UUID ownerUuid,
        Instant requestedAt,
        Instant expiresAt
) {

    /** True if {@code expiresAt} is in the past. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /** Convenience factory: knock now, expire in {@code ttlSeconds}. */
    public static KnockRequest forNow(UUID requester, UUID owner, long ttlSeconds) {
        Instant now = Instant.now();
        return new KnockRequest(requester, owner, now, now.plusSeconds(ttlSeconds));
    }
}
