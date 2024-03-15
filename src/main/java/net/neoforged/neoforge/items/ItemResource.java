package net.neoforged.neoforge.items;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IResource;

/**
 * Immutable combination of an {@link Item} and data components.
 * Similar to an {@link ItemStack}, but immutable and without amount information.
 */
public final class ItemResource implements IResource, DataComponentHolder {
    // TODO: we need codecs and stream codecs...
    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

    public static ItemResource of(ItemStack itemStack) {
        return itemStack.isEmpty() ? EMPTY : new ItemResource(itemStack.copyWithCount(1));
    }

    /**
     * We wrap an item stack which must never be exposed and/or modified.
     */
    private final ItemStack innerStack;

    private ItemResource(ItemStack innerStack) {
        this.innerStack = innerStack;
    }

    @Override
    public boolean isBlank() {
        return innerStack.isEmpty();
    }

    public Item getItem() {
        return innerStack.getItem();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, innerStack);
    }

    public ItemStack toStack(int count) {
        return this.innerStack.copyWithCount(count);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ItemResource v && ItemStack.isSameItemSameComponents(v.innerStack, innerStack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(innerStack);
    }
}
