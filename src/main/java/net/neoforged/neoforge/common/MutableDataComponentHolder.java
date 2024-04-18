/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

public interface MutableDataComponentHolder extends DataComponentHolder {
    /**
     * Sets a data component.
     */
    @Nullable
    <T> T set(DataComponentType<? super T> componentType, @Nullable T value);

    /**
     * Sets a data component.
     */
    @Nullable
    default <T> T set(Supplier<? extends DataComponentType<? super T>> componentType, @Nullable T value) {
        return set(componentType.get(), value);
    }

    /**
     * Updates a data component if it exists, using an additional {@code updateContext}.
     */
    @Nullable
    default <T, U> T update(DataComponentType<T> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value), updateContext));
    }

    /**
     * Updates a data component if it exists, using an additional {@code updateContext}.
     */
    @Nullable
    default <T, U> T update(Supplier<? extends DataComponentType<T>> componentType, T value, U updateContext, BiFunction<T, U, T> updater) {
        return update(componentType.get(), value, updateContext, updater);
    }

    /**
     * Updates a data component if it exists.
     */
    @Nullable
    default <T> T update(DataComponentType<T> componentType, T value, UnaryOperator<T> updater) {
        return set(componentType, updater.apply(getOrDefault(componentType, value)));
    }

    /**
     * Updates a data component if it exists.
     */
    @Nullable
    default <T> T update(Supplier<? extends DataComponentType<T>> componentType, T value, UnaryOperator<T> updater) {
        return update(componentType.get(), value, updater);
    }

    /**
     * Removes a data component.
     */
    @Nullable
    <T> T remove(DataComponentType<? extends T> componentType);

    /**
     * Removes a data component.
     */
    @Nullable
    default <T> T remove(Supplier<? extends DataComponentType<? extends T>> componentType) {
        return remove(componentType.get());
    }

    /**
     * Copies a data component into the {@code target} component holder.
     */
    default <T> void copyInto(MutableDataComponentHolder target, DataComponentType<T> componentType) {
        copyInto(this, target, componentType);
    }

    /**
     * Copies a data component into the {@code target} component holder.
     */
    default <T> void copyInto(MutableDataComponentHolder target, Supplier<? extends DataComponentType<T>> componentType) {
        copyInto(this, target, componentType);
    }

    /**
     * Copes a data component from {@code src} into {@code target}
     */
    static <T> void copyInto(DataComponentHolder src, MutableDataComponentHolder target, DataComponentType<T> componentType) {
        target.set(componentType, src.get(componentType));
    }

    /**
     * Copes a data component from {@code src} into {@code target}
     */
    static <T> void copyInto(DataComponentHolder src, MutableDataComponentHolder target, Supplier<? extends DataComponentType<T>> componentType) {
        copyInto(src, target, componentType.get());
    }
}
