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
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.handlers.wrappers.ScopedHandlerWrapper;
import org.jetbrains.annotations.Nullable;

public class WorldlyContainerWrapper extends ContainerWrapper {
    @Nullable
    protected final Direction side;

    public static IResourceHandlerModifiable<ItemResource> of(WorldlyContainer container, @Nullable Direction side) {
        WorldlyContainerWrapper wrapper;

        if (container instanceof AbstractFurnaceBlockEntity)
            wrapper = new Furnace(container, side);
        else if (container instanceof BrewingStandBlockEntity)
            wrapper = new BrewingStand(container, side);
        else
            wrapper = new WorldlyContainerWrapper(container, side);

        return side == null ? wrapper : new ScopedHandlerWrapper.Modifiable<>(wrapper, container.getSlotsForFace(side));
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
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        if (side != null && !getContainer().canPlaceItemThroughFace(index, resource.toStack(), side)) return 0;
        return super.insert(index, resource, amount, action);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (side != null && !getContainer().canTakeItemThroughFace(index, resource.toStack(), side)) return 0;
        return super.extract(index, resource, amount, action);
    }

    public static class Furnace extends WorldlyContainerWrapper {
        protected Furnace(WorldlyContainer container, Direction side) {
            super(container, side);
        }

        @Override
        public int getLimit(int index, ItemResource resource) {
            return index == 1 && resource.is(Items.BUCKET) ? 1 : super.getLimit(index, resource);
        }
    }

    public static class BrewingStand extends WorldlyContainerWrapper {
        protected BrewingStand(WorldlyContainer container, Direction side) {
            super(container, side);
        }

        @Override
        public int getLimit(int index, ItemResource resource) {
            return index < 3 ? 1 : super.getLimit(index, resource);
        }
    }
}
