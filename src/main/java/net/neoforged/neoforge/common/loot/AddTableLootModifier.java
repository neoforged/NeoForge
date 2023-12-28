/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

/**
 * <p>Loot modifier that rolls one loot table (the "subtable" and adds the results to the loot being modified (the "target table").
 * Loot modifiers are not rolled for the subtable, as that could result in the subtables'
 * items being modified twice (by downstream loot modifiers modifying the target table).</p>
 *
 * <p> Json format:
 * <pre>
 * {
 *   "type": "forge:add_table",
 *   "conditions": [], // conditions block to predicate target tables by
 *   "table": "namespace:loot_table_id" // subtable to roll loot for to add to the target table(s)
 * }
 * </pre>
 * </p>
 */
public class AddTableLootModifier extends LootModifier {

    public static final Codec<AddTableLootModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            IGlobalLootModifier.LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(glm -> glm.conditions),
            ResourceLocation.CODEC.fieldOf("table").forGetter(AddTableLootModifier::table)
    ).apply(instance, AddTableLootModifier::new));

    private final ResourceLocation table;

    /**
     * Constructs a LootModifier.
     *
     * @param conditionsIn the ILootConditions that need to be matched before the loot is modified.
     */
    public AddTableLootModifier(LootItemCondition[] conditionsIn, ResourceLocation table) {
        super(conditionsIn);
        this.table = table;
    }

    public ResourceLocation table() {
        return this.table;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        LootTable extraTable = context.getResolver().getLootTable(this.table);
        // Don't run loot modifiers for subtables;
        // the added loot will be modifiable by downstream loot modifiers modifying the target table,
        // so if we modify it here then it could get modified twice.
        extraTable.getRandomItemsRaw(context, LootTable.createStackSplitter(context.getLevel(), generatedLoot::add));
        return generatedLoot;
    }
}
