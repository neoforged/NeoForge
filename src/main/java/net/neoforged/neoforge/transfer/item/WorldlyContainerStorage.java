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
 *
 * @see ContainerStorage
 * @see InventoryStorage
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
    public long insert(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int convertedSlot = convertSlot(slot);
        if (!container.canPlaceItemThroughFace(convertedSlot, resource.innerStack, side)) {
            return 0;
        }
        return containerStorage.insert(convertedSlot, resource, maxAmount, transaction);
    }

    @Override
    public long extract(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        int convertedSlot = convertSlot(slot);
        if (side != null && !container.canTakeItemThroughFace(convertedSlot, resource.innerStack, side)) {
            return 0;
        }
        return containerStorage.extract(convertedSlot, resource, maxAmount, transaction);
    }

    @Override
    public boolean isResourceBlank(int slot) {
        return containerStorage.isResourceBlank(convertSlot(slot));
    }

    @Override
    public ItemVariant getResource(int slot) {
        return containerStorage.getResource(convertSlot(slot));
    }

    @Override
    public long getAmount(int slot) {
        return containerStorage.getAmount(convertSlot(slot));
    }

    @Override
    public long getCapacity(int slot, ItemVariant resource) {
        return containerStorage.getCapacity(convertSlot(slot), resource);
    }

    @Override
    public boolean isValid(int slot, ItemVariant resource) {
        return containerStorage.isValid(convertSlot(slot), resource);
    }

    @Override
    public String toString() {
        return "WorldlyContainerStorage{" +
                "container=" + container +
                ", side=" + side +
                "}";
    }
}
