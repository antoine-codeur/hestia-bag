package com.hestia.bag.commands;

import com.hestia.bag.HestiaBagPlugin;

/**
 * The main player-facing command, {@code /hestia}.
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code /hestia}            — toggle entry/exit of own home (only works if
 *                                    the player has a placed bag, otherwise an
 *                                    error message points them at the recipe).</li>
 *   <li>{@code /hestia exit}       — explicit exit, unambiguous when toggling
 *                                    is undesirable (e.g. inside someone else's home).</li>
 *   <li>{@code /hestia upgrades}   — list owned + installed upgrades.</li>
 *   <li>{@code /hestia skins}      — list unlocked skins; {@code /hestia skins <id>}
 *                                    equips the named skin.</li>
 *   <li>{@code /hestia perm <mode>}        — set permission mode (PUBLIC / WHITELIST
 *                                    / KNOCK / PRIVATE).</li>
 *   <li>{@code /hestia invite <player>}    — add to whitelist.</li>
 *   <li>{@code /hestia uninvite <player>}  — remove from whitelist.</li>
 *   <li>{@code /hestia ban <player>}       — add to blacklist.</li>
 *   <li>{@code /hestia unban <player>}     — remove from blacklist.</li>
 *   <li>{@code /hestia knocks}             — list pending knock requests, accept/deny.</li>
 * </ul>
 *
 * <p>Reset is intentionally <i>not</i> a chat command — it's only available
 * through the in-game socket UI with a confirmation modal, to make it
 * impossible to "fat-finger" away your home from a chat box.
 */
public final class HestiaCommand {

    private final HestiaBagPlugin plugin;

    public HestiaCommand(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        /*
         * ⚠️ TODO — adapt to the real Hytale command framework. Likely shape:
         *
         * CommandManager.get().register(
         *     Command.builder("hestia")
         *         .description("Hestia Bag — your private dimension")
         *         .executor(this::execute)
         *         .subCommand("exit",      this::exit)
         *         .subCommand("upgrades",  this::listUpgrades)
         *         .subCommand("skins",     this::handleSkins)
         *         .subCommand("perm",      this::setPermission)
         *         .subCommand("invite",    this::invite)
         *         .subCommand("uninvite",  this::uninvite)
         *         .subCommand("ban",       this::ban)
         *         .subCommand("unban",     this::unban)
         *         .subCommand("knocks",    this::listKnocks)
         *         .build()
         * );
         */
        plugin.getLogger().info("[TODO] Register /hestia command.");
    }

    // -----------------------------------------------------------------------
    // Handlers — placeholders. Each takes the framework's command context
    // (player, args, etc.) once we know what that type is.
    // -----------------------------------------------------------------------

    /** Default execution: toggle in/out of own home, or report error. */
    public void execute(/* CommandContext ctx */) {
        // if (no placed bag) → "Craft and place a Hestia Bag first."
        // else if (inside any home) → exitHome()
        // else → tryEnter(self, self)
    }

    public void exit(/* CommandContext ctx */) {
        // plugin.dimensions().exitHome(ctx.actor());
    }

    public void listUpgrades(/* CommandContext ctx */) {
        // Render two lines: "Installed: ..." and "Owned: ..."
    }

    public void handleSkins(/* CommandContext ctx */) {
        // 0 args → list unlocked
        // 1 arg  → equip <id>
    }

    public void setPermission(/* CommandContext ctx */) {
        // plugin.permissions().setLevel(actor, BagPermissionLevel.valueOf(arg));
    }

    public void invite(/* CommandContext ctx */)   { /* permissions().addToWhitelist */ }
    public void uninvite(/* CommandContext ctx */) { /* permissions().removeFromWhitelist */ }
    public void ban(/* CommandContext ctx */)      { /* permissions().addToBlacklist */ }
    public void unban(/* CommandContext ctx */)    { /* permissions().removeFromBlacklist */ }

    public void listKnocks(/* CommandContext ctx */) {
        // Render pending knocks with [Accept] [Deny] suggestions.
    }
}
