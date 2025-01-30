/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
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
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

/**
 * Immutable combination of an {@link Item} and data components.
 * Similar to an {@link ItemStack}, but immutable and without a count.
 */
public final class ItemResource implements IResource, DataComponentHolder {
    /**
     * Codec for an item resource.
     * Same format as {@link ItemStack#SINGLE_ITEM_CODEC}.
     * Does <b>not</b> accept blank resources.
     */
    public static final Codec<ItemResource> CODEC = Codec.lazyInitialized(() -> ItemStack.SINGLE_ITEM_CODEC.xmap(ItemResource::of, ItemResource::toStack));
    /**
     * Codec for an item resource. Same format as {@link #CODEC}, and also accepts blank resources.
     */
    public static final Codec<ItemResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(ItemResource::fromOptional, ItemResource::asOptional);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static ItemResource fromOptional(Optional<ItemResource> optional) {
        return optional.orElse(ItemResource.NONE);
    }

    private Optional<ItemResource> asOptional() {
        return isEmpty() ? Optional.empty() : Optional.of(this);
    }

    /**
     * Stream codec for an item resource. Accepts blank resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ItemResource::getItemHolder,
            DataComponentPatch.STREAM_CODEC, ItemResource::getComponentsPatch,
            ItemResource::of);

    public static ItemStack itemStackOf(IResourceStack<ItemResource> resourceStack) {
        return resourceStack.resource().toStack(resourceStack.amount());
    }

    public static final ItemResource NONE = new ItemResource(ItemStack.EMPTY);
    public static final ResourceStack<ItemResource> EMPTY_STACK = new ResourceStack<>(NONE, 0);

    /**
     * This is used only for registry, you should not use this method!
     */
    @ApiStatus.Internal
    public static ItemResource invalidateDefault(ItemLike  item) {
        return item == Items.AIR ? NONE : new ItemResource(item.asItem().getDefaultInstance().copyWithCount(1));
    }

    public static ItemResource of(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return NONE;
        if(itemStack.isComponentsPatchEmpty())
            return itemStack.getItem().defaultResource();
        return new ItemResource(itemStack.copyWithCount(1));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     */
    public static ItemResource of(ItemLike item) {
        return item == Items.AIR ? NONE : item.asItem().defaultResource();
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     */
    public static ItemResource of(Holder<Item> item, DataComponentPatch patch) {
        if (item.value() == Items.AIR) return NONE;

        return item.value().defaultResource().withPatch(patch);
    }

    /**
     * We wrap an item stack which must never be exposed and/or modified.
     */
    private final ItemStack innerStack;

    private ItemResource(ItemStack innerStack) {
        this.innerStack = innerStack;
    }

    @Override
    public boolean isEmpty() {
        return innerStack.isEmpty();
    }

    public boolean is(Item item) {
        return innerStack.is(item);
    }

    public boolean is(TagKey<Item> item) {
        return innerStack.is(item);
    }

    public ItemResource withPatch(DataComponentPatch patch) {
        ItemStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return new ItemResource(stack);
    }

    public <D> ItemResource with(DataComponentType<D> type, D data) {
        ItemStack stack = innerStack.copy();
        stack.set(type, data);
        return new ItemResource(stack);
    }

    public <D> ItemResource with(Supplier<DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    public ItemResource without(DataComponentType<?> type) {
        ItemStack stack = innerStack.copy();
        stack.remove(type);
        return new ItemResource(stack);
    }

    public ItemResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    public Item getItem() {
        return innerStack.getItem();
    }

    public Holder<Item> getItemHolder() {
        return innerStack.getItemHolder();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.immutableComponents();
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
        return count == 0 || this.isEmpty() ? ItemStack.EMPTY : this.innerStack.copyWithCount(count);
    }

    public List<ItemStack> toStacks(int count) {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int stackCount = count / getMaxStackSize();
        for (int i = 0; i < stackCount; i++) {
            stacks.add(toStack(getMaxStackSize()));
        }
        int remainder = count % getMaxStackSize();
        if (remainder > 0) {
            stacks.add(toStack(remainder));
        }
        return stacks;
    }

    public int getMaxStackSize() {
        return innerStack.getMaxStackSize();
    }

    public boolean canEquip(EquipmentSlot slot, LivingEntity entity) {
        return innerStack.canEquip(slot, entity);
    }

    public boolean canUnequip() {
        return !EnchantmentHelper.has(innerStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }

    public ResourceStack<ItemResource> withAmount(int amount) {
        return new ResourceStack<>(this, amount);
    }

    public MutableResourceStack<ItemResource> withMutableAmount(int amount) {
        return new MutableResourceStack<>(this, amount);
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

    @Override
    public String toString() {
        //DO we even want to try to encode the components into the print?
        return innerStack.getItem().toString();
    }
}
