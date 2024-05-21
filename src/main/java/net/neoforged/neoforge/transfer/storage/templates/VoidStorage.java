package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.ISingleStorage;

/**
 * VoidStorage is a template storage that can be filled indefinitely without ever getting full.
 * It does not store resources that get filled into it, but "destroys" them upon receiving them.
 * @param <T> The type of resource that this storage can accept.
 */
public class VoidStorage<T extends IResource> implements ISingleStorage<T> {
    public static final VoidStorage<ItemResource> ITEM = new VoidStorage<>(ItemResource.EMPTY);
    public static final VoidStorage<FluidResource> FLUID = new VoidStorage<>(FluidResource.EMPTY);

    private final T emptyResource;

    public VoidStorage(T emptyResource) {
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
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isResourceValid(T resource) {
        return true;
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return amount;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return 0;
    }
}
