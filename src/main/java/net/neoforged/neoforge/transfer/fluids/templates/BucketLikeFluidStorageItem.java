package net.neoforged.neoforge.transfer.fluids.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.ISingleStorage;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class BucketLikeFluidStorageItem implements ISingleStorage<FluidResource> {
    private final int individualLimit;
    private final Supplier<DataComponentType<SimpleFluidContent>> componentType;
    protected final IItemContext context;
    private Predicate<FluidResource> validator = r -> true;

    public BucketLikeFluidStorageItem(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context) {
        this.individualLimit = limit;
        this.componentType = componentType;
        this.context = context;
    }

    public BucketLikeFluidStorageItem setValidator(Predicate<FluidResource> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public FluidResource getResource() {
        return context.getResource().getOrDefault(componentType, SimpleFluidContent.EMPTY).getResource();
    }

    @Override
    public int getAmount() {
        return getIndividualAmount() * context.getAmount();
    }

    public int getIndividualAmount() {
        return context.getResource().getOrDefault(componentType, SimpleFluidContent.EMPTY).getAmount();
    }

    public int getIndividualLimit() {
        return individualLimit;
    }

    @Override
    public int getLimit() {
        return getIndividualLimit() * context.getAmount();
    }

    @Override
    public boolean isResourceValid(FluidResource resource) {
        return validator.test(resource);
    }

    @Override
    public boolean isEmpty() {
        return !context.getResource().has(componentType);
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || !isResourceValid(resource) || !isEmpty()) return 0;
        int toFill = getIndividualLimit();
        if (amount < toFill) return 0;
        return fill(resource, amount / toFill, action) * toFill;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (resource.isBlank() || amount <= 0 || isEmpty() || !getResource().equals(resource)) return 0;
        int containerFill = getIndividualAmount();
        if (amount > containerFill) {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, action);
            return exchanged * containerFill;
        }
        return 0;
    }

    protected int empty(int count, TransferAction action) {
        ItemResource emptiedContainer = context.getResource().remove(componentType);
        return context.exchange(emptiedContainer, count, action);
    }

    protected int fill(FluidResource resource, int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(componentType, SimpleFluidContent.of(resource, getIndividualAmount()));
        return context.exchange(filledContainer, count, action);
    }

    public static class Consumable extends BucketLikeFluidStorageItem {
        public Consumable(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context) {
            super(limit, componentType, context);
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends BucketLikeFluidStorageItem {
        private final ItemResource emptyContainer;

        public SwapEmpty(int limit, Supplier<DataComponentType<SimpleFluidContent>> componentType, IItemContext context, ItemResource emptyContainer) {
            super(limit, componentType, context);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.exchange(emptyContainer, count, action);
        }
    }
}
