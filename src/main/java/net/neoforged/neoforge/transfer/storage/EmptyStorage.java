package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An immutable, empty storage.
 */
public final class EmptyStorage<T> implements Storage<T> {
    private final int size;
    private final T blankResource;

    public static final EmptyStorage<FluidVariant> FLUID = new EmptyStorage<>(0, FluidVariant.EMPTY);

    public static final EmptyStorage<ItemVariant> ITEM = new EmptyStorage<>(0, ItemVariant.EMPTY);

    public EmptyStorage(int size, T blankResource) {
        this.size = size;
        this.blankResource = blankResource;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public long insert(int index, T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long extract(int index, T resource, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean isResourceBlank(int index) {
        return true;
    }

    @Override
    public T getResource(int index) {
        return blankResource;
    }

    @Override
    public long getAmount(int index) {
        return 0;
    }

    @Override
    public long getCapacity(int index, T resource) {
        return 0;
    }

    @Override
    public boolean isValid(int index, T resource) {
        return false;
    }
}
