/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import com.google.common.math.IntMath;
import java.util.Objects;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

// Not quite ready for review, but you are welcome to look over it if you wish. Missing docs but that will be coming next.
// This will be replacing the ResourceStorageComponentHandler. Items will be redundant, but likely not much harm in providing it,
// when providing fluids to help show the different approaches.
public class ResourceContainerContentsHandler<T extends IResource> implements IResourceHandler<T> {
    /**
     * Size the component is expected to be able to grow to.
     */
    protected final int size;
    protected final DataComponentType<ResourceContainerContents<T>> componentType;
    protected final IItemContext itemContext;
    private final ResourceContainerContents<T> emptyContents;
    private final IStackFactory<T, ResourceStack<T>> stackFactory;
    private final T emptyResource;
    private final int capacity;

    //Docs will come in next batch of commits or so.
    public ResourceContainerContentsHandler(IItemContext itemContext, DataComponentType<ResourceContainerContents<T>> componentType, int size, int capacity, T emptyResource, IStackFactory<T, ResourceStack<T>> stackFactory, ResourceContainerContents<T> emptyContents) {
        if (size > 256)
            throw new IllegalArgumentException("Got %d items, but maximum is 256".formatted(size));

        this.size = size;
        this.capacity = capacity;
        this.itemContext = itemContext;
        this.componentType = componentType;

        this.emptyContents = emptyContents;
        this.stackFactory = stackFactory;
        this.emptyResource = emptyResource;
    }

    //If we don't have a container, we should return empty.
    public ResourceContainerContents<T> getContents() {
        if (itemContext.getAmount() == 0) return emptyContents;
        ItemResource resource = itemContext.getResource();
        return resource.getOrDefault(componentType, emptyContents);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getResource(int index) {
        Objects.checkIndex(index, size());
        ResourceContainerContents<T> contents = getContents();
        return getStackInSlot(contents, index).resource();
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        ResourceContainerContents<T> contents = getContents();
        return getStackInSlot(contents, index).amount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        Objects.checkIndex(index, size());
        //items for example need an overridden version of this to respect stack sizes.
        return capacity;
    }

    @Override
    public boolean isValid(int index, T resource) {
        Objects.checkIndex(index, size());
        //Like items, you may need want to override this to have custom is valid behaviour
        // for things like can fit in container items.
        return true;
    }

    @Override
    public int characteristics(int index) {
        Objects.checkIndex(index, size());
        return TransferCharacteristics.DEFAULT;
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.DEFAULT;
    }

    private ResourceStack<T> getStackInSlot(ResourceContainerContents<T> contents, int index) {
        //Index has already been checked by this point
        if (contents.getSlots() <= index)
            return stackFactory.create(emptyResource, 0);
        return contents.getStackInSlot(index);
    }

    @Override
    public int insert(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount) || !isValid(index, resource)) return 0;

        ResourceContainerContents<T> contents = getContents();
        ResourceStack<T> stack = getStackInSlot(contents, index);

        int capacity = getCapacity(index, resource);
        if (stack.isEmpty()) {
            int inserted = Math.min(amount, capacity);
            return set(contents, index, inserted, amount, context, stackFactory.create(resource, inserted));
        }

        if (!resource.equals(stack.resource()) || stack.amount() >= capacity) return 0;

        int inserted = Math.min(amount, capacity - stack.amount());
        stack.grow(inserted);
        return set(contents, index, inserted, amount, context, stack);
    }

    @Override
    public int extract(int index, T resource, int amount, TransactionContext context) {
        Objects.checkIndex(index, size());
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        ResourceContainerContents<T> contents = getContents();
        ResourceStack<T> stack = getStackInSlot(contents, index);

        if (stack.isEmpty() || !resource.equals(stack.resource())) return 0;

        int extracted = Math.min(stack.amount(), amount);
        stack.shrink(extracted);
        return set(contents, index, extracted, amount, context, stack);
    }

    @ApiStatus.OverrideOnly
    protected int set(ResourceContainerContents<T> contents, int index, int changedAmount, int originalAmountRequest, TransactionContext context, ResourceStack<T> stack) {
        ItemResource contextResource = itemContext.getResource();
        ItemStack newStack = contextResource.toStack();
        // Use the max of the content's size and the handler size to avoid truncating
        int contentSize = Math.max(contents.getSlots(), size());
        newStack.set(componentType, contents.with(contentSize, index, stack));
        //using the context, trade out our current container, for the new one.
        int exchangeCount = originalAmountRequest / changedAmount;

        //While it is valid to try to do more than one at a time, we are going to handle just 1 exchange for now.
        int exchangedCount = itemContext.exchange(ItemResource.of(newStack), exchangeCount, context);
        return IntMath.saturatedMultiply(exchangedCount, changedAmount);
    }
}
