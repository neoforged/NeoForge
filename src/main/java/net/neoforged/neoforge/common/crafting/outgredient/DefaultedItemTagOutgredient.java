/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.Optional;

/**
 * Represents an item-based recipe outgredient that represents the item as a tag-fallback combination.
 * {@link DefaultedItemTagOutgredient#resolve()} resolves that combination into a concrete {@link ItemStack}.
 *
 * @param tagKey     The {@link TagKey} to use for looking up the outgredient.
 * @param fallback   The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
 * @param count      The count to use. Corresponds to {@link ItemStack#getCount()}.
 * @param components The data components to use. Corresponds to {@link ItemStack#getComponents()}.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public record DefaultedItemTagOutgredient(TagKey<Item> tagKey, Optional<Holder<Item>> fallback, int count, DataComponentPatch components) implements Outgredient<ItemStack> {
    public static final MapCodec<DefaultedItemTagOutgredient> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(it -> it.tagKey),
            BuiltInRegistries.ITEM.holderByNameCodec().optionalFieldOf("fallback").forGetter(it -> it.fallback),
            ExtraCodecs.intRange(1, 99).fieldOf("count").orElse(1).forGetter(it -> it.count),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(it -> it.components)
    ).apply(inst, DefaultedItemTagOutgredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultedItemTagOutgredient> STREAM_CODEC = StreamCodec.composite(
            TagKey.streamCodec(Registries.ITEM), it -> it.tagKey,
            Item.STREAM_CODEC.apply(ByteBufCodecs::optional), it -> it.fallback,
            ByteBufCodecs.VAR_INT, it -> it.count,
            DataComponentPatch.STREAM_CODEC, it -> it.components,
            DefaultedItemTagOutgredient::new);

    /**
     * @param tagKey     The {@link TagKey} to use for looking up the outgredient.
     * @param fallback   The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
     * @param count      The count to use. Corresponds to {@link ItemStack#getCount()}.
     * @param components The data components to use. Corresponds to {@link ItemStack#getComponents()}.
     */
    public DefaultedItemTagOutgredient(TagKey<Item> tagKey, Holder<Item> fallback, int count, DataComponentPatch components) {
        this(tagKey, Optional.of(fallback), count, components);
    }

    /**
     * @param tagKey   The {@link TagKey} to use for looking up the outgredient.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
     * @param count    The count to use. Corresponds to {@link ItemStack#getCount()}.
     */
    public DefaultedItemTagOutgredient(TagKey<Item> tagKey, Optional<Holder<Item>> fallback, int count) {
        this(tagKey, fallback, count, DataComponentPatch.EMPTY);
    }

    /**
     * @param tagKey   The {@link TagKey} to use for looking up the outgredient.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
     * @param count    The count to use. Corresponds to {@link ItemStack#getCount()}.
     */
    public DefaultedItemTagOutgredient(TagKey<Item> tagKey, Holder<Item> fallback, int count) {
        this(tagKey, Optional.of(fallback), count, DataComponentPatch.EMPTY);
    }

    /**
     * @param tagKey   The {@link TagKey} to use for looking up the outgredient.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
     */
    public DefaultedItemTagOutgredient(TagKey<Item> tagKey, Optional<Holder<Item>> fallback) {
        this(tagKey, fallback, 1, DataComponentPatch.EMPTY);
    }

    /**
     * @param tagKey   The {@link TagKey} to use for looking up the outgredient.
     * @param fallback The fallback to use if the tag-based lookup did not yield a conclusive outgredient.
     */
    public DefaultedItemTagOutgredient(TagKey<Item> tagKey, Holder<Item> fallback) {
        this(tagKey, Optional.of(fallback), 1, DataComponentPatch.EMPTY);
    }

    @Override
    public ItemStack resolve() {
        return NeoForgeEventHandler.getTagDefaultsManager()
                .resolve(Registries.ITEM, tagKey)
                .map(item -> new ItemStack(item.builtInRegistryHolder(), count, components))
                .orElseGet(() -> fallback.map(item -> new ItemStack(item, count, components)).orElse(ItemStack.EMPTY));
    }

    @Override
    public OutgredientType<? extends Outgredient<ItemStack>> type() {
        return NeoForgeMod.DEFAULTED_ITEM_TAG_OUTGREDIENT_TYPE.get();
    }

    @Override
    public SlotDisplay display() {
        return new Display(this);
    }

    public record Display(DefaultedItemTagOutgredient outgredient) implements ItemOutgredientSlotDisplay {
        public static final MapCodec<Display> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                DefaultedItemTagOutgredient.MAP_CODEC.fieldOf("outgredient").forGetter(Display::outgredient)
        ).apply(inst, Display::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Display> STREAM_CODEC = StreamCodec.composite(
                DefaultedItemTagOutgredient.STREAM_CODEC, Display::outgredient,
                Display::new);

        @Override
        public Type<? extends SlotDisplay> type() {
            return NeoForgeMod.DEFAULTED_FLUID_TAG_OUTGREDIENT_SLOT_DISPLAY.get();
        }
    }
}
