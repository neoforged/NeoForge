package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.ISingleResourceHandler;

public class EmptyHandler<T extends IResource> implements ISingleResourceHandler<T> {
    public static final EmptyHandler<ItemResource> ITEM = new EmptyHandler<>(ItemResource.BLANK);
    public static final EmptyHandler<FluidResource> FLUID = new EmptyHandler<>(FluidResource.BLANK);

    private final T emptyResource;

    public EmptyHandler(T emptyResource) {
        this.emptyResource = emptyResource;
    }

    @Override
    public T getResource() {
        return emptyResource;
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int getLimit() {
        return 0;
    }

    @Override
    public boolean isValid(T resource) {
        return true;
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
    public int insert(T resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return 0;
    }
}
