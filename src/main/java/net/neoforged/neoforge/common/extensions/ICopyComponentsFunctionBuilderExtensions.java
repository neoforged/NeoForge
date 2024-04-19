/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;

public interface ICopyComponentsFunctionBuilderExtensions {
    private CopyComponentsFunction.Builder self() {
        return (CopyComponentsFunction.Builder) this;
    }

    default CopyComponentsFunction.Builder include(Supplier<? extends DataComponentType<?>> componentType) {
        return self().include(componentType.get());
    }

    default CopyComponentsFunction.Builder exclude(Supplier<? extends DataComponentType<?>> componentType) {
        return self().exclude(componentType.get());
    }
}
