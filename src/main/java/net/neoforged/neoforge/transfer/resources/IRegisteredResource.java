/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;

/**
 * A helper and optional interface that groups resources together if they are expected to be registered.
 * You shouldn't expect mods to implement this, and is highly likely only {@link ItemResource} and {@link FluidResource} will be the only implementers.
 *
 * @param <T> The backing type that the resource is targeting. Such as {@link net.minecraft.world.item.Item Item} for {@link ItemResource} or {@link net.minecraft.world.level.material.Fluid Fluid} for {@link FluidResource}
 */
public interface IRegisteredResource<T> extends IResource, DataComponentHolder {
    /**
     * @return The backing value of the resource.
     *         <p>
     *         - In the case of an {@link ItemResource} it should be an {@link net.minecraft.world.item.Item Item}
     *         <p>
     *         - In the case of an {@link FluidResource} it should be an {@link net.minecraft.world.level.material.Fluid Fluid}
     *         <p>
     *         - For a non-builtin {@link IResource resource}, you will need to consult the documentation or sources of the provider
     */
    T getInstanceValue();

    /**
     * @return The holder of the backing value.
     */
    Holder<T> getHolder();

    DataComponentMap getComponents();

    DataComponentPatch getComponentsPatch();

    boolean isComponentsPatchEmpty();

    boolean is(T item);

    boolean is(TagKey<T> tag);

    boolean is(Predicate<Holder<T>> predicate);

    default boolean is(Holder<T> holder) {
        return is(holder.value());
    }

    default boolean is(HolderSet<T> holderSet) {
        return holderSet.contains(getHolder());
    }

    /**
     * @return the full value and data components in string form
     */
    default String toExpandedString() {
        if (isComponentsPatchEmpty()) {
            return toString();
        } else {
            return "%s %s".formatted(getInstanceValue(), getComponentsPatch().toString());
        }
    }
}
