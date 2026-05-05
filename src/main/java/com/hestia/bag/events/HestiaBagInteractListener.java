package com.hestia.bag.events;

import com.hestia.bag.HestiaBagPlugin;
import com.hestia.bag.dimension.HestiaDimensionManager;
import com.hestia.bag.items.HestiaItems;

/**
 * Écoute les interactions du joueur avec l'item "Hestia Bag" :
 * clic-droit (ou interaction "F") → entrer/sortir de la dimension.
 *
 * <p>Pourquoi pas un keybind H direct ? Cf. README → "Limitations connues" :
 * Hytale n'expose pas (encore) les keybinds custom côté client. On utilise
 * donc l'événement d'interaction joueur, qui est intercepté côté serveur
 * via les paquets {@code SyncInteractionChains}.</p>
 */
public final class HestiaBagInteractListener {

    private final HestiaBagPlugin plugin;

    public HestiaBagInteractListener(HestiaBagPlugin plugin) {
        this.plugin = plugin;
    }

    /*
     * ⚠️ TODO — câbler sur le vrai event d'interaction Hytale.
     *
     * Référence : hytalemodding.dev "Player Input Guide" — interception via
     * SyncInteractionChains, vérifier InteractionType.PRIMARY_USE / SECONDARY_USE.
     *
     * @EventHandler
     * public void onInteract(PlayerInteractEvent e) {
     *     ItemStack held = e.getItemInHand();
     *     if (held == null || !HestiaItems.isHestiaBag(held)) return;
     *
     *     UUID uuid = e.getPlayer().getUniqueId();
     *     boolean inHome = e.getPlayer().getWorld().getName()
     *                         .equals(HestiaDimensionManager.HESTIA_WORLD_NAME);
     *
     *     if (inHome) {
     *         plugin.dimensions().exitHome(uuid);
     *     } else {
     *         plugin.dimensions().enterHome(uuid);
     *     }
     *     e.setCancelled(true);
     * }
     */
}
