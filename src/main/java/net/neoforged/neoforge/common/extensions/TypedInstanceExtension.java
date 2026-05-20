/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.TypedInstance;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.IWithData;
import org.jspecify.annotations.Nullable;

public interface TypedInstanceExtension<T> extends IWithData<T> {
    @Nullable
    @Override
    default <D> D getData(DataMapType<T, D> type) {
        return self().typeHolder().getData(type);
    }

    private TypedInstance<T> self() {
        return (TypedInstance<T>) this;
    }
}
