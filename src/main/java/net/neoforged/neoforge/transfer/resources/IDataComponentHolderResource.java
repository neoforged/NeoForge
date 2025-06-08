/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;

public interface IDataComponentHolderResource<T> extends IRegisteredResource<T>, DataComponentHolder {
    boolean isComponentsPatchEmpty();

    IDataComponentHolderResource<T> withPatch(DataComponentPatch patch);

    <D> IDataComponentHolderResource<T> with(DataComponentType<D> type, D data);

    IDataComponentHolderResource<T> without(DataComponentType<?> type);

    DataComponentMap getComponents();

    DataComponentPatch getComponentsPatch();

    default <D> IDataComponentHolderResource<T> with(Supplier<DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    default IDataComponentHolderResource<T> without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }
}
