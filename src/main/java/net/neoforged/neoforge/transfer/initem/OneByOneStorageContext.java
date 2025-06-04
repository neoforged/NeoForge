package net.neoforged.neoforge.transfer.initem;

import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

class OneByOneStorageContext implements InItemStorageContext {
    private final InItemStorageContext delegate;

    public OneByOneStorageContext(InItemStorageContext delegate) {
        this.delegate = delegate;
    }

    @Override
    public ItemVariant getCurrent() {
        return delegate.getCurrent();
    }

    @Override
    public long getCurrentAmount() {
        return Math.max(1, delegate.getCurrentAmount());
    }

    @Override
    public boolean supportsModification() {
        return delegate.supportsModification();
    }

    @Override
    public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        // No reason to limit insertion, in case multiple items need to be inserted for some reason.
        return delegate.insert(itemVariant, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return delegate.extract(itemVariant, Math.max(1, maxAmount), transaction);
    }

    @Override
    public String toString() {
        return "OneByOneStorageContext[" + delegate + "]";
    }
}
