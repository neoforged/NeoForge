/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;

import java.util.Objects;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import org.jetbrains.annotations.ApiStatus;

/**
 * Immutable combination of an {@link Item} and data components.
 * Similar to an {@link ItemStack}, but immutable and without a count.
 */
public final class ItemResource implements IDataComponentHolderResource<Item> {
    /**
     * The empty resource instance of a {@link ItemResource}
     */
    public static final ItemResource EMPTY = new ItemResource(ItemStack.EMPTY);

    /**
     * Codec for an item resource.
     * Same format as {@link ItemStack#SINGLE_ITEM_CODEC}.
     * Does <b>not</b> accept empty resources.
     */
    public static final Codec<ItemResource> CODEC = ItemStack.SINGLE_ITEM_CODEC.xmap(ItemResource::of, ItemResource::toStack);

    /**
     * Codec for an item resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<ItemResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(ItemResource.EMPTY),
            itemResource -> itemResource.isEmpty() ? Optional.empty() : Optional.of(itemResource));

    /**
     * Stream codec for an item resource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), ItemResource::getHolder,
            DataComponentPatch.STREAM_CODEC, ItemResource::getComponentsPatch,
            ItemResource::of);

    /**
     * This is used only for registry, you should not use this method.
     */
    @ApiStatus.Internal
    public static ItemResource createDefaultInstance(Item item) {
        if (item == Items.AIR) return EMPTY;
        return new ItemResource(new ItemStack(item));
    }

    /**
     * Creates an ItemResource using the default or copy of the passed in item stack. Note the count is lost.
     *
     * @param stack stack to copy with a size of 1
     * @return If there were no patches on the stack's data components, the item's default resource will be returned, otherwise a new instance with the copied stack.
     */
    public static ItemResource of(ItemStack stack) {
        if (stack.isEmpty()) return EMPTY;

        if (stack.isComponentsPatchEmpty())
            return stack.getItem().getDefaultResource();

        return new ItemResource(stack.copyWithCount(1));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ItemResource of(ItemLike item) {
        Item value = item.asItem();
        if (value == Items.AIR) return EMPTY;
        return value.getDefaultResource();
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @param holder Item holder to create the resource with.
     * @param patch  Data components that should be on the resource instance.
     * @return a new {@link ItemResource}. If the item is empty, then {@link #EMPTY} will be returned; If the patch matches the default values the default instance of that item will be provided.
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ItemResource of(Holder<Item> holder, DataComponentPatch patch) {
        return of(holder.value(), patch);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @param item  Item to create the resource with.
     * @param patch Data components that should be on the resource instance.
     * @return a new {@link ItemResource}. If the item is empty, then {@link #EMPTY} will be returned; If the patch matches the default values the default instance of that item will be provided.
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ItemResource of(ItemLike item, DataComponentPatch patch) {
        Item value = item.asItem();
        if (value == Items.AIR) return EMPTY;
        if (patch.isEmpty()) return value.getDefaultResource();

        //The constructor that takes in a component patch doesn't take in an ItemLike so we opted for the setter method instead.
        ItemStack stack = new ItemStack(item);
        stack.applyComponents(patch);

        return new ItemResource(stack);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your item is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static ItemResource of(Holder<Item> holder) {
        return of(holder.value());
    }

    /**
     * A wrapped {@link ItemStack} which must never be modified or exposed. This will be a size of 1 so that we can make use
     * of the fact it is already an instance with a data component map.
     */
    private final ItemStack innerStack;

    private ItemResource(ItemStack stack) {
        this.innerStack = stack;
    }

    @Override
    public Item value() {
        return innerStack.getItem();
    }

    /**
     * @return The {@link Item} of the resource from the inner {@link ItemStack}
     */
    public Item getItem() {
        return value();
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
    public ItemResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty())
            return this;

        ItemStack stack = innerStack.copy();
        stack.applyComponents(patch);

        return ItemResource.of(stack);
    }

    @Override
    public <D> ItemResource with(DataComponentType<D> type, D data) {
        if (isEmpty()) return ItemResource.EMPTY;
        if (Objects.equals(get(type), data)) return this;

        ItemStack stack = innerStack.copy();
        stack.set(type, data);
        return ItemResource.of(stack);
    }

    //This is overridden to return ItemResource to allow method chaining
    @Override
    public <D> ItemResource with(Supplier<? extends DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    @Override
    public ItemResource without(DataComponentType<?> type) {
        if (isEmpty()) return ItemResource.EMPTY;
        if (get(type) == null) return this;

        ItemStack stack = innerStack.copy();
        stack.remove(type);
        return ItemResource.of(stack);
    }

    //This is overridden to return ItemResource to allow method chaining
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
     * @param count The amount of the item the stack should have. Must be non-negative.
     * @throws IllegalArgumentException when count is negative.
     */
    public ItemStack toStack(int count) {
        TransferPreconditions.checkNonNegative(count);
        if (count == 0) return ItemStack.EMPTY;
        return this.innerStack.copyWithCount(count);
    }

    /**
     * Creates an {@link ItemStack} with a count of 1.
     */
    public ItemStack toStack() {
        return this.innerStack.copyWithCount(1);
    }

    /**
     * @see ItemStack#getMaxStackSize()
     */
    public int getMaxStackSize() {
        return innerStack.getMaxStackSize();
    }

    /**
     * Returns the hover name of the {@link ItemStack}.
     *
     * @return The hover name of the {@link ItemStack}
     */
    public Component getHoverName() {
        return innerStack.getHoverName();
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
        //Item string with patch count
        return value() + " [" + getComponentsPatch().size() + "]";
    }
}
