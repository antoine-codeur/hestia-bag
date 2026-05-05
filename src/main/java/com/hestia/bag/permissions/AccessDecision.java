package com.hestia.bag.permissions;

/**
 * Outcome of an access check performed by {@link PermissionManager}.
 *
 * <p>This is what callers should switch on to decide what to do next. The
 * naming is deliberate: callers do not need to know <i>why</i> a player was
 * denied (whitelist? blacklist? owner offline?) — the manager has already
 * decided. The optional {@code reason} string is for human-facing feedback
 * (e.g. "Owner is offline. Try again later.") and does not encode logic.
 *
 * @param outcome  what the caller should do
 * @param reason   short message for the player; null if not applicable
 */
public record AccessDecision(Outcome outcome, String reason) {

    public enum Outcome {
        /** Allow entry immediately. */
        ALLOW,

        /** Reject entry permanently for this attempt. */
        DENY,

        /** Queue a knock request and notify the owner. */
        KNOCK_QUEUED,

        /** A knock request already exists; nothing further to do. */
        KNOCK_PENDING
    }

    public static AccessDecision allow() {
        return new AccessDecision(Outcome.ALLOW, null);
    }

    public static AccessDecision deny(String reason) {
        return new AccessDecision(Outcome.DENY, reason);
    }

    public static AccessDecision knockQueued() {
        return new AccessDecision(Outcome.KNOCK_QUEUED,
                "Knock sent. Waiting for owner...");
    }

    public static AccessDecision knockPending() {
        return new AccessDecision(Outcome.KNOCK_PENDING,
                "You already knocked. Waiting...");
    }
}
