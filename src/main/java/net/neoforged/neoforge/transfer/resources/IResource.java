/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

/**
 * Most general form of a resource that can be quantified and moved around.
 *
 * <p>Instances must all be immutable, comparable with {@link Object#equals(Object)}
 * and they must implement a suitable {@link Object#hashCode()}.
 * <p>
 * Note, the amount is not encoded in the resource, but rather something like a {@link ResourceStack} or {@link MutableResourceStack} based on what you need.
 */
public interface IResource {
    /**
     * Returns {@code true} if this represents an empty resource.
     *
     * <p>Examples include item resource with air as an item, or fluid resource with empty fluid.
     */
    boolean isEmpty();
}
