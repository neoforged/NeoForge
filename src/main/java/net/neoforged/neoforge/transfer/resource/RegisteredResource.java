/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resource;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.TypedInstance;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helper interface for resources backed by a {@linkplain #value registry entry}.
 *
 * @param <T> The type of the backing registry entry.
 */
public interface RegisteredResource<T> extends Resource, TypedInstance<T> {
    /**
     * {@return the backing instance of the resource}
     */
    T value();

    /**
     * {@return The registered holder of the backing resource}
     * 
     * @deprecated Use {@link #typeHolder()}
     */
    @Deprecated
    default Holder<T> getHolder() {
        return typeHolder();
    }

    /**
     * @param tag Tag to check
     * @return {@code true} if the holder from {@link #typeHolder()} is in the specified tag
     */
    @ApiStatus.NonExtendable
    default boolean is(TagKey<T> tag) {
        return TypedInstance.super.is(tag);
    }

    /**
     * @param instance the instance to compare
     * @return {@code true} if an exact equality comparison ('==') between the instance provided and the value from {@link #value()} is {@code true}
     */
    @ApiStatus.NonExtendable
    default boolean is(T instance) {
        return TypedInstance.super.is(instance);
    }

    /**
     * @param predicate The predicate to perform the test.
     * @return {@code true} if the predicate's test returns {@code true} for the holder from {@link #typeHolder()}.
     */
    @ApiStatus.NonExtendable
    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(typeHolder());
    }

    /**
     * @param holder the holder to check
     * @return {@code true} if the holder's value is the instance value from {@link #value()}
     */
    @ApiStatus.NonExtendable
    default boolean is(Holder<T> holder) {
        return TypedInstance.super.is(holder);
    }

    /**
     * @param holders Set of holders to check
     * @return {@code true} if the holder set contains the holder provided from {@link #typeHolder()}
     */
    @ApiStatus.NonExtendable
    default boolean is(HolderSet<T> holders) {
        return TypedInstance.super.is(holders);
    }
}
