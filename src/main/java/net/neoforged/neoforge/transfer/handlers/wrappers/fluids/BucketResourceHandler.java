/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.fluids;

import com.google.common.math.IntMath;
import java.util.Objects;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.itemaccess.ItemAccess;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A handler for empty and filled buckets. This handler makes the assumption that the item in the main context is a bucket
 * and will use an instance of check to determine the fluid resource.
 * <p>
 * With the item context, handling the scenario of filling or draining multiple stacked buckets is possible.
 */
public final class BucketResourceHandler implements ResourceHandler<FluidResource> {
    private final ItemAccess itemAccess;

    public BucketResourceHandler(ItemAccess itemAccess) {
        this.itemAccess = itemAccess;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBucket() {
        ItemResource bucket = itemAccess.getResource();
        if (bucket.is(Items.MILK_BUCKET))
            return NeoForgeMod.MILK.isBound();
        return bucket.getItem() instanceof BucketItem;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        Objects.checkIndex(index, size());

        ItemResource resource = itemAccess.getResource();
        if (resource.getItem() instanceof BucketItem bucket)
            return FluidResource.of(bucket.content);
        if (resource.is(Items.MILK_BUCKET) && NeoForgeMod.MILK.isBound())
            return FluidResource.of(NeoForgeMod.MILK.get());

        return FluidResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        if (!isBucket() || getResource(index).isEmpty()) return 0;
        return (long) FluidType.BUCKET_VOLUME * itemAccess.getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        if (!isBucket()) return 0;
        if (resource.isEmpty())
            return (long) FluidType.BUCKET_VOLUME * itemAccess.getAmount();

        FluidResource fluid = getResource(0);
        if (!fluid.isEmpty() && !resource.equals(fluid)) return 0;
        return (long) FluidType.BUCKET_VOLUME * itemAccess.getAmount();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        if (!isBucket()) return 0;
        if (!itemAccess.getResource().is(Tags.Items.BUCKETS_EMPTY)) {
            return 0; // can't fill non-empty buckets
        }

        ItemResource filledBucket = ItemResource.of(FluidUtil.getFilledBucket(resource.toStack(1)));
        if (filledBucket.isEmpty()) return 0; // the fluid has no associated bucket item

        int bucketsToFill = amount / FluidType.BUCKET_VOLUME;
        if (bucketsToFill == 0) return 0;
        int handled = itemAccess.exchange(filledBucket, bucketsToFill, transaction);
        return IntMath.saturatedMultiply(handled, FluidType.BUCKET_VOLUME);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        if (!isBucket()) return 0;
        FluidResource containedFluid = getResource(0);

        if (!resource.equals(containedFluid)) return 0; // Incompatible fluid

        int bucketsToEmpty = amount / FluidType.BUCKET_VOLUME;
        if (bucketsToEmpty == 0) return 0; // Nothing to empty

        int bucketsEmptied = itemAccess.exchange(ItemResource.of(Items.BUCKET), bucketsToEmpty, transaction);
        return IntMath.saturatedMultiply(bucketsEmptied, FluidType.BUCKET_VOLUME);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return true;
    }
}
