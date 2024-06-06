package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.storage.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.storage.wrappers.ScopedHandlerWrapper;

import java.util.Arrays;

public class WorldlyContainerWrapper extends ContainerWrapper {
    protected final Direction side;

    public static IResourceHandlerModifiable<ItemResource> of(WorldlyContainer container, Direction side) {
        WorldlyContainerWrapper wrapper;

        if (container instanceof AbstractFurnaceBlockEntity)
            wrapper = new Furnace(container, side);
        else if (container instanceof BrewingStandBlockEntity)
            wrapper = new BrewingStand(container, side);
        else
            wrapper = new WorldlyContainerWrapper(container, side);

        return new ScopedHandlerWrapper.Modifiable<>(wrapper, container.getSlotsForFace(side));
    }

    public WorldlyContainerWrapper(WorldlyContainer container, Direction side) {
        super(container);
        this.side = side;
    }

    @Override
    public WorldlyContainer getContainer() {
        return (WorldlyContainer) super.getContainer();
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        if (!getContainer().canPlaceItemThroughFace(index, resource.toStack(), side)) return 0;
        return super.insert(index, resource, amount, action);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (!getContainer().canTakeItemThroughFace(index, resource.toStack(), side)) return 0;
        return super.extract(index, resource, amount, action);
    }

    public static class Furnace extends WorldlyContainerWrapper {
        public Furnace(WorldlyContainer container, Direction side) {
            super(container, side);
        }

        @Override
        public int getLimit(int index, ItemResource resource) {
            return index == 1 && resource.is(Items.BUCKET) ? 1 : super.getLimit(index, resource);
        }
    }

    public static class BrewingStand extends WorldlyContainerWrapper {
        public BrewingStand(WorldlyContainer container, Direction side) {
            super(container, side);
        }

        @Override
        public int getLimit(int index, ItemResource resource) {
            return index < 3 ? 1 : super.getLimit(index, resource);
        }
    }
}
