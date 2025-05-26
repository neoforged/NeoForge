package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A single-slot item variant storage backed by an {@link ItemStack}.
 * Implementors should at least override {@link #getStack} and {@link #setStack},
 * and probably {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>{@link #canInsert} and {@link #canExtract} can be used for more precise control over which items may be inserted or extracted.
 * {@link #getCapacity(ItemVariant)} can be overridden to change the maximum capacity depending on the item variant.
 */
public abstract class SingleStackStorage extends SnapshotParticipant<ItemStack> implements Storage<ItemVariant> {
    /**
     * Return the stack of this storage. It will be modified directly sometimes to avoid needless copies.
     * However, any mutation of the stack will directly be followed by a call to {@link #setStack}.
     * This means that either returning the backing stack directly or a copy is safe.
     *
     * @return The current stack.
     */
    // TODO: name this getItem instead?
    protected abstract ItemStack getStack();

    /**
     * Set the stack of this storage.
     */
    // TODO: name this setItem instead?
    protected abstract void setStack(ItemStack stack);

    /**
     * Return {@code true} if the passed non-blank item variant can be inserted, {@code false} otherwise.
     */
    protected boolean canInsert(ItemVariant itemVariant) {
        return true;
    }

    /**
     * Return {@code true} if the passed non-blank item variant can be extracted, {@code false} otherwise.
     */
    protected boolean canExtract(ItemVariant itemVariant) {
        return true;
    }

    /**
     * Return the maximum capacity of this storage for the passed item variant.
     * If the passed item variant is blank, an estimate should be returned.
     *
     * <p>If the capacity should be limited by the max stack size of the item, this function must take it into account.
     * For example, a storage with a maximum count of 4, or less for items that have a smaller max stack size,
     * should override this to return {@code Math.min(itemVariant.getMaxStackSize(), 4);}.
     *
     * @return The maximum capacity of this storage for the passed item variant.
     */
    protected int getCapacity(ItemVariant itemVariant) {
        return itemVariant.getMaxStackSize();
    }

    @Override
    public int size() {
        return 1;
    }

    protected void checkSlot(int slot) {
        if (slot != 0) {
            throw new IllegalArgumentException("SingleStackStorage " + this + " can only accept slot 0, yet it received " + slot);
        }
    }

    @Override
    public long insert(int slot, ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
        checkSlot(slot);
        StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

        ItemStack currentStack = getStack();

        if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && canInsert(insertedVariant)) {
            int insertedAmount = (int) Math.min(maxAmount, getCapacity(insertedVariant) - currentStack.getCount());

            if (insertedAmount > 0) {
                updateSnapshots(transaction);
                currentStack = getStack();

                if (currentStack.isEmpty()) {
                    currentStack = insertedVariant.toStack(insertedAmount);
                } else {
                    currentStack.grow(insertedAmount);
                }

                setStack(currentStack);

                return insertedAmount;
            }
        }

        return 0;
    }

    @Override
    public long extract(int slot, ItemVariant variant, long maxAmount, TransactionContext transaction) {
        checkSlot(slot);
        StoragePreconditions.notBlankNotNegative(variant, maxAmount);

        ItemStack currentStack = getStack();

        if (variant.matches(currentStack) && canExtract(variant)) {
            int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

            if (extracted > 0) {
                this.updateSnapshots(transaction);
                currentStack = getStack();
                currentStack.shrink(extracted);
                setStack(currentStack);

                return extracted;
            }
        }

        return 0;
    }

    @Override
    public boolean isResourceBlank(int slot) {
        checkSlot(slot);
        return getStack().isEmpty();
    }

    @Override
    public ItemVariant getResource(int slot) {
        checkSlot(slot);
        return ItemVariant.of(getStack());
    }

    @Override
    public long getAmount(int slot) {
        checkSlot(slot);
        return getStack().getCount();
    }

    @Override
    public long getCapacity(int slot, ItemVariant variant) {
        checkSlot(slot);
        return getCapacity(variant);
    }

    @Override
    public boolean isValid(int slot, ItemVariant variant) {
        checkSlot(slot);
        return canInsert(variant);
    }

    @Override
    protected ItemStack createSnapshot() {
        ItemStack original = getStack();
        setStack(original.copy());
        return original;
    }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) {
        setStack(snapshot);
    }

    @Override
    public String toString() {
        return getClass().getName() + "[" + getStack() + "]";
    }
}
