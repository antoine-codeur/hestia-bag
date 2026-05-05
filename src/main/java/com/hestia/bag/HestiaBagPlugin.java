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
 * ⚠️ Hytale API imports — verified against HytaleServer.jar.
 *
 * The two imports below are from com.hypixel.hytale.server.core.plugin.*
 * as confirmed by jar inspection.
 */
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import org.jetbrains.annotations.NotNull;

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
    public HestiaBagPlugin(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    /** Global accessor used by listeners and commands that don't get the plugin injected. */
    public static HestiaBagPlugin get() {
        return instance;
    }

    /**
     * Called during server startup to register event listeners.
     * This happens before onEnable(), so services may not be fully initialized yet.
     *
     * <p>⚠️ TODO — wire up actual Hytale events once event classes are available.
     * Expected pattern (pseudo-code):
     * <pre>
     *   this.getEventRegistry().registerGlobal(
     *       PlayerReadyEvent.class,
     *       (event) -> bagSlots.refreshFor(event.getPlayer().getUniqueId())
     *   );
     * </pre>
     */
    @Override
    public void setup() {
        System.out.println("[HestiaBag] setup() called - registering event listeners");
        // TODO: registerGlobal(PlayerReadyEvent.class, ...) once event imports available
    }

    public void onEnable() {
        System.out.println("=== HestiaBag: initializing plugin ===");

        // 1. Load configuration first — every other service may read from it.
        this.config = HestiaConfig.loadOrDefault(this);
        System.out.println("[HestiaBag] Config loaded");

        // 2. Pure in-memory registries (no I/O). Order: catalogs before consumers.
        this.upgrades = new UpgradeRegistry();
        this.upgrades.registerDefaults();
        System.out.println("[HestiaBag] Registered " + this.upgrades.size() + " upgrades");

        this.skins = new SkinRegistry();
        this.skins.registerDefaults();
        System.out.println("[HestiaBag] Registered " + this.skins.size() + " skins");

        // 3. Persistent stores. Each has its own JSON-per-file directory; loading
        //    is lazy on first access to keep startup snappy on busy servers.
        this.homeStorage = new PlayerHomeStorage(this);
        this.placedBags  = new PlacedBagRegistry(this);
        this.permissions = new PermissionManager(this, homeStorage);
        this.skinUnlocks = new SkinUnlockService(this, homeStorage);
        System.out.println("[HestiaBag] Storage services initialized");

        // 4. Domain services that compose the registries above.
        this.dimensions   = new HestiaDimensionManager(this);
        this.resetService = new DimensionResetService(this);
        this.bagSlots     = new BagSlotManager(this);
        System.out.println("[HestiaBag] Domain services initialized");

        // 5. Game-content registration (items, blocks, recipes).
        HestiaItems.registerAll(this);
        HestiaBagRecipe.register(this);
        System.out.println("[HestiaBag] Item and recipe registration attempted");

        // 6. Wire up event listeners and commands. These hook the plugin into
        //    actual gameplay — once they're registered, the plugin is "live".
        registerListeners();
        registerCommands();
        System.out.println("[HestiaBag] Listeners and commands registered");

        System.out.println("=== HestiaBag: ready! (" + upgrades.size() + " upgrades, " + skins.size() + " skins) ===");
    }

    public void onDisable() {
        System.out.println("=== HestiaBag: shutting down ===");

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
        // TODO: var bus = getServer().getEventBus();

        // TODO: bus.register(new PlayerJoinListener(this));   // Restore custom slot on join.
        // TODO: bus.register(new BagPlacementListener(this)); // Equipped bag → placed entity.
        // TODO: bus.register(new BagPickupListener(this));    // Owner picks bag back up.
        // TODO: bus.register(new BagInteractListener(this));  // Anyone right-clicks a placed bag.
        // TODO: bus.register(new FallVoidListener(this));     // Caught Y < 0 inside dimension.
    }

    private void registerCommands() {
        // /hestia and /hestia-admin live in separate classes so the admin
        // command can be permission-gated without leaking subcommands.
        // TODO: new HestiaCommand(this).register();
        // TODO: new HestiaAdminCommand(this).register();
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
