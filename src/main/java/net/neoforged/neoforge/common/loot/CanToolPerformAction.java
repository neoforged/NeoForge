/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.ToolAction;
import org.jetbrains.annotations.NotNull;

/**
 * This LootItemCondition "neoforge:can_tool_perform_action" can be used to check if a tool can perform a given ToolAction.
 */
public class CanToolPerformAction implements LootItemCondition {
    public static Codec<CanToolPerformAction> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            ToolAction.CODEC.fieldOf("action").forGetter(action -> action.action))
                    .apply(builder, CanToolPerformAction::new));

    public static final LootItemConditionType LOOT_CONDITION_TYPE = new LootItemConditionType(CODEC);

    final ToolAction action;

    public CanToolPerformAction(ToolAction action) {
        this.action = action;
    }

    @NotNull
    public LootItemConditionType getType() {
        return LOOT_CONDITION_TYPE;
    }

    @NotNull
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.TOOL);
    }

    public boolean test(LootContext lootContext) {
        ItemStack itemstack = lootContext.getParamOrNull(LootContextParams.TOOL);
        return itemstack != null && itemstack.canPerformAction(this.action);
    }

    public static LootItemCondition.Builder canToolPerformAction(ToolAction action) {
        return () -> new CanToolPerformAction(action);
    }
}
