package com.hestia.bag.storage;

import java.util.UUID;

/**
 * A {@code PlacedBag} represents a Hestia Bag entity sitting in the outer world,
 * waiting for someone to interact with it.
 *
 * <p>This is value-only data. The actual entity rendering and physics live in
 * Hytale's entity system; this record is just the bridge between an entity in
 * the world and the player who owns it (so we know which dimension to portal
 * a guest into when they interact with it).
 *
 * @param entityUuid uuid of the in-world bag entity (provided by Hytale on spawn)
 * @param ownerUuid  player whose home this bag points to
 * @param position   where the bag was placed (used by reset/drop to know where
 *                   to spill the dimension contents)
 * @param skinId     active skin at placement time; the entity model uses this
 */
public record PlacedBag(
        UUID entityUuid,
        UUID ownerUuid,
        PlayerHome.WorldPosition position,
        String skinId
) {}
