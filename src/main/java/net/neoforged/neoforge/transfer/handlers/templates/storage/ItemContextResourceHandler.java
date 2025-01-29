package net.neoforged.neoforge.transfer.handlers.templates.storage;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;

import java.util.function.Predicate;

public class ItemContextResourceHandler<T extends IResource> implements ISingleResourceHandler<T> {
    protected final IItemContext context;
    protected final DataComponentType<ResourceStack<T>> componentType;

    protected final int singleItemLimit;

    protected final T emptyResource;
    protected final ResourceStack<T> emptyStack;

    protected final Predicate<T> validator;

    public ItemContextResourceHandler(IItemContext context, DataComponentType<ResourceStack<T>> componentType, T emptyResource, int singleItemLimit) {
        this(context, componentType, emptyResource, singleItemLimit, r->true);
    }

    public ItemContextResourceHandler(IItemContext context, DataComponentType<ResourceStack<T>> componentType, T emptyResource, int singleItemLimit, Predicate<T> validator) {
        this.context = context;
        this.componentType = componentType;
        this.singleItemLimit = singleItemLimit;
        this.emptyResource = emptyResource;
        this.emptyStack = new ResourceStack<>(emptyResource, 0);
        this.validator = validator;
    }


    @Override
    public T getResource(int index) {
        return context.getResource().getOrDefault(componentType, emptyStack).resource();
    }

    @Override
    public int getAmount(int index) {
        return getSingleItemAmount() * context.getAmount();
    }

    protected int getSingleItemAmount() {
        return context.getResource().getOrDefault(componentType, emptyStack).amount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        //This ignores say the resource size limits at the moment. As well as possibly able to overflow if done incorrectly
        return singleItemLimit*context.getAmount();
    }

    //Theoretical version
    @Override
    public int getCapacity(int index) {
        //Possibly able to overflow if done incorrectly
        return singleItemLimit * context.getAmount();
    }

    @Override
    public boolean isValid(int index, T resource) {
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
        if (resource.isEmpty() || amount <= 0 || !isValid(0, resource)) return 0;
        T presentResource = getResource(0);
        if (presentResource.isEmpty()) {
            if (amount < singleItemLimit) return setPartial(resource, amount, action) == 1 ? amount : 0;
            return setFull(resource, amount / singleItemLimit, action) * singleItemLimit;
        }

        if (!presentResource.equals(resource)) return 0;

        int containerFill = getSingleItemAmount();
        int spaceLeft = singleItemLimit - containerFill;
        if (amount < spaceLeft) return setPartial(resource, amount + containerFill, action) == 1 ? amount : 0;
        return setFull(resource, amount / spaceLeft, action) * spaceLeft;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || isEmpty() || !getResource(0).equals(resource)) return 0;
        int containerFill = getSingleItemAmount();
        if (amount < containerFill) {
            int exchanged = setPartial(resource, containerFill - amount, action);
            return exchanged == 1 ? amount : 0;
        } else {
            int extractedCount = amount / containerFill;
            int exchanged = empty(extractedCount, action);
            return exchanged * containerFill;
        }
    }

    protected int empty(int count, TransferAction action) {
        ItemResource emptiedContainer = context.getResource().without(componentType);
        return context.exchange(emptiedContainer, count, action);
    }

    protected int setFull(T resource, int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().with(componentType, new ResourceStack<>(resource, singleItemLimit));
        return context.exchange(filledContainer, count, action);
    }

    protected int setPartial(T resource, int amount, TransferAction action) {
        ItemResource filledContainer = context.getResource().with(componentType, new ResourceStack<>(resource, amount));
        return context.exchange(filledContainer, 1, action);
    }
}
