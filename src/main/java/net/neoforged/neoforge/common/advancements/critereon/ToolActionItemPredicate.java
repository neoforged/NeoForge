/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ToolAction;

public record ToolActionItemPredicate(ToolAction action) implements ItemSubPredicate {
    public static final Codec<ToolActionItemPredicate> CODEC = ToolAction.CODEC.xmap(ToolActionItemPredicate::new, ToolActionItemPredicate::action);
    public static final Type<ToolActionItemPredicate> TYPE = new Type<>(ToolActionItemPredicate.CODEC);

    @Override
    public boolean matches(ItemStack stack) {
        return stack.canPerformAction(action);
    }
}
