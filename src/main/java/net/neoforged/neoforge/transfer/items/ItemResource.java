/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;

/**
 * Immutable combination of an {@link Item} and data components.
 * Similar to an {@link ItemStack}, but immutable and without amount information.
 */
public final class ItemResource implements IResource, DataComponentHolder {
    /**
     * Codec for an item resource.
     * Same format as {@link ItemStack#SINGLE_ITEM_CODEC}.
     * Does <b>not</b> accept blank resources.
     */
    public static final Codec<ItemResource> CODEC = ItemStack.SINGLE_ITEM_CODEC
            .xmap(ItemResource::of, ItemResource::toStack);
    /**
     * Codec for an item resource. Same format as {@link #CODEC}, and also accepts blank resources.
     */
    public static final Codec<ItemResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(o -> o.orElse(ItemResource.EMPTY), r -> r.isBlank() ? Optional.of(ItemResource.EMPTY) : Optional.of(r));
    /**
     * Codec for an item resource and an amount. Does <b>not</b> accept empty stacks.
     */
    public static final Codec<ResourceStack<ItemResource>> WITH_AMOUNT_CODEC = ItemStack.CODEC
            .xmap(ItemStack::immutable, ItemStack::of);
    /**
     * Codec for an item resource and an amount. Accepts empty stacks.
     */
    public static final Codec<ResourceStack<ItemResource>> OPTIONAL_WITH_AMOUNT_CODEC = ItemStack.OPTIONAL_CODEC
            .xmap(ItemStack::immutable, ItemStack::of);
    /**
     * Stream codec for an item resource. Accepts blank resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> OPTIONAL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM),
            ItemResource::getItemHolder,
            DataComponentPatch.STREAM_CODEC,
            ItemResource::getComponentsPatch,
            ItemResource::of);

    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

    public static ItemResource of(ItemStack itemStack) {
        return itemStack.isEmpty() ? EMPTY : new ItemResource(itemStack.copyWithCount(1));
    }

    public static ItemResource of(ItemLike item) {
        return item == Items.AIR ? EMPTY : new ItemResource(new ItemStack(item));
    }

    public static ItemResource of(Holder<Item> item, DataComponentPatch patch) {
        return item.value() == Items.AIR ? EMPTY : new ItemResource(new ItemStack(item, 1, patch));
    }

    /**
     * We wrap an item stack which must never be exposed and/or modified.
     */
    private final ItemStack innerStack;

    private ItemResource(ItemStack innerStack) {
        this.innerStack = innerStack;
    }

    @Override
    public boolean isBlank() {
        return innerStack.isEmpty();
    }

    public ItemResource applyPatch(DataComponentPatch patch) {
        ItemStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return new ItemResource(stack);
    }

    public <D> ItemResource set(DataComponentType<D> type, D data) {
        ItemStack stack = innerStack.copy();
        stack.set(type, data);
        return new ItemResource(stack);
    }

    public <D> ItemResource set(Supplier<DataComponentType<D>> type, D data) {
        return set(type.get(), data);
    }

    public ItemResource remove(DataComponentType<?> type) {
        ItemStack stack = innerStack.copy();
        stack.remove(type);
        return new ItemResource(stack);
    }

    public ItemResource remove(Supplier<? extends DataComponentType<?>> type) {
        return remove(type.get());
    }

    public Item getItem() {
        return innerStack.getItem();
    }

    public Holder<Item> getItemHolder() {
        return innerStack.getItemHolder();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    public boolean matches(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, innerStack);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        return this.innerStack.copyWithCount(count);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ItemResource v && ItemStack.isSameItemSameComponents(v.innerStack, innerStack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(innerStack);
    }
}