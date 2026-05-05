package com.hestia.bag.dimension;

import com.hestia.bag.HestiaBagPlugin;
import com.hestia.bag.permissions.AccessDecision;
import com.hestia.bag.storage.PlayerHome;

import java.util.UUID;

/**
 * Manages every player's pocket dimension.
 *
 * <p><b>Architecture:</b> instead of creating one Hytale World per player
 * (memory-prohibitive at scale), we use a single shared world named
 * {@code hestia_void} and allocate each player a slot inside it. Slot
 * coordinates are derived deterministically from the player's UUID and spaced
 * far enough apart that no expansion can ever cause two homes to overlap.
 *
 * <p>This pattern is borrowed from Bukkit's {@code PocketDimensions} plugin
 * and works well up to ~100k unique players per server. Beyond that we'd need
 * a sharded world strategy.
 *
 * <p>Y-coordinates:
 * <ul>
 *   <li>Spawn pad surface: Y={@link #HESTIA_SPAWN_Y}</li>
 *   <li>Floor blocks one below: Y={@link #HESTIA_SPAWN_Y} - 1</li>
 *   <li>Void-fall trigger:    Y &lt; {@link #VOID_FALL_Y}</li>
 * </ul>
 */
public final class HestiaDimensionManager {

    /** Name of the shared world that hosts every player's pocket dimension. */
    public static final String HESTIA_WORLD_NAME = "hestia_void";

    /**
     * Distance in blocks between two adjacent player slots. Set high enough to
     * accommodate the maximum allowed expansion ({@code maxActiveChunks * 16})
     * with a comfortable margin. 1024 fits 64 chunks plus generous padding.
     */
    private static final int PLAYER_SLOT_STRIDE = 1024;

    /** Y of the spawn pad surface — high enough to leave room below for void-fall. */
    private static final int HESTIA_SPAWN_Y = 64;

    /** Players falling below this Y are teleported back to their spawn pad. */
    public static final int VOID_FALL_Y = 0;

    private final HestiaBagPlugin plugin;

    public HestiaDimensionManager(HestiaBagPlugin plugin) {
        this.plugin = plugin;
        ensureHestiaWorldExists();
    }

    // -----------------------------------------------------------------------
    // World setup
    // -----------------------------------------------------------------------

    /**
     * Create the {@code hestia_void} world if it doesn't exist yet.
     *
     * <p>The world uses a void chunk generator so unallocated slots stay empty
     * — this is what makes "fall into the void" a usable game mechanic.
     */
    private void ensureHestiaWorldExists() {
        // ⚠️ TODO — real world creation API:
        //   if (plugin.getServer().getWorld(HESTIA_WORLD_NAME) == null) {
        //       WorldCreator wc = new WorldCreator(HESTIA_WORLD_NAME)
        //           .generator(new VoidChunkGenerator())
        //           .seed(0)
        //           .environment(Environment.NORMAL);
        //       plugin.getServer().createWorld(wc);
        //   }
        // TODO: plugin.getLogger().info(
        //         "[TODO] Create world '{}' with VoidChunkGenerator.", HESTIA_WORLD_NAME);
    }

    // -----------------------------------------------------------------------
    // Slot allocation
    // -----------------------------------------------------------------------

    /**
     * Compute the spawn coordinates of a player's home, deterministically from
     * their UUID. Same UUID always returns the same slot — that's why we can
     * skip per-player slot persistence.
     */
    public DimensionSlot slotFor(UUID playerUuid) {
        // Mix high and low halves of the UUID to spread players across the slot grid.
        long h = playerUuid.getMostSignificantBits()
                ^ playerUuid.getLeastSignificantBits();

        // % into a 100k x 100k grid, then multiply by stride to space slots out.
        // Mask with 0xFFFFFFFFL to force unsigned interpretation before modulo.
        int slotX = (int) ((h & 0xFFFFFFFFL)        % 100_000) * PLAYER_SLOT_STRIDE;
        int slotZ = (int) (((h >>> 32) & 0xFFFFFFFFL) % 100_000) * PLAYER_SLOT_STRIDE;

        return new DimensionSlot(slotX, HESTIA_SPAWN_Y, slotZ);
    }

    // -----------------------------------------------------------------------
    // Entry / exit
    // -----------------------------------------------------------------------

    /**
     * Entry point called by {@link com.hestia.bag.events.BagInteractListener}
     * when a guest interacts with a placed bag. Checks permissions before
     * teleporting.
     *
     * @param guestUuid who is trying to enter
     * @param ownerUuid whose home they're trying to enter
     * @return the access decision; ALLOW means we did teleport
     */
    public AccessDecision tryEnter(UUID guestUuid, UUID ownerUuid) {
        AccessDecision decision = plugin.permissions().check(guestUuid, ownerUuid);
        if (decision.outcome() == AccessDecision.Outcome.ALLOW) {
            performTeleportToHome(guestUuid, ownerUuid);
        }
        return decision;
    }

    /** Unconditional teleport (after a knock has been accepted, or for the owner). */
    public void performTeleportToHome(UUID playerUuid, UUID homeOwnerUuid) {
        PlayerHome home = plugin.homeStorage().getOrCreate(homeOwnerUuid);
        DimensionSlot slot = slotFor(homeOwnerUuid);

        // Snapshot the entering player's outer-world position so /hestia exit knows
        // where to send them back. We track this against the home being entered, but
        // a multi-guest implementation would key it by guest-uuid instead.
        // ⚠️ TODO: WorldPosition currentPos = capturePosition(playerUuid);
        //          home.setLastExitPosition(currentPos);  // owner only
        //          // for guests, store in a per-guest map

        ensurePlatform(home, slot);
        // ⚠️ TODO: actual teleport API:
        //   player.teleport(new Location(world, slot.x()+0.5, slot.y(), slot.z()+0.5));
        // TODO: plugin.getLogger().info("[TODO] Teleport {} to home of {} at {}",
        //         playerUuid, homeOwnerUuid, slot);
    }

    /**
     * Send a player out of any home dimension back to their last known outer-world
     * position. Used by {@code /hestia exit}, the void-fall handler (rare edge
     * case where someone falls into a non-allocated slot), and the kick-on-blacklist
     * flow.
     */
    public void exitHome(UUID playerUuid) {
        PlayerHome home = plugin.homeStorage().getOrCreate(playerUuid);
        var pos = home.lastExitPosition();
        if (pos == null) {
            // Fallback: send to the default world spawn. Better than leaving them stuck.
            // TODO: plugin.getLogger().info(
            //         "[TODO] No exit position recorded for {}; falling back to default-world spawn.",
            //         playerUuid);
            return;
        }
        // TODO: plugin.getLogger().info("[TODO] Teleport {} back to {}", playerUuid, pos);
    }

    // -----------------------------------------------------------------------
    // Platform construction
    // -----------------------------------------------------------------------

    /**
     * Lay down (or extend) the floor blocks for a home. Called on first entry
     * and after every upgrade install that flips {@code expandsArea}.
     *
     * <p>The platform is a single-Y layer of solid blocks at {@code slot.y - 1},
     * sized {@code activeChunks * 16} on each side. Extension is idempotent —
     * we always re-place the full floor rather than tracking which chunks
     * already exist. Cheap enough at our sizes.
     */
    public void ensurePlatform(PlayerHome home, DimensionSlot slot) {
        int size = home.activeChunks() * 16;
        // ⚠️ TODO — real block-placement API:
        //   World world = plugin.getServer().getWorld(HESTIA_WORLD_NAME);
        //   for (int dx = 0; dx < size; dx++) {
        //       for (int dz = 0; dz < size; dz++) {
        //           world.setBlock(slot.x() + dx, slot.y() - 1, slot.z() + dz,
        //                          BlockTypes.STONE_BRICKS);
        //       }
        //   }
        // TODO: plugin.getLogger().info("[TODO] Ensure {}x{} platform at {} for owner {}",
        //         size, size, slot, home.ownerUuid());
    }

    // -----------------------------------------------------------------------
    // Void fall
    // -----------------------------------------------------------------------

    /**
     * Bounce a player who fell off their platform back to the spawn pad.
     */
    public void respawnFromVoid(UUID playerUuid) {
        DimensionSlot slot = slotFor(playerUuid);
        // TODO: plugin.getLogger().info("[TODO] Player {} fell into the void → respawning at {}",
        //         playerUuid, slot);
        // ⚠️ TODO: teleport to the spawn pad of the dimension they were inside.
        // NOTE: a guest visiting another player's home falls in *that* player's
        // void — they should respawn at the host's spawn pad, not their own.
        // Track which home each player is currently inside.
    }

    /** Coordinates of an allocated dimension slot. */
    public record DimensionSlot(int x, int y, int z) {}
}
