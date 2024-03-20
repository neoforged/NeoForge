/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;

/**
 * <p>Loot modifier that rolls one loot table (the "subtable" and adds the results to the loot being modified (the "target table").
 * Loot modifiers are not rolled for the subtable, as that could result in the subtables'
 * items being modified twice (by downstream loot modifiers modifying the target table).</p>
 *
 * <p> Json format:
 * 
 * <pre>
 * {
 *   "type": "neoforge:add_table",
 *   "conditions": [], // conditions block to predicate target tables by
 *   "table": "namespace:loot_table_id" // subtable to roll loot for to add to the target table(s)
 * }
 * </pre>
 * 
 * </p>
 */
public class AddTableLootModifier extends LootModifier {
    /**
     * @see NeoForgeMod#ADD_TABLE_LOOT_MODIFIER_TYPE
     */
    @ApiStatus.Internal
    public static final Codec<AddTableLootModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
            ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(AddTableLootModifier::table)).apply(instance, AddTableLootModifier::new));

    private final ResourceKey<LootTable> table;

    protected AddTableLootModifier(LootItemCondition[] conditionsIn, ResourceKey<LootTable> table) {
        super(conditionsIn);
        this.table = table;
    }

    public ResourceKey<LootTable> table() {
        return this.table;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        context.getResolver().get(Registries.LOOT_TABLE, this.table).ifPresent(extraTable -> {
            // Don't run loot modifiers for subtables;
            // the added loot will be modifiable by downstream loot modifiers modifying the target table,
            // so if we modify it here then it could get modified twice.
            extraTable.value().getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        });
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return NeoForgeMod.ADD_TABLE_LOOT_MODIFIER_TYPE.get();
    }
}
