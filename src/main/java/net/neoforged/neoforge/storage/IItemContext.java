package net.neoforged.neoforge.storage;

import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.items.ItemResource;

public interface IItemContext {
    default <T> T getCapability(ItemCapability<T, IItemContext> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    ItemResource getResource();

    int getAmount();

    // ResourceStack<ItemResource> getMainStack() instead of the 2 methods?

    int insert(ItemResource resource, int amount, boolean simulate);

    int extract(ItemResource resource, int amount, boolean simulate);

    int exchange(ItemResource resource, int amount, boolean simulate);
}
