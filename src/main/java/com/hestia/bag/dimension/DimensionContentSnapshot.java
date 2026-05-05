package com.hestia.bag.dimension;

/**
 * Captured contents of a dimension at the moment of reset.
 *
 * <p>This is a value-collecting bag for {@link DimensionResetService}. It
 * intentionally has no {@code reset()} or {@code clear()} method — once a
 * snapshot exists, it represents a frozen point-in-time view that gets fed
 * to the drop pipeline.
 *
 * <p>Currently a placeholder: the actual implementation will hold typed
 * collections of dropped items, mature crops, and pending entity removals
 * once the Hytale inventory and entity APIs are wired.
 */
public final class DimensionContentSnapshot {

    // ⚠️ TODO — once Hytale's ItemStack / Inventory API is wired in, replace
    // this size counter with concrete typed collections, e.g.:
    //   private final List<ItemStack> droppedItems = new ArrayList<>();
    //   private final List<MatureCrop> harvestedCrops = new ArrayList<>();
    //   private final List<UUID> entitiesToDespawn = new ArrayList<>();

    private int size = 0;

    public void recordItemPlaceholder() {
        size++;
    }

    public int size() {
        return size;
    }
}
