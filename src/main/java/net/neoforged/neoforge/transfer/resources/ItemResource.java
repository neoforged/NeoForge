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
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import org.jetbrains.annotations.ApiStatus;

/**
 * Immutable combination of an {@link Item} and data components.
 * Similar to an {@link ItemStack}, but immutable and without a count.
 */
public final class ItemResource implements IDataComponentHolderResource<Item> {
    /**
     * Resource information used to initialize the empty instance fields {@link #EMPTY} and {@link #EMPTY_STACK}.
     */
    private static final EmptyResourceInfo<ItemResource> INFO = new EmptyResourceInfo<>(new ItemResource(ItemStack.EMPTY));

    /**
     * The empty resource instance of a {@link ItemResource}
     */
    public static final ItemResource EMPTY = INFO.emptyInstance();
    /**
     * The empty resource stack instance of a {@link ItemResource}.
     */
    public static final ResourceStack<ItemResource> EMPTY_STACK = INFO.emptyResourceStack();

    /**
     * Codec for an item resource.
     * Same format as {@link ItemStack#SINGLE_ITEM_CODEC}.
     * Does <b>not</b> accept empty resources.
     */
    public static final Codec<ItemResource> CODEC = ItemStack.SINGLE_ITEM_CODEC.xmap(ItemResource::of, itemResource -> itemResource.toStack(1));

    /**
     * Codec for an item resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<ItemResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(ItemResource.EMPTY),
            itemResource -> itemResource.isEmpty() ? Optional.empty() : Optional.of(itemResource));

    /**
     * A codec for a {@code ResourceStack<ItemResource>} serializing the resource and the amount. Can accept empty resources.
     */
    public static final Codec<ResourceStack<ItemResource>> RESOURCE_STACK_CODEC = Codec.lazyInitialized(() -> ResourceStack.codec(OPTIONAL_CODEC));

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

    /**
     * A helper method to quickly construct an {@link ItemStack} from a ResourceStack
     *
     * @param resourceStack The resource stack with the item resource and amount
     * @return A new item stack with the same size as the resourceStack.
     */
    public static ItemStack itemStackOf(ResourceStack<ItemResource> resourceStack) {
        return resourceStack.as(ItemResource::toStack);
    }

    /**
     * This is used only for registry, you should not use this method!
     */
    @ApiStatus.Internal
    public static ItemResource createDefaultInstance(ItemLike item) {
        if (item.asItem() == Items.AIR) return EMPTY;
        return new ItemResource(new ItemStack(item));
    }

    /**
     * Creates an ItemResource using the default or copy of the passed in item stack. Note the count is lost.
     *
     * @param itemStack stack to copy with a size of 1
     * @return If there were no patches on the stack's data components, the item's default resource will be returned, otherwise a new instance with the copied stack.
     */
    public static ItemResource of(ItemStack itemStack) {
        if (itemStack.isEmpty()) return EMPTY;

        if (itemStack.isComponentsPatchEmpty())
            return itemStack.getItem().getDefaultResource();

        return new ItemResource(itemStack.copyWithCount(1));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ItemResource of(ItemLike item) {
        if (item.asItem() == Items.AIR) return EMPTY;
        return item.asItem().getDefaultResource();
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @param item  Item holder to create the resource with.
     * @param patch Data components that should be on the resource instance.
     * @return a new {@link ItemResource}. If the item is empty, then {@link #EMPTY} will be returned; If the patch matches the default values the default instance of that item will be provided.
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     * @throws IllegalStateException If the underlying default FluidResource when used has not been yet initialized.
     */
    public static ItemResource of(Holder<Item> item, DataComponentPatch patch) {
        return of(item.value(), patch);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @param item  Item to create the resource with.
     * @param patch Data components that should be on the resource instance.
     * @return a new {@link ItemResource}. If the item is empty, then {@link #EMPTY} will be returned; If the patch matches the default values the default instance of that item will be provided.
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     * @throws IllegalStateException If the underlying default {@link ItemResource} when used has not been yet initialized.
     */
    public static ItemResource of(Item item, DataComponentPatch patch) {
        if (item == Items.AIR) return EMPTY;
        if (patch.isEmpty()) return item.getDefaultResource();
        return item.getDefaultResource().withPatch(patch);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ItemResource of(Holder<Item> item) {
        return of(item.value());
    }

    /**
     * A wrapped {@link ItemStack} which must never be modified or exposed. This will be a size of 1 so that we can make use
     * of the fact it is already an instance with a data component map.
     */
    private final ItemStack innerStack;

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

    @Override
    public boolean isEmpty() {
        return innerStack.isEmpty();
    }

    /**
     * {@return true if the stack components and instance matches the inner stack's components and instance} Uses the {@link ItemStack#isSameItemSameComponents(ItemStack, ItemStack)} method for comparison.
     *
     * @param stack the item stack to check
     */
    public boolean is(ItemStack stack) {
        return ItemStack.isSameItemSameComponents(stack, innerStack);
    }

    /**
     * {@return true if the item instance matches the backing instance value}
     *
     * @param item the item to check
     */
    public boolean is(ItemLike item) {
        return is(item.asItem());
    }

    /**
     * Tests an {@link ItemStack} predicate with the inner stack.
     *
     * @param predicate Predicate to perform the test with
     * @return {@code true} if the test passed
     */
    public boolean test(Predicate<ItemStack> predicate) {
        return predicate.test(innerStack);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    @Override
    public ItemResource withPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty() || innerStack.getComponentsPatch().equals(patch))
            return this;

        ItemStack stack = innerStack.copy();
        stack.applyComponents(patch);

        return ItemResource.of(stack);
    }

    @Override
    public <D> ItemResource with(DataComponentType<D> type, D data) {
        if (isEmpty()) return ItemResource.EMPTY;
        ItemStack stack = innerStack.copy();
        stack.set(type, data);
        if (ItemStack.isSameItemSameComponents(innerStack, stack)) return this;

        return ItemResource.of(stack);
    }

    @Override
    public <D> ItemResource with(Supplier<? extends DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    @Override
    public ItemResource without(DataComponentType<?> type) {
        if (isEmpty()) return ItemResource.EMPTY;
        ItemStack stack = innerStack.copy();
        stack.remove(type);
        if (ItemStack.isSameItemSameComponents(innerStack, stack)) return this;

        return ItemResource.of(stack);
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

    /**
     * Creates an {@link ItemStack} of the specified count.
     *
     * @param count The amount of the item the stack should have.
     * @return A new copy of the inner item stack with the specified count.
     */
    public ItemStack toStack(int count) {
        return this.innerStack.copyWithCount(count);
    }

    /**
     * Creates an {@link ItemStack} with a count of 1.
     *
     * @return A new copy of the inner item stack with a count of 1.
     */
    public ItemStack toStack() {
        return toStack(1);
    }

    /**
     * Creates a list of {@link ItemStack ItemStacks} of the specified count taking into account the max stack size of the item.
     * Note: This is not guaranteed to be mutable. For the case of empty, an immutable empty list is returned.
     *
     * @param count The amount of the item the stack should have. Must be non-negative.
     * @return A list of item stacks that all have a maximum count of the max stack size of the item.
     * @throws IllegalArgumentException when the count is negative.
     */
    public List<ItemStack> toStacks(int count) {
        TransferPreconditions.checkNonNegative(count);
        if (count == 0 || isEmpty())
            return Collections.emptyList();

        int maxStackSize = getMaxStackSize();
        int stackCount = count / maxStackSize;
        int remainder = count % maxStackSize;

        List<ItemStack> stacks = new ArrayList<>(stackCount + 1);
        for (int i = 0; i < stackCount; i++) {
            stacks.add(toStack(maxStackSize));
        }

        if (remainder > 0) {
            stacks.add(toStack(remainder));
        }
        return stacks;
    }

    /**
     * Creates a new {@link ResourceStack} of the item resource with the specified amount.
     * 
     * @param amount Amount to make the stack with. Must be non-negative
     * @return A new {@link ResourceStack} with the specified amount.
     * @throws IllegalArgumentException when amount is negative
     */
    public ResourceStack<ItemResource> withAmount(int amount) {
        return ResourceStack.of(this, amount);
    }

    public int getMaxStackSize() {
        return innerStack.getMaxStackSize();
    }

    /**
     * @return The hover name of the {@link ItemStack}
     */
    public Component getHoverName() {
        return innerStack.getHoverName();
    }

    public boolean canEquip(EquipmentSlot slot, LivingEntity entity) {
        return innerStack.canEquip(slot, entity);
    }

    public boolean canUnequip() {
        return !EnchantmentHelper.has(innerStack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE);
    }

    @Override
    public EmptyResourceInfo<ItemResource> getEmptyInfo() {
        return INFO;
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
}
