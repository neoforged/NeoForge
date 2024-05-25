package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.ISingleResourceHandler;

public class EmptyResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    public static final EmptyResourceHandler<ItemResource> ITEM = new EmptyResourceHandler<>(ItemResource.EMPTY);
    public static final EmptyResourceHandler<FluidResource> FLUID = new EmptyResourceHandler<>(FluidResource.EMPTY);

    private final T emptyResource;

    public EmptyResourceHandler(T emptyResource) {
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
    public boolean isResourceValid(T resource) {
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
