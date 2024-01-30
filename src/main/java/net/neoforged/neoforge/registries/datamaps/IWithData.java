/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a registry object (usually a {@link net.minecraft.core.Holder}) that has data maps.
 *
 * @param <R> the type of the object
 */
public interface IWithData<R> {
    /**
     * {@return the data of the given type that is attached to this object, or {@code null} if one isn't}
     *
     * @param type the data type
     * @param <T>  the type of the data
     */
    @Nullable
    default <T> T getData(DataMapType<R, T> type) {
        return null;
    }
}
