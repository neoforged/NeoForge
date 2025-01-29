package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;

import java.util.Objects;

/**
 * A basic {@link IResourceHandlerModifiable} implementation derived from an {@link IResourceContainer}.
 */
public record ResourceContainerToHandlerAdapter<TResource extends IResource>(
        IResourceContainer<TResource> container,
        IHandleIOBehaviour behavior) implements IResourceHandlerModifiable<TResource> {

    @Override
    public int size() {
        return container.size();
    }

    @Override
    public boolean allowsInsertion() {
        for (var i = 0; i < container.size(); i++)
            if (allowsInsertion(i))
                return true;
        return false;
    }

    @Override
    public boolean allowsExtraction() {
        for (var i = 0; i < container.size(); i++)
            if (allowsExtraction(i))
                return true;
        return false;
    }

    @Override
    public boolean allowsInsertion(int index) {
        return behavior.canInsert(index);
    }

    @Override
    public boolean allowsExtraction(int index) {
        return behavior.canExtract(index);
    }

    @Override
    public TResource getResource(int index) {
        return container.get(index).resource();
    }

    @Override
    public int getAmount(int index) {
        return container.get(index).amount();
    }

    @Override
    public int getCapacity(int index, TResource resource) {
        return container.getCapacity(index, resource);
    }

    @Override
    public int getCapacity(int index) {
        return container.getCapacity(index);
    }

    @Override
    public boolean isValid(int index, TResource resource) {
        return behavior.canInsert(index) && container.isValid(index, resource);
    }

    @Override
    public int insert(TResource resource, int amount, TransferAction action) {
        if (resource.isEmpty()) return 0;
        var handled = 0;
        for (var index = 0; index < size(); index++) {
            if (handled == amount)
                break;
            handled += indexInsert(index, resource, amount - handled, action);
        }
        return handled;
    }

    @Override
    public int insert(int index, TResource resource, int amount, TransferAction action) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty())
            return 0;
        return indexInsert(index, resource, amount, action);
    }

    private int indexInsert(int index, TResource resource, int amount, TransferAction action) {
        if (!behavior.canInsert(index) || !container.isValid(index, resource)) return 0;

        var resourceStackInSlot = container.get(index);
        var capacity = container.getCapacity(index, resource);

        int inserted, newStackSize;
        if (resourceStackInSlot.isEmpty()) {
            //the slot is empty
            inserted = Math.min(capacity, amount);
            newStackSize = inserted;
        } else {
            //is there an item in the slot already?
            if (!resourceStackInSlot.resource().equals(resource)) return 0;

            inserted = Math.min(capacity - resourceStackInSlot.amount(), amount);
            newStackSize = resourceStackInSlot.amount() + inserted;
        }

        if (newStackSize > 0 && action.isExecuting())
            set(index, resource, newStackSize);

        return inserted;
    }

    @Override
    public int extract(int index, TResource resource, int amount, TransferAction action) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty() || amount <= 0)
            return 0;
        return indexedExtract(index, resource, amount, action);
    }

    @Override
    public int extract(TResource resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0)
            return 0;
        var handled = 0;
        for (var index = 0; index < container.size(); index++) {
            if (handled == amount) break;
            handled += indexedExtract(index, resource, amount - handled, action);
        }
        return handled;
    }

    private int indexedExtract(int index, TResource resource, int amount, TransferAction action) {
        if (!behavior.canExtract(index)) return 0;

        var currentStack = container.get(index);
        if (!resource.equals(currentStack.resource())) return 0;

        var currentAmount = currentStack.amount();
        int handledAmount = Math.min(amount, currentAmount);
        if (action.isExecuting())
            set(index, resource, currentAmount - handledAmount);
        return handledAmount;
    }

    @Override
    public void set(int index, TResource resource, int amount) {
        Objects.checkIndex(index, size());
        var current = container.get(index);
        if (resource.isEmpty()) amount = 0;
        if (amount == 0)
            container.set(index, container.defaultResource().mutable());
        else if (current.resource().equals(resource))
            container.set(index, current.withAmount(amount));
        else
            container.set(index, MutableResourceStack.of(resource, amount));
    }
}
