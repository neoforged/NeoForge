package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;

import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

import java.util.Objects;

/**
 * A slice of a  {@link IResourceContainer}. Changes to the slice should reflect in the parent.
 */
public record ResourceContainerSlice<TResource extends IResource>(
        IResourceContainer<TResource> parent,
        int start,
        int length) implements IResourceContainer<TResource> {

    @Override
    public int size() {
        return length;
    }

    @Override
    public MutableResourceStack<TResource> get(int index) {
        Objects.checkIndex(index, length);
        return parent.get(index + start);
    }

    @Override
    public void set(int index, MutableResourceStack<TResource> stack) {
        Objects.checkIndex(index, length);
        parent.set(index + start, stack);
    }

    @Override
    public boolean isValid(int index, TResource stack) {
        Objects.checkIndex(index, length);
        return parent.isValid(index + start, stack);
    }

    @Override
    public int getCapacity(int index, TResource resource) {
        return parent.getCapacity(index, resource);
    }

    @Override
    public int getCapacity(int index) {
        return parent.getCapacity(index);    }

    @Override
    public IResourceContainer<TResource> slice(int from, int to) {
        Objects.checkFromToIndex(from, to, length);
        return new ResourceContainerSlice<>(parent, this.start + from, to - from);
    }

    @Override
    public ResourceStack<TResource> defaultResource() {
        return parent.defaultResource();
    }
}
