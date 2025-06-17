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
public interface IDataComponentHolderResource<T> extends IRegisteredResource<T>, DataComponentHolder {
    boolean isComponentsPatchEmpty();

    IDataComponentHolderResource<T> withPatch(DataComponentPatch patch);

    <D> IDataComponentHolderResource<T> with(DataComponentType<D> type, D data);

    IDataComponentHolderResource<T> without(DataComponentType<?> type);

    DataComponentPatch getComponentsPatch();

    default <D> IDataComponentHolderResource<T> with(Supplier<DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    default IDataComponentHolderResource<T> without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }
}
