package net.neoforged.neoforge.transfer.items;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.ResourceStack;

public class ItemStorageContents {
    public static final ItemStorageContents EMPTY = new ItemStorageContents(0);
    NonNullList<ResourceStack<ItemResource>> items;
    int hashCode;

    public static ItemStorageContents of(int size) {
        return new ItemStorageContents(size);
    }

    private ItemStorageContents(NonNullList<ResourceStack<ItemResource>> items) {
        this.items = items;
        this.hashCode = items.stream().map(ResourceStack::resource).toList().hashCode();
    }

    public ItemStorageContents(int size) {
        this(NonNullList.withSize(size, ItemResource.EMPTY_STACK));
    }

    public ItemStorageContents set(int slot, ResourceStack<ItemResource> stack) {
        NonNullList<ResourceStack<ItemResource>> newList = NonNullList.copyOf(items);
        newList.set(slot, stack);
        return new ItemStorageContents(newList);
    }

    public ItemStorageContents set(int slot, ItemResource resource, int amount) {
        return set(slot, new ResourceStack<>(resource, amount));
    }

    public ResourceStack<ItemResource> get(int slot) {
        return items.get(slot);
    }

    public int getAmount(int slot) {
        return get(slot).amount();
    }

    public ItemResource getResource(int slot) {
        return get(slot).resource();
    }

    public int size() {
        return items.size();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
