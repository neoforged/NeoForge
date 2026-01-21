/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Item {@link ResourceHandler} backed by an {@link ItemAccess} for use with bundle-like items.
 * The stacks are stored in a {@link BundleContents} data component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link BundleContents} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 */
public class BundleItemHandler implements ResourceHandler<ItemResource> {
    protected final ItemAccess itemAccess;
    protected final Item validItem;
    protected final DataComponentType<BundleContents> component;

    public BundleItemHandler(ItemAccess itemAccess, DataComponentType<BundleContents> component) {
        this.itemAccess = itemAccess;
        this.validItem = itemAccess.getResource().getItem();
        this.component = component;
    }

    @Override
    public int size() {
        BundleContents contents = itemAccess.getResource().get(component);
        if (contents == null) return 0;
        return contents.size() + (contents.weight().intValue() < 1 ? 1 : 0);
    }

    protected ItemStack getStack(int index) {
        BundleContents contents = itemAccess.getResource().get(component);
        if (contents == null || index >= contents.size()) return ItemStack.EMPTY;
        return contents.getItemUnsafe(index);
    }

    @Override
    public ItemResource getResource(int index) {
        if (itemAccess.getResource().is(validItem)) {
            return ItemResource.of(getStack(index));
        } else {
            return ItemResource.EMPTY;
        }
    }

    @Override
    public long getAmountAsLong(int index) {
        if (itemAccess.getResource().is(validItem)) {
            return (long) itemAccess.getAmount() * getStack(index).getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        if (isValid(index, resource)) {
            return (long) itemAccess.getAmount() * (resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE));
        } else {
            return 0;
        }
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return itemAccess.getResource().is(validItem) && BundleContents.canItemBeInBundle(resource.toStack());
    }

    // TODO: We should potentially refuse the insertion if the bundle contains the item already
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferPreconditions.checkNonNegative(index);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0)
            return 0;
        int extractedPerItem = amount / accessAmount;

        ItemResource accessResource = itemAccess.getResource();
        BundleContents contents = accessResource.get(component);
        if (contents == null || index > contents.size()) return 0;

        BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
        int inserted = 0;

        if (index == contents.size()) {
            inserted = mutable.tryInsert(resource.toStack(extractedPerItem));
        } else {
            ItemStack existing = contents.getItemUnsafe(index);
            if (resource.matches(existing)) {
                inserted += mutable.tryInsert(resource.toStack(extractedPerItem));
            } else {
                return 0;
            }
        }

        return inserted * itemAccess.exchange(accessResource.with(component, mutable.toImmutable()), accessAmount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        TransferPreconditions.checkNonNegative(index);

        int accessAmount = itemAccess.getAmount();
        if (accessAmount == 0)
            return 0;
        int extractedPerItem = amount / accessAmount;

        ItemResource accessResource = itemAccess.getResource();
        BundleContents contents = accessResource.get(component);
        if (contents == null || index >= contents.size()) return 0;

        ItemStack stack = contents.getItemUnsafe(index);
        if (stack.isEmpty() || !resource.matches(stack)) return 0;

        int toExtract = Math.min(extractedPerItem, stack.getCount());
        if (toExtract <= 0) return 0;

        List<ItemStack> items = new ObjectArrayList<>(contents.size());
        contents.items().forEach(items::add);

        if (toExtract == stack.getCount()) {
            items.remove(index);
        } else {
            items.set(index, stack.copy().split(toExtract));
        }

        return toExtract * itemAccess.exchange(accessResource.with(component, new BundleContents(items)), accessAmount, transaction);
    }
}
