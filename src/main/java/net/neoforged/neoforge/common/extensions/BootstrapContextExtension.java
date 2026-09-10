/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;

public interface BootstrapContextExtension<T> {
    default Holder.Reference<T> register(ResourceKey<T> key, T value, ICondition... conditions) {
        return self().register(key, value);
    }

    private BootstrapContext<T> self() {
        return (BootstrapContext<T>) this;
    }
}
