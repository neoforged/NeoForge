/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A helper version of {@link IResource} intended for resources registered to some registry bound by some backing
 * that also holds data component values. Such as {@link ItemResource} that has an inner {@link ItemStack}
 * which holds data components and patches associated to it; backed by an {@link Item}
 *
 * @param <T> The type of the backing instance.
 * @see ItemResource
 * @see FluidResource
 */
public interface IDataComponentHolderResource<T, R extends IResource<R>> extends IRegisteredResource<T, R>, DataComponentHolder {
    boolean isComponentsPatchEmpty();

    /**
     * Creates a new instance of the resource with the desired patch
     * 
     * @param patch The patch added to the new resource instance.
     * @return A new resource instance with applied patch.
     *         In the case of the resource being empty, the empty instance should be returned instead with no patches.
     */
    IDataComponentHolderResource<T, R> withPatch(DataComponentPatch patch);

    /**
     * Creates a new copy of this resource with the set data component.
     *
     * @param type the type of data component
     * @param data the data to set
     * @param <D>  the type of data component
     * @return The new resource. In the case of the resource being empty, the empty instance should be returned instead with no patches.
     */
    <D> IDataComponentHolderResource<T, R> with(DataComponentType<D> type, D data);

    /**
     * Creates a new copy of this resource without the data component.
     *
     * @param type the type of data component
     * @return The new resource. In the case of the resource being empty, the empty instance should be returned instead with no patches.
     */
    IDataComponentHolderResource<T, R> without(DataComponentType<?> type);

    /**
     * Patches currently applied to the resource's data component holder.
     */
    DataComponentPatch getComponentsPatch();

    /**
     * Creates a new copy of this resource with the set data component.
     *
     * @param type the supplier for the type of data component
     * @param data the data to set
     * @param <D>  the type of data component
     * @return The new resource. In the case of the resource being empty, the empty instance should be returned instead with no patches.
     */
    default <D> IDataComponentHolderResource<T, R> with(Supplier<DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    /**
     * Creates a new copy of this resource without the data component.
     *
     * @param type the supplier for the type of data component
     * @return The new resource. In the case of the resource being empty, the empty instance should be returned instead with no patches.
     */
    default IDataComponentHolderResource<T, R> without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }
}
