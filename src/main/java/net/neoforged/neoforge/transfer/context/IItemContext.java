package net.neoforged.neoforge.transfer.context;

import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.TransferAction;

public interface IItemContext {
    default <T> T getCapability(ItemCapability<T, IItemContext> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    ItemResource getResource();

    int getAmount();

    // ResourceStack<ItemResource> getMainStack() instead of the 2 methods?

    int insert(ItemResource resource, int amount, TransferAction action);

    int extract(ItemResource resource, int amount, TransferAction action);

    int exchange(ItemResource resource, int amount, TransferAction action);
}
