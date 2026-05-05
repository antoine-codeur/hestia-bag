package com.hestia.bag.storage;

import com.hestia.bag.HestiaBagPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persistent storage for {@link PlayerHome} instances.
 *
 * <p>Strategy: <b>one JSON file per player</b> in
 * {@code <data-folder>/homes/<uuid>.json}. Trade-offs:
 * <ul>
 *   <li>+ Bounded write size: changing one player's state writes only their file.</li>
 *   <li>+ Easy manual repair: admins can edit/delete a single player's home.</li>
 *   <li>− Doesn't scale past a few thousand players (filesystem inode pressure).
 *       Migrate to SQLite if HestiaBag ever ships on large public servers.</li>
 * </ul>
 *
 * <p>Loading is <b>lazy</b>: we only hit disk when a UUID is first requested,
 * not at startup. This keeps server boot fast even when thousands of player
 * files exist.
 */
public final class PlayerHomeStorage {

    private final HestiaBagPlugin plugin;
    private final Path dataDir;

    // Hot cache: any home that has been touched since startup lives here.
    // ConcurrentHashMap because event handlers may run on different threads.
    private final Map<UUID, PlayerHome> cache = new ConcurrentHashMap<>();

    public PlayerHomeStorage(HestiaBagPlugin plugin) {
        this.plugin = plugin;

        // ⚠️ TODO — replace hardcoded path with the real plugin data folder API.
        //   Likely: plugin.getDataFolder().toPath().resolve("homes")
        //   The path "plugins/HestiaBag/homes" is the conventional default and
        //   matches what most Hytale plugin examples use.
        this.dataDir = Path.of("plugins", "HestiaBag", "homes");

        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to create homes data directory at {}", dataDir, e);
        }
    }

    /**
     * Get-or-create a home, hitting disk on first access for a given UUID.
     *
     * <p>If the JSON file fails to parse (corrupt, hand-edited badly), we log
     * the error and return a fresh home. This is intentionally lossy: a corrupt
     * home is better replaced than left in a half-broken state, and the player
     * gets a clean slate rather than a server crash.
     */
    public PlayerHome getOrCreate(UUID playerUuid) {
        return cache.computeIfAbsent(playerUuid, uuid -> {
            Path file = fileFor(uuid);
            if (Files.exists(file)) {
                try {
                    String json = Files.readString(file);
                    return HomeJsonCodec.deserialize(uuid, json);
                } catch (IOException | RuntimeException e) {
                    plugin.getLogger().error(
                            "Failed to load home for {}; regenerating fresh.", uuid, e);
                }
            }
            return new PlayerHome(uuid);
        });
    }

    /**
     * Save one home synchronously. Called by mutation-heavy operations
     * (install upgrade, change permission mode) to avoid losing state on crash.
     */
    public void save(PlayerHome home) {
        Path file = fileFor(home.ownerUuid());
        try {
            String json = HomeJsonCodec.serialize(home);
            // Write to temp file then atomic-rename to avoid half-written files
            // if the server crashes during the write.
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.writeString(tmp, json);
            Files.move(tmp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to save home for {}", home.ownerUuid(), e);
        }
    }

    /** Bulk save called at server shutdown. */
    public void saveAll() {
        // Snapshot the cache to avoid ConcurrentModificationException if a save
        // operation triggers a cache eviction mid-iteration.
        Map<UUID, PlayerHome> snapshot = new HashMap<>(cache);
        for (PlayerHome home : snapshot.values()) {
            save(home);
        }
        plugin.getLogger().info("PlayerHomeStorage: saved {} homes.", snapshot.size());
    }

    private Path fileFor(UUID uuid) {
        return dataDir.resolve(uuid + ".json");
    }
}
