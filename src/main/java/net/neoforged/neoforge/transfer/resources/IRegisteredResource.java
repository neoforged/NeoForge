/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

/**
 * A helper version of {@link IResource} intended for resources registered to some registry bound by some holder, such as {@link Item Item} is for {@link ItemResource}
 *
 * @param <T> The type of the backing instance.
 * @see ItemResource
 * @see FluidResource
 */
public interface IRegisteredResource<T, R extends IResource<R>> extends IResource<R> {
    /**
     * @return The backing instance of the resource.
     * @see ItemResource#getInstanceValue() returns an Item
     * @see FluidResource#getInstanceValue() returns a Fluid
     */
    T getInstanceValue();

    /**
     * {@return The registered holder of the backing resource}
     * 
     * @see ItemResource#getHolder()
     * @see FluidResource#getHolder()
     */
    Holder<T> getHolder();

    /**
     * @param tag Tag to check
     * @return {@code true} if the holder from {@link #getHolder()} has the specified tag applied
     */
    @ApiStatus.NonExtendable
    default boolean is(TagKey<T> tag) {
        return getHolder().is(tag);
    }

    /**
     * @param instance the instance to compare
     * @return {@code true} if an exact equality comparison ('==') between the instance provided and the value from {@link #getInstanceValue()} is {@code true}
     */
    @ApiStatus.NonExtendable
    default boolean is(T instance) {
        return instance == getInstanceValue();
    }

    /**
     * @param predicate The predicate to perform the test.
     * @return {@code true} if the predicate's test returns {@code true} for the holder from {@link #getHolder()}.
     */
    @ApiStatus.NonExtendable
    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(getHolder());
    }

    /**
     * @param holder the holder to check
     * @return {@code true} if the holder's value is exactly equal ('==') to the instance value from {@link #getInstanceValue()}
     */
    @ApiStatus.NonExtendable
    default boolean is(Holder<T> holder) {
        return is(holder.value());
    }

    /**
     * @param holders Set of holders to check
     * @return {@code true} if the holder set contains the holder provided from {@link #getHolder()}
     */
    @ApiStatus.NonExtendable
    default boolean is(HolderSet<T> holders) {
        return holders.contains(getHolder());
    }
}
