/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import java.util.Objects;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ResourceContainerContents;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ResourceContainerContentsHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Wraps the vanilla ComponentData of {@link ItemContainerContents} to allow it to be used as a {@link IResourceHandler}.
 * For use with any resource, consider using {@link ResourceContainerContents} and its associated handler {@link ResourceContainerContentsHandler}
 *
 * @see ResourceContainerContents
 * @see ResourceContainerContentsHandler
 */
public class ItemContainerContentsResourceHandler implements IResourceHandler<ItemResource> {
    protected final IItemContext itemContext;
    protected final DataComponentType<ItemContainerContents> componentType;
    /**
     * Size the component is expected to be able to grow to.
     */
    protected final int size;

    public ItemContainerContentsResourceHandler(IItemContext itemContext, DataComponentType<ItemContainerContents> componentType, int size) {
        if (size > 256)
            throw new IllegalArgumentException("Got %d items, but maximum is 256".formatted(size));

        this.componentType = componentType;
        this.itemContext = itemContext;
        this.size = size;
    }

    public ItemContainerContents getContents() {
        //If we don't have a container, we should return empty.
        if (itemContext.getAmount() == 0) return ItemContainerContents.EMPTY;
        ItemResource resource = itemContext.getResource();
        return resource.getOrDefault(componentType, ItemContainerContents.EMPTY);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        ItemContainerContents contents = getContents();
        return ItemResource.of(getStackInSlot(contents, index));
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        ItemContainerContents contents = getContents();
        return getStackInSlot(contents, index).getCount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) return Item.ABSOLUTE_MAX_STACK_SIZE;
        return Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) return true;
        return resource.getInstanceValue().canFitInsideContainerItems();
    }

    @Override
    public int characteristics(int index) {
        // despite the component growing and shrinking, the size of the handler is static
        return TransferCharacteristics.DEFAULT;
    }

    @Override
    public int characteristics() {
        // despite the component growing and shrinking, the size of the handler is static
        return TransferCharacteristics.DEFAULT;
    }

    private ItemStack getStackInSlot(ItemContainerContents contents, int index) {
        //Index bounds has already been checked by this point
        return contents.getSlots() <= index ? ItemStack.EMPTY : contents.getStackInSlot(index);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !isValid(index, resource)) return 0;

        ItemContainerContents contents = getContents();
        ItemStack stack = getStackInSlot(contents, index);

        if (stack.isEmpty()) {
            int inserted = Math.min(amount, resource.getMaxStackSize());
            return set(contents, inserted, context, index, resource.toStack(inserted));
        }

        if (!resource.is(stack) || stack.getCount() >= resource.getMaxStackSize()) return 0;

        int inserted = Math.min(amount, getCapacity(index, resource) - stack.getCount());
        stack.grow(inserted);
        return set(contents, inserted, context, index, stack);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        ItemContainerContents contents = getContents();
        ItemStack stack = getStackInSlot(contents, index);

        if (stack.isEmpty() || !resource.is(stack)) return 0;

        int extracted = Math.min(stack.getCount(), amount);
        stack.shrink(extracted);
        return set(contents, amount, context, index, stack);
    }

    /**
     * Handles changing the component out on the context container
     */
    @ApiStatus.OverrideOnly
    protected int set(ItemContainerContents contents, int changedAmount, TransactionContext context, int index, ItemStack stack) {
        ItemResource contextResource = itemContext.getResource();
        ItemStack newStack = contextResource.toStack();
        // Use the max of the content's size and the handler size to avoid truncating
        int contentSize = Math.max(contents.getSlots(), size());
        newStack.set(componentType, contents.with(contentSize, index, stack));
        //using the context, trade out our current container, for the new one.
        //While it is valid to try to do more than one at a time, we are going to handle just 1 exchange for now.
        int exchangedCount = itemContext.exchange(ItemResource.of(newStack), 1, context);
        return exchangedCount == 1 ? changedAmount : 0;
    }
}
