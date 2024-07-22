package net.neoforged.neoforge.items;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Slot to handle immutable itemstack storages (Ex: {@link ComponentItemHandler})
 * <p>
 * For an implementation for use with an {@link IItemHandler} see {@link SlotItemHandlerImmutable}
 * <p>
 * Vanilla MC code modifies the stack returned by `getStack()` directly, but it
 * calls {@code setChanged()} when that happens, so we just cache the returned stack,
 * and set it when {@code setChanged()} is called.
 */
public abstract class StackCopySlot extends Slot {
    private static final Container emptyInventory = new SimpleContainer(0);

    @Nullable
    private ItemStack cachedReturnedStack = null;

    public StackCopySlot(int x, int y) {
        super(emptyInventory, 0, x, y);
    }

    /**
     * Gets the immutable itemstack from the storage
     *
     * @return The immutable stack in this slot
     */
    protected abstract ItemStack getStackCopy();

    /**
     * Sets the immutable itemstack from the storage
     *
     * @param stack The stack to put into this slot
     */
    protected abstract void setStackCopy(ItemStack stack);

    @Override
    public final ItemStack getItem() {
        return cachedReturnedStack = getStackCopy();
    }

    @Override
    public final void set(ItemStack stack) {
        setStackCopy(stack);
        cachedReturnedStack = stack;
    }

    @Override
    public final void setChanged() {
        if (cachedReturnedStack != null) {
            set(cachedReturnedStack);
        }
    }

    @Override
    public final ItemStack remove(int amount) {
        ItemStack stack = getStackCopy().copy();
        ItemStack ret = stack.split(amount);
        set(stack);
        cachedReturnedStack = null;
        return ret;
    }
}