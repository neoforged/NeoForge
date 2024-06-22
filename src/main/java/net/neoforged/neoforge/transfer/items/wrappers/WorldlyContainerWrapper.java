/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.ScopedHandlerWrapper;
import net.neoforged.neoforge.transfer.items.ItemResource;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.IntStream;

public class WorldlyContainerWrapper extends ContainerWrapper {
    @Nullable
    protected final Direction side;

    public static WorldlyContainerWrapper of(WorldlyContainer container, @Nullable Direction side) {
        if (container instanceof AbstractFurnaceBlockEntity) {
            return new Furnace(container, side);
        }
        else if (container instanceof BrewingStandBlockEntity) {
            return new BrewingStand(container, side);
        }

        return new WorldlyContainerWrapper(container, side);
    }

    protected WorldlyContainerWrapper(WorldlyContainer container, @Nullable Direction side) {
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
        return side == null ? super.allowsInsertion(index) : IntStream.of(getContainer().getSlotsForFace(side)).anyMatch(i -> i == index);
    }

    @Override
    public boolean allowsExtraction(int index) {
        return side == null ? super.allowsExtraction(index) : Arrays.stream(getContainer().getSlotsForFace(side)).anyMatch(i -> i == index);
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
