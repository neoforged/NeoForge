/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation that defines what a global loot modifier must implement in order to be functional.
 * {@link LootModifier} Supplies base functionality; most modders should only need to extend that.<br/>
 * Requires a {@link Codec} to be registered: {@link ForgeRegistries#GLOBAL_LOOT_MODIFIER_SERIALIZERS}, and returned in {@link #codec()}
 * Individual instances of modifiers must be registered via json, see neoforge:loot_modifiers/global_loot_modifiers
 */
public interface IGlobalLootModifier {
    Codec<IGlobalLootModifier> DIRECT_CODEC = ForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS.byNameCodec()
            .dispatch(IGlobalLootModifier::codec, Function.identity());
    Codec<Optional<WithConditions<IGlobalLootModifier>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(DIRECT_CODEC, "neoforge:conditions").codec();

    Codec<LootItemCondition[]> LOOT_CONDITIONS_CODEC = LootItemConditions.CODEC.listOf().xmap(list -> list.toArray(LootItemCondition[]::new), List::of);

    /**
     * Applies the modifier to the list of generated loot. This function needs to be responsible for
     * checking ILootConditions as well.
     * 
     * @param generatedLoot the list of ItemStacks that will be dropped, generated by loot tables
     * @param context       the LootContext, identical to what is passed to loot tables
     * @return modified loot drops
     */
    @NotNull
    ObjectArrayList<ItemStack> apply(ObjectArrayList<ItemStack> generatedLoot, LootContext context);

    /**
     * Returns the registered codec for this modifier
     */
    Codec<? extends IGlobalLootModifier> codec();
}
