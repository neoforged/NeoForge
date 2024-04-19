/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
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
     * Copies a data component from {@code src}
     * 
     * @implNote This will clear the current component value if the requested {@code src} holder does not contain a matching {@code componentType} value.
     */
    default <T> void copyFrom(DataComponentType<T> componentType, DataComponentHolder src) {
        set(componentType, src.get(componentType));
    }

    /**
     * Copies a data component from {@code src}
     * 
     * @implNote This will clear the current component value if the requested {@code src} holder does not contain a matching {@code componentType} value.
     */
    default <T> void copyFrom(Supplier<? extends DataComponentType<T>> componentType, DataComponentHolder src) {
        copyFrom(componentType.get(), src);
    }

    /**
     * Applies a set of component changes to this stack.
     */
    void applyComponents(DataComponentPatch patch);

    /**
     * Applies a set of component changes to this stack.
     */
    void applyComponents(DataComponentMap components);
}
