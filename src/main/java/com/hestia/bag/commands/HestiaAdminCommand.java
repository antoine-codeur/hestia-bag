package com.hestia.bag.commands;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Operator-only command, {@code /hestia-admin}.
 *
 * <p>Lives in its own class so the permission gate is enforced on the whole
 * subtree — no risk of accidentally exposing a debug subcommand to regular
 * players if {@link HestiaCommand} grows.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code /hestia-admin reset <player>}        — force-reset a player's home.</li>
 *   <li>{@code /hestia-admin grant-skin <player> <skin>} — admin gift skin.</li>
 *   <li>{@code /hestia-admin grant-upgrade <player> <upgrade>} — admin gift upgrade.</li>
 *   <li>{@code /hestia-admin tp <player>}           — teleport into a player's home
 *                                                     ignoring permissions.</li>
 *   <li>{@code /hestia-admin info <player>}         — dump a player's home state.</li>
 * </ul>
 */
public final class HestiaAdminCommand {

    private final HestiaBagPlugin plugin;

    public HestiaAdminCommand(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        /*
         * ⚠️ TODO — register with a permission predicate that limits this to ops:
         *
         * CommandManager.get().register(
         *     Command.builder("hestia-admin")
         *         .requiresPermission("hestia.admin")
         *         .subCommand("reset",         this::reset)
         *         .subCommand("grant-skin",    this::grantSkin)
         *         .subCommand("grant-upgrade", this::grantUpgrade)
         *         .subCommand("tp",            this::adminTp)
         *         .subCommand("info",          this::info)
         *         .build()
         * );
         */
        // TODO: plugin.getLogger().info("[TODO] Register /hestia-admin command.");
    }

    public void reset(/* CommandContext ctx */) {
        // plugin.resetService().reset(targetPlayerUuid);
    }

    public void grantSkin(/* CommandContext ctx */) {
        // plugin.skinUnlocks().unlock(targetUuid, skinId);
    }

    public void grantUpgrade(/* CommandContext ctx */) {
        // plugin.homeStorage().getOrCreate(targetUuid).ownedUpgrades().add(upgradeId);
    }

    public void adminTp(/* CommandContext ctx */) {
        // plugin.dimensions().performTeleportToHome(adminUuid, targetUuid);
    }

    public void info(/* CommandContext ctx */) {
        // Dump the home state to chat — useful for support tickets.
    }
}
