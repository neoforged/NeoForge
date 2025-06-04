package net.neoforged.neoforge.transfer.storage.base;

import com.google.common.base.Preconditions;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A {@link Storage} that restricts the slots to a specific range.
 * Shifting of slot indices is handled automatically for you.
 */
public class SlotRangeStorage<T> implements Storage<T> {
    private final Storage<T> delegate;
    private final int minSlot;
    private final int maxSlot;

    public SlotRangeStorage(Storage<T> delegate, int minSlot, int maxSlotExclusive) {
        Preconditions.checkArgument(maxSlotExclusive > minSlot, "Max slot must be greater than min slot");
        this.delegate = delegate;
        this.minSlot = minSlot;
        this.maxSlot = maxSlotExclusive;
    }

    @Override
    public int size() {
        return maxSlot - minSlot;
    }

    private int adaptSlot(int slot) {
        StoragePreconditions.checkSlot(slot, size());
        return slot - minSlot;
    }

    @Override
    public long insert(int slot, T resource, long maxAmount, TransactionContext transaction) {
        return delegate.insert(adaptSlot(slot), resource, maxAmount, transaction);
    }

    @Override
    public boolean supportsInsertion() {
        return delegate.supportsInsertion();
    }

    @Override
    public long extract(int slot, T resource, long maxAmount, TransactionContext transaction) {
        return delegate.extract(adaptSlot(slot), resource, maxAmount, transaction);
    }

    @Override
    public boolean supportsExtraction() {
        return delegate.supportsExtraction();
    }

    @Override
    public boolean isResourceBlank(int slot) {
        return delegate.isResourceBlank(adaptSlot(slot));
    }

    @Override
    public T getResource(int slot) {
        return delegate.getResource(adaptSlot(slot));
    }

    @Override
    public long getAmount(int slot) {
        return delegate.getAmount(adaptSlot(slot));
    }

    @Override
    public long getCapacity(int slot, T resource) {
        return delegate.getCapacity(adaptSlot(slot), resource);
    }

    @Override
    public boolean isValid(int slot, T resource) {
        return delegate.isValid(adaptSlot(slot), resource);
    }
}
