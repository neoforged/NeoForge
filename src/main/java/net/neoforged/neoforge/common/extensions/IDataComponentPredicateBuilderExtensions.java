/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;

public interface IDataComponentPredicateBuilderExtensions {
    private DataComponentPredicate.Builder self() {
        return (DataComponentPredicate.Builder) this;
    }

    default <T> DataComponentPredicate.Builder expect(Supplier<? extends DataComponentType<? super T>> componentType, T value) {
        return self().expect(componentType.get(), value);
    }
}
