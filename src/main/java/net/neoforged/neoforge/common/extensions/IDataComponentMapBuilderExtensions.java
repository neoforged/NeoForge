/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

public interface IDataComponentMapBuilderExtensions {
    private DataComponentMap.Builder self() {
        return (DataComponentMap.Builder) this;
    }

    default <T> DataComponentMap.Builder set(Supplier<? extends DataComponentType<T>> componentType, @Nullable T value) {
        return self().set(componentType.get(), value);
    }
}
