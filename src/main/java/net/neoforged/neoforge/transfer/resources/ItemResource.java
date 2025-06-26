/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
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
public final class ItemResource implements IDataComponentHolderResource<Item> {
    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);
    public static final ResourceStack<ItemResource> EMPTY_STACK = ResourceStack.constructEmptyReference(ItemResource.EMPTY);

    /**
     * Codec for an item resource.
     * Same format as {@link ItemStack#SINGLE_ITEM_CODEC}.
     * Does <b>not</b> accept empty resources.
     */
    public static final Codec<ItemResource> CODEC = Codec.lazyInitialized(() -> ItemStack.SINGLE_ITEM_CODEC.xmap(ItemResource::of, ItemResource::toStack));

    /**
     * Codec for an item resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<ItemResource> OPTIONAL_CODEC = Codec.lazyInitialized(() -> ExtraCodecs.optionalEmptyMap(CODEC).xmap(ItemResource::fromOptional, ItemResource::asOptional));

    /**
     * A codec for a {@code ResourceStack<ItemResource>} serializing the resource and the amount. Can accept empty resources.
     */
    public static final Codec<ResourceStack<ItemResource>> RESOURCE_STACK_CODEC = Codec.lazyInitialized(() -> ResourceStack.codec(OPTIONAL_CODEC, ItemResource::withAmount));

    /**
     * Stream codec for an item resource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ItemResource::getHolder,
            DataComponentPatch.STREAM_CODEC, ItemResource::getComponentsPatch,
            ItemResource::of);

    /**
     * Stream codec for a resource stack backed by an ItemResource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStack<ItemResource>> RESOURCE_STACK_STREAM_CODEC = ResourceStack.streamCodec(ItemResource.STREAM_CODEC, ItemResource::withAmount);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static ItemResource fromOptional(Optional<ItemResource> optional) {
        return optional.orElse(ItemResource.EMPTY);
    }

    public Optional<ItemResource> asOptional() {
        return isEmpty() ? Optional.empty() : Optional.of(this);
    }

    /**
     * A helper method to quickly construct an {@link ItemStack} from a ResourceStack
     *
     * @param resourceStack The resource stack with the fluid resource and amount
     * @return A new item stack with the same size as the resourceStack.
     */
    public static ItemStack itemStackOf(ResourceStack<ItemResource> resourceStack) {
        return resourceStack.resource().toStack(resourceStack.amount());
    }

    /**
     * This is used only for registry, you should not use this method!
     */
    @ApiStatus.Internal
    public static ItemResource invalidateDefault(ItemLike item) {
        if (item.asItem() == Items.AIR) return EMPTY;
        return new ItemResource(new ItemStack(item));
    }

    public static ItemResource of(ItemStack itemStack) {
        if (itemStack.isEmpty()) return EMPTY;

        if (itemStack.isComponentsPatchEmpty())
            return itemStack.getItem().getDefaultResource();

        return new ItemResource(itemStack.copyWithCount(1));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     */
    public static ItemResource of(ItemLike item) {
        if (item.asItem() == Items.AIR) return EMPTY;
        return item.asItem().getDefaultResource();
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     */
    public static ItemResource of(Holder<Item> item, DataComponentPatch patch) {
        if (item.value() == Items.AIR) return EMPTY;
        if (patch.isEmpty()) return item.value().getDefaultResource();
        return item.value().getDefaultResource().withPatch(patch);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     */
    public static ItemResource of(Holder<Item> item) {
        return of(item.value());
    }

    /**
     * We wrap an item stack which must never be modified.
     */
    final ItemStack innerStack;

    private ItemResource(ItemStack innerStack) {
        this.innerStack = innerStack;
    }

    /**
     * @return The {@link Item} of the resource from the inner {@link ItemStack}
     */
    @Override
    public Item getInstanceValue() {
        return innerStack.getItem();
    }

    @Override
    public Holder<Item> getHolder() {
        return innerStack.getItemHolder();
    }

    //Defers to the stack in case there are injections done to it at some point
    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return innerStack.isItemEnabled(enabledFeatures);
    }

    @Override
    public boolean isEmpty() {
        return innerStack.isEmpty();
    }

    public boolean is(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, innerStack);
    }

    public boolean is(ItemLike item) {
        return is(item.asItem());
    }

    public boolean test(Predicate<ItemStack> predicate) {
        return predicate.test(innerStack);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    @Override
    public ItemResource withPatch(DataComponentPatch patch) {
        ItemStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return new ItemResource(stack);
    }

    @Override
    public <D> ItemResource with(DataComponentType<D> type, D data) {
        ItemStack stack = innerStack.copy();
        stack.set(type, data);
        return new ItemResource(stack);
    }

    @Override
    public <D> ItemResource with(Supplier<DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    @Override
    public ItemResource without(DataComponentType<?> type) {
        ItemStack stack = innerStack.copy();
        stack.remove(type);
        return new ItemResource(stack);
    }

    @Override
    public ItemResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.immutableComponents();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        if (count == 0 || this.isEmpty()) return ItemStack.EMPTY;
        return this.innerStack.copyWithCount(count);
    }

    public List<ItemStack> toStacks(int count) {
        if (count == 0 || isEmpty())
            return Collections.emptyList();

        int maxStackSize = getMaxStackSize();
        int stackCount = count / maxStackSize;
        List<ItemStack> stacks = new ArrayList<>(stackCount + 1);
        for (int i = 0; i < stackCount; i++) {
            stacks.add(toStack(maxStackSize));
        }
        int remainder = count % maxStackSize;
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
        if (amount == 0 || isEmpty()) return ItemResource.EMPTY_STACK;
        return ResourceStack.of(this, amount, ItemResource.EMPTY_STACK);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof ItemResource other && other.is(innerStack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(innerStack);
    }

    @Override
    public String toString() {
        return innerStack.getItem().toString();
    }

    /**
     * @return the full value and data components in string form
     */
    public String toExpandedString() {
        if (isComponentsPatchEmpty()) {
            return toString();
        } else {
            return "%s %s".formatted(getInstanceValue(), getComponentsPatch().toString());
        }
    }
}
