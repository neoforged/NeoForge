/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.ItemAbility;
import org.jspecify.annotations.Nullable;

/**
 * This LootItemCondition "neoforge:can_item_perform_ability" can be used to check if an item can perform a given ItemAbility.
 */
public class CanItemPerformAbility implements LootItemCondition {
    public static MapCodec<CanItemPerformAbility> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            ItemAbility.CODEC.fieldOf("ability").forGetter(action -> action.ability))
                    .apply(builder, CanItemPerformAbility::new));

    final ItemAbility ability;

    public CanItemPerformAbility(ItemAbility ability) {
        this.ability = ability;
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    @Override
    public @Nullable Boolean constantValue(ConstantConditionEvaluationContext context) {
        return null;
    }

    @Override
    public void validate(ValidationContext context) {}

    @Override
    public boolean test(LootContext lootContext) {
        ItemInstance stack = lootContext.getOptional(LootContextParams.TOOL);
        return stack != null && stack.canPerformAction(this.ability);
    }

    public static LootItemCondition.Builder canItemPerformAbility(ItemAbility action) {
        return () -> new CanItemPerformAbility(action);
    }
}
