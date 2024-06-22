package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.HandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

public record LegacyItemHandlerWrapper(IItemHandler handler) implements IResourceHandler<ItemResource> {
    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        ItemStack notInserted = handler.insertItem(index, resource.toStack(amount), action.isSimulating());
        return notInserted.isEmpty() ? amount : amount - notInserted.getCount();
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        ItemStack test = handler.extractItem(index, amount, true);
        if (test.isEmpty() || !resource.matches(test)) return 0;
        return handler.extractItem(index, amount, action.isSimulating()).getCount();
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtil.insertStacking(this, resource, amount, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtil.extract(this, resource, amount, action);
    }

    @Override
    public int size() {
        return handler.getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(handler.getStackInSlot(index));
    }

    @Override
    public int getAmount(int index) {
        return handler.getStackInSlot(index).getCount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return handler.getSlotLimit(index);
    }

    @Override
    public int getCapacity(int index) {
        return handler.getSlotLimit(index);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return handler.isItemValid(index, resource.toStack());
    }

    @Override
    public boolean allowsInsertion(int index) {
        return true;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return true;
    }
}
