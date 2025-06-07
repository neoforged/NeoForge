/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.UnsafeResourceUtils;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class WorldlyContainerWrapper implements IResourceHandlerModifiable<ItemResource> {
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
            throw new IllegalArgumentException("Cannot access storage with negative slot index: " + slot);
        }
        if (side == null) {
            return slot;
        }
        int[] slots = worldlyContainer.getSlotsForFace(side);
        if (slot >= slots.length) {
            throw new IllegalArgumentException("Cannot access worldly container on side " + side + " : out of bounds slot index " + slot + " with size " + slots.length);
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
    public int getAmount(int index) {
        return wrappedContainer.getAmount(convertSlot(index));
    }

    @Override
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getCapacity(int index, ItemResource resource) {
        return wrappedContainer.getCapacity(convertSlot(index), resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return wrappedContainer.isValid(convertSlot(index), resource);
    }

    @Override
    public boolean supportsInsertion(int index) {
        return wrappedContainer.supportsInsertion(convertSlot(index));
    }

    @Override
    public boolean supportsExtraction(int index) {
        return wrappedContainer.supportsExtraction(convertSlot(index));
    }

    @Override
    public int insert(int index, ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        int convertedIndex = convertSlot(index);
        if (!worldlyContainer.canPlaceItemThroughFace(convertedIndex, UnsafeResourceUtils.innerStackOf(resource), side)) {
            return 0;
        }
        return wrappedContainer.insert(convertedIndex, resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        var size = size();
        var handled = 0;
        for (var index = 0; index < size; index++) {
            handled += insert(index, resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public int extract(int index, ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        int convertedSlot = convertSlot(index);
        if (side != null && !worldlyContainer.canTakeItemThroughFace(convertedSlot, UnsafeResourceUtils.innerStackOf(resource), side)) {
            return 0;
        }
        return wrappedContainer.extract(convertedSlot, resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount, TransactionContext transaction) {
        var size = size();
        var handled = 0;
        for (var index = 0; index < size; index++) {
            handled += extract(index, resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public void set(int index, ItemResource resource, @Range(from = 0, to = Integer.MAX_VALUE) int amount) {
        worldlyContainer.setItem(index, resource.toStack(amount), true);
    }
}
