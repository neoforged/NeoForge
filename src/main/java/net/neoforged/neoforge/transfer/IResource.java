/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

/**
 * Most general form of a resource that can be quantified and moved around.
 *
 * <p>Instances must all be immutable, comparable with {@link Object#equals(Object)}
 * and they must implement a suitable {@link Object#hashCode()}.
 */
public interface IResource {
    /**
     * Returns {@code true} if this represents the absence of a resource.
     *
     * <p>Examples include item resource with air as an item.
     */
    boolean isBlank(); // TODO: potentially use a different name
}
