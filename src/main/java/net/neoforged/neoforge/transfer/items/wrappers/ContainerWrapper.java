package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.TransferUtils;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.IResourceHandlerModifiable;

public class ContainerWrapper implements IResourceHandlerModifiable<ItemResource> {
    protected final Container container;

    public ContainerWrapper(Container container) {
        this.container = container;
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        container.setItem(index, resource.toStack(amount));
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public ItemResource getResource(int index) {
        return null;
    }

    @Override
    public int getAmount(int index) {
        return 0;
    }

    @Override
    public int getLimit(int index, ItemResource resource) {
        return 0;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return false;
    }

    @Override
    public boolean canInsert() {
        return false;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        return TransferUtils.insertStacking(this, resource, amount, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return TransferUtils.extractStacking(this, resource, amount, action);
    }
}
