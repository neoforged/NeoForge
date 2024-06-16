/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids.wrappers;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.Objects;

/**
 * A handler for empty and filled buckets. This handler makes the assumption that the item in the main context is a bucket
 * and will use an instance of check to determine the fluid resource.
 */
public class BucketHandler implements ISingleResourceHandler<FluidResource> {
    private final IItemContext context;

    public BucketHandler(IItemContext context) {
        this.context = context;
    }

    @Override
    public FluidResource getResource() {
        Item item = context.getResource().getItem();
        if (item instanceof BucketItem bucket) {
            return bucket.content.defaultResource;
        } else if (item instanceof MilkBucketItem && NeoForgeMod.MILK.isBound()) {
            return NeoForgeMod.MILK.get().defaultResource;
        }
        return FluidResource.BLANK;
    }

    @Override
    public int getAmount() {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public int getLimit(FluidResource resource) {
        return FluidType.BUCKET_VOLUME;
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return !resource.getFilledBucket().isBlank();
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    private ItemResource getFilled(FluidResource resource) {
        return resource.getFilledBucket();
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (amount < FluidType.BUCKET_VOLUME || resource.isBlank() || !getResource().isBlank()) return 0;

        int exchanged = context.exchange(getFilled(resource), amount / FluidType.BUCKET_VOLUME, action);
        return exchanged * FluidType.BUCKET_VOLUME;
    }

    private ItemResource getEmpty() {
        return Items.BUCKET.defaultResource;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (amount < FluidType.BUCKET_VOLUME || resource.isBlank() || !Objects.equals(resource, getResource())) return 0;

        int exchanged = context.exchange(getEmpty(), amount / FluidType.BUCKET_VOLUME, action);
        return exchanged * FluidType.BUCKET_VOLUME;
    }
}
