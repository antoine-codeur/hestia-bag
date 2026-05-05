package com.hestia.bag;

import com.hestia.bag.commands.HestiaAdminCommand;
import com.hestia.bag.commands.HestiaCommand;
import com.hestia.bag.config.HestiaConfig;
import com.hestia.bag.crafting.HestiaBagRecipe;
import com.hestia.bag.dimension.DimensionResetService;
import com.hestia.bag.dimension.HestiaDimensionManager;
import com.hestia.bag.events.BagInteractListener;
import com.hestia.bag.events.BagPickupListener;
import com.hestia.bag.events.BagPlacementListener;
import com.hestia.bag.events.FallVoidListener;
import com.hestia.bag.events.PlayerJoinListener;
import com.hestia.bag.inventory.BagSlotManager;
import com.hestia.bag.items.HestiaItems;
import com.hestia.bag.permissions.PermissionManager;
import com.hestia.bag.skins.SkinRegistry;
import com.hestia.bag.skins.SkinUnlockService;
import com.hestia.bag.storage.PlacedBagRegistry;
import com.hestia.bag.storage.PlayerHomeStorage;
import com.hestia.bag.upgrades.UpgradeRegistry;

/*
 * ⚠️ Hytale API imports — verification required.
 *
 * The two imports below match the API surface documented by Britakee Studios
 * GitBook and hytale-docs.pages.dev as of Hytale Update 3 (Feb 2026).
 * If the server rejects the plugin at load time with a NoClassDefFoundError,
 * decompile HytaleServer.jar (it ships unobfuscated) and locate the real
 * package — likely com.hypixel.hytale.api.plugin or similar.
 */
import com.hypixel.hytale.plugin.JavaPlugin;
import com.hypixel.hytale.plugin.JavaPluginInit;

import javax.annotation.Nonnull;

/**
 * HestiaBag — main plugin class and dependency-injection root.
 *
 * <p>Concept overview:
 * <ul>
 *   <li>Each player can craft a <b>Hestia Bag</b> item.</li>
 *   <li>The bag is equipped in a custom inventory slot, then placed in the world
 *       as a small physical entity that other players can interact with.</li>
 *   <li>Interacting with the bag opens a portal to the owner's private dimension.
 *       Access is gated by a per-bag permission level (PUBLIC / WHITELIST / KNOCK
 *       / PRIVATE) plus a global blacklist.</li>
 *   <li>The dimension is keyed by player UUID — crafting a second bag does NOT
 *       create a second dimension; both bags portal into the same home.</li>
 *   <li>Inside the home, a <b>Hestia Socket</b> block lets the owner install
 *       upgrades (workshops, storage, farms, decorations, teleporters,
 *       expansions) and equip cosmetic skins (full themes affecting both the
 *       bag entity model and the dimension decoration).</li>
 *   <li>Resetting the home is destructive: every container, plant, and entity
 *       inside is dropped at the placed-bag position in the outer world before
 *       the dimension is wiped.</li>
 * </ul>
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link #HestiaBagPlugin(JavaPluginInit)} — constructor, called once on load.</li>
 *   <li>{@link #onEnable()} — services, listeners, commands, recipes are registered.</li>
 *   <li>{@link #onDisable()} — state is flushed to disk, listeners auto-unregister.</li>
 * </ol>
 *
 * <p>This class deliberately holds <b>no static state</b> beyond the singleton
 * instance reference. Hytale supports plugin hot-reload, and static state would
 * leak across reloads.
 */
public final class HestiaBagPlugin extends JavaPlugin {

    /**
     * Single instance reference, populated in the constructor.
     * Public access is via {@link #get()} so it can be replaced cleanly on reload.
     */
    private static HestiaBagPlugin instance;

    // === Service references (initialized in onEnable, never reassigned afterward) ===

    private HestiaConfig          config;
    private PlayerHomeStorage     homeStorage;
    private PlacedBagRegistry     placedBags;
    private PermissionManager     permissions;
    private UpgradeRegistry       upgrades;
    private SkinRegistry          skins;
    private SkinUnlockService     skinUnlocks;
    private HestiaDimensionManager dimensions;
    private DimensionResetService resetService;
    private BagSlotManager        bagSlots;

    /**
     * Constructor invoked by the Hytale plugin loader.
     * <p>We only stash the instance reference here — actual setup happens in
     * {@link #onEnable()}, after the server has finished booting its own services.
     */
    public HestiaBagPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    /** Global accessor used by listeners and commands that don't get the plugin injected. */
    public static HestiaBagPlugin get() {
        return instance;
    }

    @Override
    public void onEnable() {
        getLogger().info("=== HestiaBag: starting ===");

        // 1. Load configuration first — every other service may read from it.
        this.config = HestiaConfig.loadOrDefault(this);

        // 2. Pure in-memory registries (no I/O). Order: catalogs before consumers.
        this.upgrades = new UpgradeRegistry();
        this.upgrades.registerDefaults();

        this.skins = new SkinRegistry();
        this.skins.registerDefaults();

        // 3. Persistent stores. Each has its own JSON-per-file directory; loading
        //    is lazy on first access to keep startup snappy on busy servers.
        this.homeStorage = new PlayerHomeStorage(this);
        this.placedBags  = new PlacedBagRegistry(this);
        this.permissions = new PermissionManager(this, homeStorage);
        this.skinUnlocks = new SkinUnlockService(this, homeStorage);

        // 4. Domain services that compose the registries above.
        this.dimensions   = new HestiaDimensionManager(this);
        this.resetService = new DimensionResetService(this);
        this.bagSlots     = new BagSlotManager(this);

        // 5. Game-content registration (items, blocks, recipes).
        HestiaItems.registerAll(this);
        HestiaBagRecipe.register(this);

        // 6. Wire up event listeners and commands. These hook the plugin into
        //    actual gameplay — once they're registered, the plugin is "live".
        registerListeners();
        registerCommands();

        getLogger().info("HestiaBag ready: {} upgrades, {} skins registered.",
                upgrades.size(), skins.size());
    }

    @Override
    public void onDisable() {
        getLogger().info("=== HestiaBag: shutting down ===");

        // Persistent stores: flush in-memory state to disk before the server
        // process exits. Order matters — placed-bag positions reference player
        // homes, so save homes last (they're what we'd want to recover).
        if (placedBags  != null) placedBags.saveAll();
        if (homeStorage != null) homeStorage.saveAll();

        // Listeners and commands are auto-unregistered by the plugin manager
        // when our class loader is discarded — no manual cleanup required.
    }

    // -----------------------------------------------------------------------
    // Listener / command wiring
    // -----------------------------------------------------------------------

    private void registerListeners() {
        /*
         * Pattern: grab the server EventBus and `register` listener objects.
         * Each listener instance holds a reference back to the plugin so it
         * can look up services without going through the static singleton.
         *
         * ⚠️ TODO — confirm the exact accessor name. Candidates seen in docs:
         *   - getServer().getEventBus()
         *   - getServer().getEvents()
         *   - getEventBus()  (inherited from JavaPlugin)
         */
        var bus = getServer().getEventBus();

        bus.register(new PlayerJoinListener(this));   // Restore custom slot on join.
        bus.register(new BagPlacementListener(this)); // Equipped bag → placed entity.
        bus.register(new BagPickupListener(this));    // Owner picks bag back up.
        bus.register(new BagInteractListener(this));  // Anyone right-clicks a placed bag.
        bus.register(new FallVoidListener(this));     // Caught Y < 0 inside dimension.
    }

    private void registerCommands() {
        // /hestia and /hestia-admin live in separate classes so the admin
        // command can be permission-gated without leaking subcommands.
        new HestiaCommand(this).register();
        new HestiaAdminCommand(this).register();
    }

    // -----------------------------------------------------------------------
    // Public service accessors — preferred over the static singleton inside
    // this codebase. The singleton is for code that can't reach the plugin
    // through normal injection (e.g. recipe predicates, low-level hooks).
    // -----------------------------------------------------------------------

    public HestiaConfig           config()       { return config;       }
    public PlayerHomeStorage      homeStorage()  { return homeStorage;  }
    public PlacedBagRegistry      placedBags()   { return placedBags;   }
    public PermissionManager      permissions()  { return permissions;  }
    public UpgradeRegistry        upgrades()     { return upgrades;     }
    public SkinRegistry           skins()        { return skins;        }
    public SkinUnlockService      skinUnlocks()  { return skinUnlocks;  }
    public HestiaDimensionManager dimensions()   { return dimensions;   }
    public DimensionResetService  resetService() { return resetService; }
    public BagSlotManager         bagSlots()     { return bagSlots;     }
}
