/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class WorldlyContainerWrapper extends ContainerWrapper {
    protected final Direction side;

    public static WorldlyContainerWrapper of(WorldlyContainer container, Direction side) {
        if (container instanceof AbstractFurnaceBlockEntity) {
            return new Furnace(container, side);
        } else if (container instanceof BrewingStandBlockEntity) {
            return new BrewingStand(container, side);
        }

        return new WorldlyContainerWrapper(container, side);
    }

    protected WorldlyContainerWrapper(WorldlyContainer container, Direction side) {
        super(container);
        this.side = side;
    }

    @Override
    public WorldlyContainer getContainer() {
        return (WorldlyContainer) super.getContainer();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return side == null ? super.isValid(index, resource) : getContainer().canPlaceItemThroughFace(index, resource.toStack(), side) && super.isValid(index, resource);
    }

    @Override
    public boolean isExtractable(int index, ItemResource resource) {
        return side == null ? super.isExtractable(index, resource) : getContainer().canTakeItemThroughFace(index, resource.toStack(), side) && super.isExtractable(index, resource);
    }

    @Override
    public boolean allowsInsertion(int index) {
        if (side == null) {
            return super.allowsInsertion(index);
        }
        for (int i : getContainer().getSlotsForFace(side)) {
            if (i == index) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean allowsExtraction(int index) {
        if (side == null) {
            return super.allowsExtraction(index);
        }
        for (int i : getContainer().getSlotsForFace(side)) {
            if (i == index)
                return true;
        }
        return false;
    }

    public static class Furnace extends WorldlyContainerWrapper {
        protected Furnace(WorldlyContainer container, Direction side) {
            super(container, side);
        }

        @Override
        public int getCapacity(int index, ItemResource resource) {
            return index == 1 && resource.is(Items.BUCKET) ? 1 : super.getCapacity(index, resource);
        }
    }

    public static class BrewingStand extends WorldlyContainerWrapper {
        protected BrewingStand(WorldlyContainer container, Direction side) {
            super(container, side);
        }

        @Override
        public int getCapacity(int index, ItemResource resource) {
            return index < 3 ? 1 : super.getCapacity(index, resource);
        }
    }
}
