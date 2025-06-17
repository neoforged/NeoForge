/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources;

import java.util.Iterator;
import java.util.Objects;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.adapters.ResourceContainerSlice;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.adapters.ResourceContainerToHandlerAdapter;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.adapters.ResourceHandlerToContainerAdapter;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources.adapters.VanillaToItemContainerAdapter;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import org.jetbrains.annotations.Contract;

// Originally written by Soaryn for XyCraft adopted from Amadornes's ItemContainer.
public interface IResourceContainer<TResource extends IResource> extends Iterable<IResourceStack<TResource>>, Clearable {
    /**
     * Gets the size of this container.
     *
     * @return The size.
     */
    @Contract(pure = true)
    int size();

    SnapshotJournal<?> getIndexJournal(int index);

    /**
     * Clears all indices of resources. Sets them all to the empty variant.<br>
     * <b>Note:</b> There was a point of realization that this may not be performant on inheriting classes, but keep in mind that something like the slice needs to do this operation on its sub list.
     */
    default void clearContent() {
        var size = size();
        for (int i = 0; i < size; i++) {
            set(i, defaultResource().mutable());
        }
    }

    /**
     * Checks whether this container is completely empty.
     *
     * @return True if empty, false otherwise.
     */
    @Contract(pure = true)
    default boolean isEmpty() {
        var size = size();
        for (int index = 0; index < size; index++) {
            if (!get(index).isEmpty()) return false;
        }
        return true;
    }

    /**
     * Creates a new item holder that wraps the specified vanilla {@link Container}.
     *
     * @param container The vanilla container.
     * @return A wrapping item holder.
     */
    @Contract(value = "_ -> new", pure = true)
    static IResourceContainer<ItemResource> wrap(Container container) {
        return new VanillaToItemContainerAdapter(container);
    }

    /**
     * Creates a new item holder that wraps the specified {@link IResourceHandler}.
     *
     * @param handler The resource handler.
     * @return A wrapping item holder.
     */
    @Contract(value = "_, _ -> new", pure = true)
    static <T extends IResource> IResourceContainer<T> wrap(IResourceHandler<T> handler, ResourceStack<T> emptyResource) {
        return new ResourceHandlerToContainerAdapter<>(handler, emptyResource);
    }

    /**
     * Gets the {@link MutableResourceStack} in the specified index.<br/>
     * Stack is immutable, thus any updates must be carried out by calling {@link #set(int, MutableResourceStack)}.
     *
     * @param index The index.
     * @return The stack in the index.
     */

    MutableResourceStack<TResource> get(int index);

    /**
     * Sets the {@link MutableResourceStack} in the specified index.<br/>
     * Callers should call {@link #isValid(int, TResource)} before calling this.
     *
     * @param index The index.
     * @param stack The new stack.
     */
    void set(int index, MutableResourceStack<TResource> stack);

    /**
     * Checks whether the {@link TResource} can be put in the specified index.
     *
     * @param index    The index.
     * @param resource The resource.
     * @return True if valid, false otherwise.
     */
    @Contract(pure = true)
    default boolean isValid(int index, TResource resource) {
        Objects.checkIndex(index, size());
        return true;
    }

    /**
     * Gets the capacity or allowed size for the specified index.
     *
     * @param index The index.
     * @return The maximum allowed capacity.
     */
    @Contract(pure = true)
    int getCapacity(int index, TResource resource);

    /**
     * Creates an iterator over the indices of this container.
     *
     * @return A new iterator.
     */
    @Contract(value = "-> new", pure = true)
    @Override
    default Iterator<IResourceStack<TResource>> iterator() {
        return new Iterator<>() {
            final int size = size();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public IResourceStack<TResource> next() {
                return get(i++);
            }
        };
    }

    /**
     * Creates a slice view of this resource container.<br/>
     * Any changes made to the returned container will be reflected in this one.
     *
     * @param from Starting index (inclusive).
     * @param to   Final index (exclusive).
     * @return The resource holder slice.
     */
    @Contract(value = "_, _ -> new", pure = true)
    default IResourceContainer<TResource> slice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return new ResourceContainerSlice<>(this, from, to - from);
    }

    /**
     * Creates an {@link IResourceHandler} instance that reflects this item holder.
     *
     * @return The resourceHandler handler.
     */
    @Contract(pure = true)
    default IResourceHandler<TResource> asHandler() {
        return asHandler(IHandleIOBehaviour.DEFAULT);
    }

    /**
     * Creates an {@link IResourceHandler} instance that reflects this container with a specification of how to handle what indices can be inserted or extracted.
     *
     * @param behavior The behavior of the resource handler
     * @return The resource handler.
     */
    @Contract(pure = true)
    default IResourceHandler<TResource> asHandler(IHandleIOBehaviour behavior) {
        return new ResourceContainerToHandlerAdapter<>(this, behavior);
    }

    ResourceStack<TResource> defaultResource();

    /**
     * Copies all the contents of this container to a non-null list of the same size.
     *
     * @return A new non-null list.
     */
    @Contract(pure = true)
    default NonNullList<MutableResourceStack<TResource>> copyToList() {
        var list = NonNullList.withSize(size(), defaultResource().mutable());
        var size = size();
        for (int index = 0; index < size; index++) {
            list.set(index, get(index));
        }
        return list;
    }

    default NonNullList<ResourceStack<TResource>> immutableList() {
        var size = size();
        var list = NonNullList.withSize(size, defaultResource());
        for (int index = 0; index < size; index++) list.set(index, get(index).immutable());
        return list;
    }
}
