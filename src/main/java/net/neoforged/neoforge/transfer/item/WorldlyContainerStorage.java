package net.neoforged.neoforge.transfer.item;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@code Storage<ItemVariant>} for vanilla's {@link WorldlyContainer} .
 *
 * <p><b>Important note:</b> This wrapper assumes that the container owns its slots.
 * If the container does not own its slots, for example because it delegates to another container, this wrapper should not be used!
 */
public final class WorldlyContainerStorage implements Storage<ItemVariant> {
    private final WorldlyContainer container;
    private final ContainerStorage containerStorage;
    @Nullable
    private final Direction side;

    public WorldlyContainerStorage(WorldlyContainer container, @Nullable Direction side) {
        this.container = container;
        this.containerStorage = ContainerStorage.of(container);
        this.side = side;
    }

    private int convertSlot(int slot) {
        if (slot < 0) {
            throw new IllegalArgumentException("Cannot access storage with negative slot index: " + slot);
        }
        if (side == null) {
            return slot;
        }
        int[] slots = container.getSlotsForFace(side);
        if (slot >= slots.length) {
            throw new IllegalArgumentException("Cannot access storage at side " + side + " with out of bounds slot index " + slot);
        }
        return slots[slot];
    }

    @Override
    public int size() {
        if (side == null) {
            return container.getContainerSize();
        }
        return container.getSlotsForFace(side).length;
    }

    @Override
    public long insert(int index, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int convertedSlot = convertSlot(index);
        if (!container.canPlaceItemThroughFace(convertedSlot, resource.innerStack, side)) {
            return 0;
        }
        return containerStorage.insert(convertedSlot, resource, maxAmount, transaction);
    }

    @Override
    public long extract(int index, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int convertedSlot = convertSlot(index);
        if (side != null && !container.canTakeItemThroughFace(convertedSlot, resource.innerStack, side)) {
            return 0;
        }
        return containerStorage.extract(convertedSlot, resource, maxAmount, transaction);
    }

    @Override
    public boolean isResourceBlank(int index) {
        return containerStorage.isResourceBlank(convertSlot(index));
    }

    @Override
    public ItemVariant getResource(int index) {
        return containerStorage.getResource(convertSlot(index));
    }

    @Override
    public long getAmount(int index) {
        return containerStorage.getAmount(convertSlot(index));
    }

    @Override
    public long getCapacity(int index, ItemVariant resource) {
        return containerStorage.getCapacity(convertSlot(index), resource);
    }

    @Override
    public boolean isValid(int index, ItemVariant resource) {
        return containerStorage.isValid(convertSlot(index), resource);
    }

    @Override
    public String toString() {
        return "WorldlyContainerStorage{" +
                "container=" + container +
                ", side=" + side +
                "}";
    }
}
