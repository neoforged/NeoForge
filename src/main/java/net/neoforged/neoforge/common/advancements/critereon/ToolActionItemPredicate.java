/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ToolAction;

public record ToolActionItemPredicate(ToolAction action) implements ICustomItemPredicate {
    public static final Codec<ToolActionItemPredicate> CODEC = ToolAction.CODEC.xmap(ToolActionItemPredicate::new, ToolActionItemPredicate::action);

    @Override
    public Codec<ToolActionItemPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.canPerformAction(action);
    }
}
