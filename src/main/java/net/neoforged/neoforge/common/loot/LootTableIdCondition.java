/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class LootTableIdCondition implements LootItemCondition {
    public static final Codec<LootTableIdCondition> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            ResourceLocation.CODEC.fieldOf("loot_table_id").forGetter(idCondition -> idCondition.targetLootTableId))
                    .apply(builder, LootTableIdCondition::new));
    // TODO Forge Registry at some point?
    public static final LootItemConditionType LOOT_TABLE_ID = new LootItemConditionType(CODEC);
    public static final ResourceLocation UNKNOWN_LOOT_TABLE = new ResourceLocation("neoforge", "unknown_loot_table");

    private final ResourceLocation targetLootTableId;

    private LootTableIdCondition(final ResourceLocation targetLootTableId) {
        this.targetLootTableId = targetLootTableId;
    }

    @Override
    public LootItemConditionType getType() {
        return LOOT_TABLE_ID;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getQueriedLootTableId().equals(this.targetLootTableId);
    }

    public static Builder builder(final ResourceLocation targetLootTableId) {
        return new Builder(targetLootTableId);
    }

    public static class Builder implements LootItemCondition.Builder {
        private final ResourceLocation targetLootTableId;

        public Builder(ResourceLocation targetLootTableId) {
            if (targetLootTableId == null) throw new IllegalArgumentException("Target loot table must not be null");
            this.targetLootTableId = targetLootTableId;
        }

        @Override
        public LootItemCondition build() {
            return new LootTableIdCondition(this.targetLootTableId);
        }
    }
}
