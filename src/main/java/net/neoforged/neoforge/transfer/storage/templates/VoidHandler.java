package net.neoforged.neoforge.transfer.storage.templates;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.ISingleResourceHandler;

/**
 * VoidStorage is a template storage that can be filled indefinitely without ever getting full.
 * It does not store resources that get filled into it, but "destroys" them upon receiving them.
 * @param <T> The type of resource that this storage can accept.
 */
public class VoidHandler<T extends IResource> implements ISingleResourceHandler<T> {
    public static final VoidHandler<ItemResource> ITEM = new VoidHandler<>(ItemResource.BLANK);
    public static final VoidHandler<FluidResource> FLUID = new VoidHandler<>(FluidResource.BLANK);

    private final T emptyResource;

    public VoidHandler(T emptyResource) {
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
    public int getLimit(T ignored) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isValid(T resource) {
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

    public static class Item extends VoidHandler<ItemResource> {
        public Item() {
            super(ItemResource.BLANK);
        }
    }

    public static class Fluid extends VoidHandler<FluidResource> {
        public Fluid() {
            super(FluidResource.BLANK);
        }
    }
}
