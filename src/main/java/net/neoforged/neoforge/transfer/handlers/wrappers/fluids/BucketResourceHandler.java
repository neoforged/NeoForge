/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import java.util.Objects;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;

/**
 * A handler for empty and filled buckets. This handler makes the assumption that the item in the main context is a bucket
 * and will use an instance of check to determine the fluid resource.
 * <p>
 * With the item context, handling the scenario of filling or draining multiple stacked buckets is possible.
 */
public final class BucketResourceHandler implements ISingleResourceHandler<FluidResource> {
    private final IItemContext itemContext;

    public BucketResourceHandler(IItemContext itemContext) {
        this.itemContext = itemContext;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());

        ItemResource resource = itemContext.getResource();
        if (resource.getInstanceValue() instanceof BucketItem bucket) {
            return FluidResource.of(bucket.content);
            // Should this check for milk item specifically, tag, or something else. It was an instanceof check before.
        } else if (resource.is(Items.MILK_BUCKET) && NeoForgeMod.MILK.isBound()) {
            return NeoForgeMod.MILK.get().defaultResource();
        }
        return FluidResource.EMPTY;
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getResource(0).isEmpty() ? 0 : FluidType.BUCKET_VOLUME * itemContext.getAmount();
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());

        //Shouldn't be able to overflow given the max stack size is 99, thus the max this can be on a single item should be 99,000.
        // Of course this will differ for other implementations, so care will be needed for those.
        return FluidType.BUCKET_VOLUME * itemContext.getAmount();
    }

    //These are hints to consumers, but given these are on items, the hints are less valuable to be fully stateless
    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public boolean supportsExtraction() {
        return true;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        if (!itemContext.getResource().is(Tags.Items.BUCKETS_EMPTY)) {
            return 0; // can't fill non-empty buckets
        }

        ItemResource filledBucket = resource.getFilledBucket();
        if (filledBucket.isEmpty()) {
            return 0; // the fluid has no associated bucket item
        }

        int bucketsToFill = amount / FluidType.BUCKET_VOLUME;
        if (bucketsToFill == 0) return 0;
        int handled = itemContext.exchange(filledBucket, bucketsToFill, transaction);
        return handled * FluidType.BUCKET_VOLUME;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        FluidResource containedFluid = getResource(0);

        if (!resource.equals(containedFluid)) {
            // Incompatible fluid
            return 0;
        }

        int bucketsToEmpty = amount / FluidType.BUCKET_VOLUME;
        if (bucketsToEmpty == 0) {
            // Nothing to empty
            return 0;
        }
        try (Transaction subTransaction = TransactionManager.open(transaction)) {
            int bucketsEmptied = itemContext.exchange(ItemResource.of(Items.BUCKET), bucketsToEmpty, subTransaction);
            subTransaction.commit();
            return bucketsEmptied * FluidType.BUCKET_VOLUME;
        }
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return !resource.getFilledBucket().isEmpty();
    }
}
