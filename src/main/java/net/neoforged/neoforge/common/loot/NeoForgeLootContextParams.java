/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemInstance;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.RegisterLootContextParamsEvent;

public final class NeoForgeLootContextParams {
    /// Holds the itemstack whose data is being queried with the loot context (i.e. the fuel item in a furnace).
    public static final ContextKey<ItemInstance> QUERIED_STACK = new ContextKey<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "queried_stack"));
    public static final ContextKey<Direction> DIRECTION = new ContextKey<>(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "direction"));

    private NeoForgeLootContextParams() {}

    public static void registerParams(RegisterLootContextParamsEvent event) {
        event.register(QUERIED_STACK);
        event.register(DIRECTION);
    }
}
