/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.world.item.ItemStack;

public class PiglinCurrencyItemPredicate implements DataComponentPredicate {
    public static final PiglinCurrencyItemPredicate INSTANCE = new PiglinCurrencyItemPredicate();
    public static final Codec<PiglinCurrencyItemPredicate> CODEC = MapCodec.unitCodec(INSTANCE);
    public static final Type<PiglinCurrencyItemPredicate> TYPE = new ConcreteType<>(PiglinCurrencyItemPredicate.CODEC);

    private PiglinCurrencyItemPredicate() {}

    @Override
    public boolean matches(DataComponentGetter dataComponentGetter) {
        return dataComponentGetter instanceof ItemStack itemStack && itemStack.isPiglinCurrency();
    }
}
