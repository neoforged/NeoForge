/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy;

import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.Objects;
import net.neoforged.neoforge.transfer.handlers.energy.IEnergyHandlerModifiable;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IHandleIOBehaviour;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.adapters.EnergyContainerSlice;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.energy.adapters.EnergyContainerToHandlerAdapter;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import org.jetbrains.annotations.Contract;

// Originally written by Soaryn for XyCraft adopted from Amadornes's ItemContainer.
public interface IEnergyContainer extends IntIterable {
    /**
     * Gets the size of this container.
     *
     * @return The size.
     */
    @Contract(pure = true)
    int size();

    int getMaxInsertRate();

    int getMaxExtractRate();

    /**
     * Gets the capacity or allowed size for the specified index.
     *
     * @param index The index.
     * @return The maximum allowed capacity.
     */
    @Contract(pure = true)
    int getCapacity(int index);

    SnapshotJournal<?> getSnapshotJournal(int index);

    /**
     * Clears all slots of resources. Sets them all to the empty variant.<br>
     * <b>Note:</b> There was a point of realization that this may not be performant on inheriting classes, but keep in mind that something like the slice needs to do this operation on its sub list.
     */
    default void clear() {
        var size = size();
        for (int i = 0; i < size; i++) {
            set(i, 0);
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
            if (get(index) > 0) return false;
        }
        return true;
    }

    //    /**
    //     * Creates a new item holder that wraps the specified {@link IResourceHandler}.
    //     *
    //     * @param handler The resource handler.
    //     * @return A wrapping item holder.
    //     */
    //    @Contract(value = "_, _ -> new", pure = true)
    //    static <T extends IResource> IEnergyContainer<T> wrap(IResourceHandler<T> handler, ResourceStack<T> emptyResource) {
    //        return new ResourceHandlerToContainerAdapter<>(handler, emptyResource);
    //    }

    /**
     * @param index The index where the energy is being stored.
     * @return The amount stored at the index.
     */

    int get(int index);

    /**
     * Sets the value at the specified index.
     *
     * @param index The index.
     */
    void set(int index, int value);

    @Override
    default IntIterator iterator() {
        return new IntIterator() {
            final int size = size();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public int nextInt() {
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
    default IEnergyContainer slice(int from, int to) {
        Objects.checkFromToIndex(from, to, size());
        return new EnergyContainerSlice(this, from, to - from);
    }

    /**
     * Creates an {@link IEnergyHandlerModifiable} instance that reflects this item holder.
     *
     * @return The resourceHandler handler.
     */
    @Contract(pure = true)
    default IEnergyHandlerModifiable asHandler() {
        return asHandler(IHandleIOBehaviour.DEFAULT);
    }

    /**
     * Creates an {@link IEnergyHandlerModifiable} instance that reflects this container with a specification of how to handle what slots can be inserted or extracted.
     *
     * @param behavior The behavior of the resource handler
     * @return The energy handler.
     */
    @Contract(pure = true)
    default IEnergyHandlerModifiable asHandler(IHandleIOBehaviour behavior) {
        return new EnergyContainerToHandlerAdapter(this, behavior);
    }

    /**
     * Copies all the contents of this container to a non-null list of the same size.
     *
     * @return A new non-null list.
     */
    @Contract(pure = true)
    default int[] copyToArray() {
        var array = new int[size()];
        var size = size();
        for (int index = 0; index < size; index++) {
            array[index] = get(index);
        }
        return array;
    }
}
