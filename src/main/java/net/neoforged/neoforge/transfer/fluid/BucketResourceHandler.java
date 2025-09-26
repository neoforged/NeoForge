/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import java.util.Objects;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * A handler for empty and filled buckets.
 * Swaps between empty bucket and filled bucket of the correct type.
 */
public final class BucketResourceHandler extends ItemAccessResourceHandler<FluidResource> {
    public BucketResourceHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.getItem() instanceof BucketItem bucketItem) {
            return FluidResource.of(bucketItem.content);
        } else if (accessResource.is(Items.MILK_BUCKET) && NeoForgeMod.MILK.isBound()) {
            return FluidResource.of(NeoForgeMod.MILK.get());
        } else {
            return FluidResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        var resource = getResourceFrom(accessResource, index);
        return resource.isEmpty() ? 0 : FluidType.BUCKET_VOLUME;
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        if (newAmount == 0) {
            return ItemResource.of(Items.BUCKET);
        } else if (newAmount != FluidType.BUCKET_VOLUME) {
            return ItemResource.EMPTY;
        } else {
            var newStack = newResource.toStack(newAmount);
            return ItemResource.of(newStack.getFluidType().getBucket(newStack));
        }
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        Objects.checkIndex(index, size());
        return FluidType.BUCKET_VOLUME;
    }
}
