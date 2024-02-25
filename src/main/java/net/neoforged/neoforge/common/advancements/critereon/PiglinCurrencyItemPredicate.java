/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

public class PiglinCurrencyItemPredicate implements ICustomItemPredicate {
    public static final PiglinCurrencyItemPredicate INSTANCE = new PiglinCurrencyItemPredicate();
    public static final Codec<PiglinCurrencyItemPredicate> CODEC = Codec.unit(INSTANCE);

    private PiglinCurrencyItemPredicate() {}

    @Override
    public Codec<PiglinCurrencyItemPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.isPiglinCurrency();
    }
}
