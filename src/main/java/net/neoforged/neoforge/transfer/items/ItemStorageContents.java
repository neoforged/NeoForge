package net.neoforged.neoforge.transfer.items;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.transfer.ResourceStack;

import java.util.List;

public class ItemStorageContents {
    NonNullList<ResourceStack<ItemResource>> items;
    int hashCode;

    private ItemStorageContents(NonNullList<ResourceStack<ItemResource>> items) {
        this.items = items;
        this.hashCode = items.stream().map(ResourceStack::resource).toList().hashCode();
    }

    private ItemStorageContents(int size) {
        this(NonNullList.withSize(size, ItemResource.EMPTY_STACK));
    }

    private ItemStorageContents(List<ResourceStack<ItemResource>> items) {
        this(items.size());

        for (int i = 0; i < items.size(); i++) {
            this.items.set(i, items.get(i));
        }
    }

    public ItemStorageContents set(int slot, ResourceStack<ItemResource> stack) {
        NonNullList<ResourceStack<ItemResource>> newList = NonNullList.copyOf(items);
        newList.set(slot, stack);
        return new ItemStorageContents(newList);
    }

    public ItemStorageContents set(int slot, ItemResource resource, int amount) {
        return set(slot, new ResourceStack<>(resource, amount));
    }

    public ItemStorageContents setResource(int slot, ItemResource resource) {
        return set(slot, resource, get(slot).amount());
    }

    public ItemStorageContents setAmount(int slot, int amount) {
        return set(slot, get(slot).resource(), amount);
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
