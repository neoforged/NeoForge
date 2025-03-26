/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

public interface IDataComponentHolderExtension {
    private DataComponentHolder self() {
        return (DataComponentHolder) this;
    }

    default <T extends TooltipProvider> void addToTooltip(DataComponentType<T> type, Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        var value = self().get(type);

        if (value != null)
            value.addToTooltip(context, adder, flag, self());
    }

    default <T extends TooltipProvider> void addToTooltip(Supplier<? extends DataComponentType<T>> type, Item.TooltipContext context, Consumer<Component> adder, TooltipFlag flag) {
        addToTooltip(type.get(), context, adder, flag);
    }
}
