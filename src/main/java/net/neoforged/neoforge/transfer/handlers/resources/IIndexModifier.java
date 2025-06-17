package net.neoforged.neoforge.transfer.handlers.resources;

import net.neoforged.neoforge.transfer.resources.IResource;

@FunctionalInterface
public interface IIndexModifier<T extends IResource> {
    /**
     * Overrides the resource and amount at the given index.
     *
     * @param index    The index to set the resource at.
     * @param resource The resource to set.
     * @param amount   The amount of the resource to set.
     */
    void set(int index, T resource, int amount);
}
