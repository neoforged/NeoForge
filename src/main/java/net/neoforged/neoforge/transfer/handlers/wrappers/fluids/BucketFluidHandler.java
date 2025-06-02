/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/**
 * A handler for empty and filled buckets. This handler makes the assumption that the item in the main context is a bucket
 * and will use an instance of check to determine the fluid resource.
 */
public class BucketFluidHandler implements ISingleResourceHandler<FluidResource> {
    private final IItemContext itemContext;

    public BucketFluidHandler(IItemContext itemContext) {
        this.itemContext = itemContext;
    }

    @Override
    public FluidResource getResource(int index) {
        ItemResource resource = itemContext.getResource();
        if (resource.getInstanceValue() instanceof BucketItem bucket) {
            return bucket.content.defaultResource();
            // Should this check for milk item specifically, tag, or something else. It was an instanceof check before.
        } else if (resource.is(Items.MILK_BUCKET) && NeoForgeMod.MILK.isBound()) {
            return NeoForgeMod.MILK.get().defaultResource();
        }
        return FluidResource.EMPTY;
    }

    @Override
    public int getAmount(int ignoredIndex) {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return !resource.getFilledBucket().isEmpty();
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }

    private ItemResource getFilled(FluidResource resource) {
        return resource.getFilledBucket();
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext context) {
        if (amount < FluidType.BUCKET_VOLUME || resource.isEmpty() || !getResource(0).isEmpty()) return 0;

        int exchanged = itemContext.exchange(getFilled(resource), amount / FluidType.BUCKET_VOLUME, context);
        return exchanged * FluidType.BUCKET_VOLUME;
    }

    private ItemResource getEmpty() {
        return Items.BUCKET.defaultResource();
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext context) {
        if (amount < FluidType.BUCKET_VOLUME || resource.isEmpty() || !resource.equals(getResource(0))) return 0;

        int exchanged = itemContext.exchange(getEmpty(), amount / FluidType.BUCKET_VOLUME, context);
        return exchanged * FluidType.BUCKET_VOLUME;
    }
}
