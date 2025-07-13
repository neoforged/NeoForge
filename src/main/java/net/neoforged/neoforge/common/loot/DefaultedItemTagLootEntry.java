/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * A loot pool entry that resolves the item using {@link net.neoforged.neoforge.common.crafting.outgredient.TagDefaultsManager}.
 * If the specified tag cannot be resolved, the fallback will be used. If the fallback is not specified, the entry will be empty.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DefaultedItemTagLootEntry extends LootPoolSingletonContainer {
    public static final MapCodec<DefaultedItemTagLootEntry> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(it -> it.tagKey),
            BuiltInRegistries.ITEM.holderByNameCodec().optionalFieldOf("fallback").forGetter(it -> it.fallback)).and(singletonFields(inst)).apply(inst, DefaultedItemTagLootEntry::new));
    private final TagKey<Item> tagKey;
    private final Optional<Holder<Item>> fallback;

    private DefaultedItemTagLootEntry(TagKey<Item> tagKey, Optional<Holder<Item>> fallback, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.tagKey = tagKey;
        this.fallback = fallback;
    }

    /**
     * Creates a new {@link DefaultedItemTagLootEntry} from a {@link TagKey} and a fallback {@link ItemStack}.
     * If you do not want to specify a fallback, use {@link DefaultedItemTagLootEntry#ofTag(TagKey)}.
     *
     * @param tagKey   The {@link TagKey} to use.
     * @param fallback The fallback {@link ItemStack} to use.
     * @return A new {@link DefaultedItemTagLootEntry}.
     */
    public static DefaultedItemTagLootEntry.Builder<?> ofTagWithFallback(TagKey<Item> tagKey, ItemLike fallback) {
        return simpleBuilder((weight, quality, conditions, functions) -> new DefaultedItemTagLootEntry(tagKey, Optional.of(fallback.asItem().builtInRegistryHolder()), weight, quality, conditions, functions));
    }

    /**
     * Creates a new {@link DefaultedItemTagLootEntry} from a {@link TagKey}.
     * If you want to specify a fallback, use {@link DefaultedItemTagLootEntry#ofTagWithFallback(TagKey, ItemLike)}.
     *
     * @param tagKey The {@link TagKey} to use.
     * @return A new {@link DefaultedItemTagLootEntry}.
     */
    public static DefaultedItemTagLootEntry.Builder<?> ofTag(TagKey<Item> tagKey) {
        return simpleBuilder((weight, quality, conditions, functions) -> new DefaultedItemTagLootEntry(tagKey, Optional.empty(), weight, quality, conditions, functions));
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> consumer, LootContext context) {
        NeoForgeEventHandler.getTagDefaultsManager()
                .resolve(Registries.ITEM, tagKey)
                .map(ItemStack::new)
                .ifPresentOrElse(consumer, () -> fallback.map(ItemStack::new).ifPresent(consumer));
    }

    @Override
    public LootPoolEntryType getType() {
        return NeoForgeMod.DEFAULTED_ITEM_TAG_LOOT_ENTRY.get();
    }
}
