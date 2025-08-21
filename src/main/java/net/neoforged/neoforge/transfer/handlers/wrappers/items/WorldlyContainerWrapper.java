/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public class WorldlyContainerWrapper implements ResourceHandler<ItemResource> {
    private final WorldlyContainer worldlyContainer;
    private final VanillaContainerWrapper wrappedContainer;
    @Nullable
    private final Direction side;

    public WorldlyContainerWrapper(WorldlyContainer container, @Nullable Direction side) {
        this.worldlyContainer = container;
        this.wrappedContainer = VanillaContainerWrapper.of(container);
        this.side = side;
    }

    private int convertSlot(int slot) {
        if (slot < 0) {
            throw new IndexOutOfBoundsException("Cannot access storage with negative slot index: " + slot);
        }
        if (side == null) {
            return slot;
        }
        int[] slots = worldlyContainer.getSlotsForFace(side);
        if (slot >= slots.length) {
            throw new IndexOutOfBoundsException("Cannot access worldly container on side " + side + " : out of bounds slot index " + slot + " with size " + slots.length);
        }
        return slots[slot];
    }

    @Override
    public int size() {
        return side == null ? worldlyContainer.getContainerSize() : worldlyContainer.getSlotsForFace(side).length;
    }

    @Override
    public ItemResource getResource(int index) {
        return wrappedContainer.getResource(convertSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return wrappedContainer.getAmountAsLong(convertSlot(index));
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return wrappedContainer.getCapacityAsLong(convertSlot(index), resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return wrappedContainer.isValid(convertSlot(index), resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int convertedIndex = convertSlot(index);
        if (!worldlyContainer.canPlaceItemThroughFace(convertedIndex, resource.toStack(), side)) {
            return 0;
        }
        return wrappedContainer.insert(convertedIndex, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int convertedSlot = convertSlot(index);
        if (side != null && !worldlyContainer.canTakeItemThroughFace(convertedSlot, resource.toStack(), side)) {
            return 0;
        }
        return wrappedContainer.extract(convertedSlot, resource, amount, transaction);
    }
}
