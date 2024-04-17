/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

public interface MutableComponentHolder extends DataComponentHolder {
    /**
     * Sets a data component.
     */
    @Nullable
    <T> T set(DataComponentType<? super T> componentType, @Nullable T value);

    /**
     * Updates a data component if it exists, using an additional {@code updateContext}.
     */
    @Nullable
    default <T, U> T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value), updateContext));
    }

    /**
     * Updates a data component if it exists.
     */
    @Nullable
    default <T> T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value)));
    }

    /**
     * Removes a data component.
     */
    @Nullable
    <T> T remove(DataComponentType<? extends T> componentType);

    /**
     * Copies a data component into the {@code target} component holder.
     */
    default <T> void copyInto(MutableComponentHolder target, DataComponentType<T> componentType) {
        copyInto(this, target, componentType);
    }

    /**
     * Copes a data component from {@code src} into {@code target}
     */
    static <T> void copyInto(DataComponentHolder src, MutableComponentHolder target, DataComponentType<T> componentType) {
        target.set(componentType, src.get(componentType));
    }
}
