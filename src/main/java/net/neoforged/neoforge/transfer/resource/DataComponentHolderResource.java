/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resource;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

/**
 * Helper interface for resources backed by a registry entry and which also hold data component values.
 *
 * <p>Note that {@link #isEmpty() empty resources} never have any data components,
 * and methods that modify data components will simply return the empty resource instance again.
 *
 * @param <T> The type of the backing registry entry.
 */
public interface DataComponentHolderResource<T> extends RegisteredResource<T>, DataComponentHolder {
    /**
     * Checks if the resource's data component holder has no patches applied to it.
     * Equivalent to checking if the {@link #getComponentsPatch() patch} is empty, but more efficient.
     */
    boolean isComponentsPatchEmpty();

    /**
     * {@return an instance of the resource with the desired patch applied on top of the existing one}
     *
     * @param patch The patch added to the new resource instance.
     * @implSpec If the patch is empty, the same resource instance is returned directly.
     */
    DataComponentHolderResource<T> withMergedPatch(DataComponentPatch patch);

    /**
     * {@return a resource with the data component set to the given value}
     *
     * @param type the type of data component
     * @param data the data to set
     * @param <D>  the type of data component
     */
    <D> DataComponentHolderResource<T> with(DataComponentType<D> type, D data);

    /**
     * {@return a resource without the data component, i.e. with the data component explicitly removed}
     *
     * @param type the type of data component
     */
    DataComponentHolderResource<T> without(DataComponentType<?> type);

    /**
     * Patches currently applied to the resource's data component holder.
     */
    DataComponentPatch getComponentsPatch();

    /**
     * {@return a resource with the data component set to the given value}
     *
     * @param type the supplier for the type of data component
     * @param data the data to set
     * @param <D>  the type of data component
     */
    default <D> DataComponentHolderResource<T> with(Supplier<? extends DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    /**
     * {@return a resource without the data component, i.e. with the data component explicitly removed}
     *
     * @param type the supplier for the type of data component
     */
    default DataComponentHolderResource<T> without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }
}
