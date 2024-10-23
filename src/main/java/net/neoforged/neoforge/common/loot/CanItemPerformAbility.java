/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * This LootItemCondition "neoforge:can_item_perform_ability" can be used to check if an item can perform a given ItemAbility.
 */
public class CanItemPerformAbility implements LootItemCondition {
    public static MapCodec<CanItemPerformAbility> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            ItemAbility.CODEC.fieldOf("ability").forGetter(action -> action.ability))
                    .apply(builder, CanItemPerformAbility::new));

    public static final LootItemConditionType LOOT_CONDITION_TYPE = new LootItemConditionType(CODEC);

    final ItemAbility ability;

    public CanItemPerformAbility(ItemAbility ability) {
        this.ability = ability;
    }

    public LootItemConditionType getType() {
        return LOOT_CONDITION_TYPE;
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext lootContext) {
        ItemStack itemstack = lootContext.getParamOrNull(LootContextParams.TOOL);
        return itemstack != null && itemstack.canPerformAction(this.ability);
    }

    public static LootItemCondition.Builder canItemPerformAbility(ItemAbility action) {
        return () -> new CanItemPerformAbility(action);
    }
}
