/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

public interface IItemPropertiesExtensions {
    private Item.Properties self() {
        return (Item.Properties) this;
    }

    default <T> Item.Properties component(Supplier<? extends DataComponentType<T>> componentType, T value) {
        return self().component(componentType.get(), value);
    }
}
