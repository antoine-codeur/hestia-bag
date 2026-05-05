package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;

/**
 * Écoute les interactions sur le bloc "Hestia Socket" — le socle central
 * du havre-sac où on installe/retire les upgrades.
 *
 * <p>Comportement attendu :</p>
 * <ul>
 *   <li>Clic-droit sans item → ouvre l'UI du socle (liste des upgrades possédées).</li>
 *   <li>Clic-droit avec un Module d'upgrade → installe directement.</li>
 * </ul>
 */
public final class SocketInteractListener {

    private final HestiaBagPlugin plugin;

    public SocketInteractListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO :
     *
     * @EventHandler
     * public void onBlockInteract(PlayerBlockInteractEvent e) {
     *     if (!HestiaItems.isHestiaSocket(e.getBlock())) return;
     *     // ... ouvrir l'UI ou installer l'item tenu
     * }
     */
}
