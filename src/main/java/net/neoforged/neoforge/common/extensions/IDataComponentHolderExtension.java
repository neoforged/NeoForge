/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import org.jetbrains.annotations.Nullable;

public interface IDataComponentHolderExtension {
    private DataComponentHolder self() {
        return (DataComponentHolder) this;
    }

    @Nullable
    default <T> T get(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().get(type.get());
    }

    @Nullable
    default <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> type, T defaultValue) {
        return self().getOrDefault(type.get(), defaultValue);
    }

    default boolean has(Supplier<? extends DataComponentType<?>> type) {
        return self().has(type.get());
    }

    /**
     * Copies a data component from {@code this} component holder into the {@code target} component holder.
     */
    default <T> void copyFrom(MutableDataComponentHolder target, DataComponentType<T> componentType) {
        MutableDataComponentHolder.copyFrom(self(), target, componentType);
    }

    /**
     * Copies a data component from {@code this} component holder into the {@code target} component holder.
     */
    default <T> void copyFrom(MutableDataComponentHolder target, Supplier<? extends DataComponentType<T>> componentType) {
        MutableDataComponentHolder.copyFrom(self(), target, componentType);
    }
}
