/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;

import java.util.stream.Stream;

/**
 * Superinterface for {@link Result}s of type {@link ItemStack}.
 * Automatically resolves the display for {@link DisplayContentsFactory.ForStacks}.
 */
public interface ItemResultSlotDisplay extends ResultSlotDisplay<ItemStack> {
    @Override
    default <T> Stream<T> resolve(ContextMap context, DisplayContentsFactory<T> factory) {
        return factory instanceof DisplayContentsFactory.ForStacks<T> forStacks ? Stream.of(forStacks.forStack(result().resolve())) : Stream.empty();
    }
}
