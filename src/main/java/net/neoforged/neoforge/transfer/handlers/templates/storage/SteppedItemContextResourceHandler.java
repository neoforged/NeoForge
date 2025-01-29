package net.neoforged.neoforge.transfer.handlers.templates.storage;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;

import java.util.function.Predicate;

public class SteppedItemContextResourceHandler<T extends IResource> extends ItemContextResourceHandler<T> {

    public SteppedItemContextResourceHandler(IItemContext context, DataComponentType<ResourceStack<T>> componentType, T emptyResource, int singleItemLimit) {
        super(context, componentType, emptyResource, singleItemLimit);
    }
    public SteppedItemContextResourceHandler(IItemContext context, DataComponentType<ResourceStack<T>> componentType, T emptyResource, int singleItemLimit, Predicate<T> validator) {
        super(context, componentType, emptyResource, singleItemLimit, validator);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !isValid(0, resource) || !isEmpty()) return 0;
        if (amount < singleItemLimit) return 0;
        return fill(resource, amount / singleItemLimit, action) * singleItemLimit;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || isEmpty() || !getResource(0).equals(resource)) return 0;
        if (amount > singleItemLimit) {
            int extractedCount = amount / singleItemLimit;
            int exchanged = empty(extractedCount, action);
            return exchanged * singleItemLimit;
        }
        return 0;
    }

    protected int fill(T resource, int count, TransferAction action) {
        ItemResource filledContainer = context.getResource().with(componentType, new ResourceStack<>(resource, singleItemLimit));
        return context.exchange(filledContainer, count, action);
    }
}

