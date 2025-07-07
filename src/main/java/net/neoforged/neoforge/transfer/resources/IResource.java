/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import org.jetbrains.annotations.ApiStatus;

/**
 * Most general form of a resource that can be quantified and moved around.
 *
 * <p>Instances must all be immutable, comparable with {@link Object#equals(Object)}
 * and they must implement a suitable {@link Object#hashCode()}.
 * <p>
 * Note, the amount is not encoded in the resource, for that you can use something like {@link ResourceStack}.
 */
public interface IResource<T extends IResource<T>> {
    /**
     * Returns {@code true} if this represents an empty resource.
     *
     * <p>Examples include item resource with air as an item, or fluid resource with empty fluid.
     */
    boolean isEmpty();

    /**
     * @return The empty resource stack of the resource type. If the resource type is classified as never being empty, then a defaulting instance should be specified.
     * @see ItemResource#EMPTY_STACK
     */
    ResourceStack<T> getEmptyResourceStackInstance();

    /**
     * @param amount Amount for the resource stack to have. Must be non-negative.
     * @return A new {@link ResourceStack} of the specified {@code amount}. If the amount or the resource is empty,
     *         the instance value provided by {@link #getEmptyResourceStackInstance()} will be returned instead.
     */
    @ApiStatus.NonExtendable
    default ResourceStack<T> withAmount(int amount) {
        //noinspection unchecked
        return ResourceStack.of((T) this, amount);
    }

    /**
     * @return The empty instance of the resource type. If the resource type is classified as never being empty, then a defaulting instance should be specified.
     * @see ItemResource#EMPTY
     */
    @ApiStatus.NonExtendable
    default T getEmptyInstance() {
        return getEmptyResourceStackInstance().resource();
    };
}
