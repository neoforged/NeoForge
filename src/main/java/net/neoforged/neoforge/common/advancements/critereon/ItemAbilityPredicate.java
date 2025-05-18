/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbility;

public record ItemAbilityPredicate(ItemAbility action) implements DataComponentPredicate {
    public static final Codec<ItemAbilityPredicate> CODEC = ItemAbility.CODEC.xmap(ItemAbilityPredicate::new, ItemAbilityPredicate::action);
    public static final Type<ItemAbilityPredicate> TYPE = new Type<>(ItemAbilityPredicate.CODEC);

    @Override
    public boolean matches(DataComponentGetter dataComponentGetter) {
        return dataComponentGetter instanceof ItemStack itemStack && itemStack.canPerformAction(action);
    }
}
