package com.hestia.bag.storage;

import com.hestia.bag.permissions.BagPermissionLevel;

import java.util.UUID;

/**
 * Minimal hand-rolled JSON codec for {@link PlayerHome}.
 *
 * <p>Why hand-rolled?
 * <ol>
 *   <li>Zero runtime dependencies — keeps the shaded JAR small during bootstrap.</li>
 *   <li>The schema is small and stable enough that the maintenance cost is low.</li>
 * </ol>
 *
 * <p>This codec is a stop-gap. The intended long-term solution is Hytale's
 * built-in <i>Codec</i> system (see Britakee Studios docs § "Custom Config Files"),
 * which gives us schema validation, defaulting, and migration for free.
 *
 * <p>The format is intentionally permissive on read (unknown fields ignored)
 * and explicit on write (all fields always emitted), so future schema bumps
 * don't break old saves.
 */
public final class HomeJsonCodec {

    private HomeJsonCodec() {}

    // -----------------------------------------------------------------------
    // Serialization
    // -----------------------------------------------------------------------

    public static String serialize(PlayerHome home) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("{\n");
        appendField(sb, "schemaVersion", "1", false);                              sb.append(",\n");
        appendField(sb, "ownerUuid", home.ownerUuid().toString(), true);            sb.append(",\n");
        appendField(sb, "activeChunks", String.valueOf(home.activeChunks()), false); sb.append(",\n");
        appendField(sb, "permissionLevel", home.permissionLevel().name(), true);   sb.append(",\n");
        appendField(sb, "activeSkinId",
                home.activeSkinId() == null ? "null" : "\"" + home.activeSkinId() + "\"",
                false);                                                            sb.append(",\n");

        appendArray(sb, "installed",     home.installedUpgrades());                sb.append(",\n");
        appendArray(sb, "owned",         home.ownedUpgrades());                    sb.append(",\n");
        appendArray(sb, "unlockedSkins", home.unlockedSkins());                    sb.append(",\n");
        appendUuidArray(sb, "whitelist", home.whitelist());                        sb.append(",\n");
        appendUuidArray(sb, "blacklist", home.blacklist());                        sb.append(",\n");

        sb.append("  \"lastExit\": ").append(serializePos(home.lastExitPosition())).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static void appendField(StringBuilder sb, String name, String value, boolean quoted) {
        sb.append("  \"").append(name).append("\": ");
        if (quoted) sb.append("\"").append(escape(value)).append("\"");
        else        sb.append(value);
    }

    private static void appendArray(StringBuilder sb, String name, Iterable<String> items) {
        sb.append("  \"").append(name).append("\": [");
        boolean first = true;
        for (String s : items) {
            if (!first) sb.append(", ");
            sb.append("\"").append(escape(s)).append("\"");
            first = false;
        }
        sb.append("]");
    }

    private static void appendUuidArray(StringBuilder sb, String name, Iterable<UUID> items) {
        sb.append("  \"").append(name).append("\": [");
        boolean first = true;
        for (UUID u : items) {
            if (!first) sb.append(", ");
            sb.append("\"").append(u).append("\"");
            first = false;
        }
        sb.append("]");
    }

    private static String serializePos(PlayerHome.WorldPosition pos) {
        if (pos == null) return "null";
        return "{ \"world\": \"" + escape(pos.worldName()) + "\""
                + ", \"x\": " + pos.x()
                + ", \"y\": " + pos.y()
                + ", \"z\": " + pos.z()
                + ", \"yaw\": " + pos.yaw()
                + ", \"pitch\": " + pos.pitch()
                + " }";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    // -----------------------------------------------------------------------
    // Deserialization
    // -----------------------------------------------------------------------

    /**
     * Deserialize a {@link PlayerHome} from its JSON representation.
     *
     * <p>This is intentionally a stub. Writing a robust JSON parser by hand is
     * a foot-gun and we have no JSON dependency yet. Hytale's Codec system —
     * once wired in — will replace this with a typed, validated round-trip.
     *
     * <p>Until then: any home that was serialized to disk during a previous
     * run will be replaced by a fresh home on next access. Players will lose
     * their state across restarts if we ship like this.
     *
     * @throws RuntimeException always, until a real parser is wired.
     */
    public static PlayerHome deserialize(UUID uuid, String json) {
        // ⚠️ TODO — implement using Hytale's Codec system.
        // Until then, throw so PlayerHomeStorage's catch-all logs it and falls
        // back to a fresh home. Don't silently return new PlayerHome() — we want
        // the log to be loud during development.
        throw new UnsupportedOperationException(
                "JSON deserialization not yet implemented; wire Hytale Codec system");
    }

    // -----------------------------------------------------------------------
    // Convenience for nullable enum fields
    // -----------------------------------------------------------------------

    /** Best-effort parse of a permission level from a string; defaults to PRIVATE. */
    public static BagPermissionLevel parseLevel(String raw) {
        if (raw == null) return BagPermissionLevel.PRIVATE;
        try {
            return BagPermissionLevel.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return BagPermissionLevel.PRIVATE;
        }
    }
}
