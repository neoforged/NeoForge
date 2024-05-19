/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public class PiglinCurrencyItemPredicate implements ItemSubPredicate {
    public static final PiglinCurrencyItemPredicate INSTANCE = new PiglinCurrencyItemPredicate();
    public static final Codec<PiglinCurrencyItemPredicate> CODEC = Codec.unit(INSTANCE);
    public static final Type<PiglinCurrencyItemPredicate> TYPE = new Type<>(PiglinCurrencyItemPredicate.CODEC);

    private PiglinCurrencyItemPredicate() {}

    @Override
    public boolean matches(ItemStack stack) {
        return stack.isPiglinCurrency();
    }
}
