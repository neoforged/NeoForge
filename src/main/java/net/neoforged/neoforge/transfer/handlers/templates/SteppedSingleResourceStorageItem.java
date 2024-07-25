package net.neoforged.neoforge.transfer.handlers.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class SteppedSingleResourceStorageItem<T extends IResource> implements ISingleResourceHandler<T> {
    protected final IItemContext context;
    protected final DataComponentType<ResourceStack<T>> componentType;

    protected final int singleItemLimit;

    protected final T emptyResource;
    protected final ResourceStack<T> emptyStack;

    protected Predicate<T> validator = r -> true;

    public SteppedSingleResourceStorageItem(IItemContext context, DataComponentType<ResourceStack<T>> componentType, T emptyResource, int singleItemLimit) {
        this.context = context;
        this.componentType = componentType;
        this.singleItemLimit = singleItemLimit;
        this.emptyResource = emptyResource;
        this.emptyStack = new ResourceStack<>(emptyResource, 0);
    }

    @Override
    public T getResource() {
        return context.getResource().getOrDefault(componentType, emptyStack).resource();
    }

    @Override
    public int getAmount() {
        return getSingleItemAmount() * context.getAmount();
    }

    protected int getSingleItemAmount() {
        return context.getResource().getOrDefault(componentType, emptyStack).amount();
    }

    @Override
    public int getCapacity(T resource) {
        return getCapacity();
    }

    @Override
    public int getCapacity() {
        return singleItemLimit * context.getAmount();
    }

    @Override
    public boolean isValid(T resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return !context.getResource().has(componentType);
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !isValid(resource) || !isEmpty()) return 0;
        if (amount < singleItemLimit) return 0;
        return fill(resource, amount / singleItemLimit, action) * singleItemLimit;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || isEmpty() || !getResource().equals(resource)) return 0;
        if (amount > singleItemLimit) {
            int extractedCount = amount / singleItemLimit;
            int exchanged = empty(extractedCount, action);
            return exchanged * singleItemLimit;
        }
        return 0;
    }

    protected int empty(int count, TransferAction action) {
        ItemResource emptiedContainer = context.getResource().remove(componentType);
        return context.exchange(emptiedContainer, count, action);
    }

    protected int fill(T resource, int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().set(componentType, new ResourceStack<>(resource, singleItemLimit));
        return context.exchange(filledContainer, count, action);
    }
}

