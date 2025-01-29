/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VanillaInventoryCodeHooks {
    /**
     * Tries to extract items from an item handler and insert them in the hopper.
     *
     * @param handler target item handler
     * @return {@code true} if we moved an item, {@code false} if we moved no items
     */
    public static boolean extractHook(Hopper dest, IResourceHandler<ItemResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
//            ItemStack extractItem = handler.extractItem(i, 1, true);
            var extracted = ResourceHandlerUtil.extractAny(handler, 1, TransferAction.SIMULATE, ItemResource.NONE);
            //Should likely be "isEmpty" but because it is null we need to check differently then expected
            if (extracted.isEmpty()) continue;

            var extractItem = ItemResource.itemStackOf(extracted);

            for (int j = 0; j < dest.getContainerSize(); j++) {
                ItemStack destStack = dest.getItem(j);

                if (dest.canPlaceItem(j, extractItem) && (destStack.isEmpty() || destStack.getCount() < destStack.getMaxStackSize() && destStack.getCount() < dest.getMaxStackSize() && ItemStack.isSameItemSameComponents(extractItem, destStack))) {
                    extracted = ResourceHandlerUtil.extractAny(handler, 1, TransferAction.EXECUTE, ItemResource.NONE);
                    if(extracted.isEmpty()) continue;//Should be unneeded
                    if (destStack.isEmpty())
                        dest.setItem(j, ItemResource.itemStackOf(extracted));
                    else {
                        destStack.grow(1);
                        dest.setItem(j, destStack);
                    }
                    dest.setChanged();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Tries to insert a hopper's items into an item handler.
     *
     * @param handler target item handler
     * @return {@code true} if we moved an item, {@code false} if we moved no items
     */
    public static boolean insertHook(HopperBlockEntity hopper, IResourceHandler<ItemResource> handler) {
        if (ResourceHandlerUtil.isFull(handler))
            return false;

        for (int i = 0; i < hopper.getContainerSize(); ++i) {
            if (hopper.getItem(i).isEmpty())
                continue;

            ItemStack originalSlotContents = hopper.getItem(i).copy();
            ResourceStack<ItemResource> insertStack = hopper.removeItem(i, 1).immutable();
            int accepted = ResourceHandlerUtil.insertIndexForced(handler, insertStack.resource(), insertStack.amount(), TransferAction.EXECUTE);
            if (accepted > 0)
                return true;

            hopper.setItem(i, originalSlotContents);
        }

        return false;
    }

//    private static boolean isFull(IItemHandler itemHandler) {
//        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
//            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
//            if (stackInSlot.isEmpty() || stackInSlot.getCount() < itemHandler.getSlotLimit(slot)) {
//                return false;
//            }
//        }
//        return true;
//    }

    public static @Nullable Either<Container, IResourceHandler<ItemResource>> getEntityContainerOrHandler(Level level, double x, double y, double z, @Nullable Direction side) {
        List<Entity> list = level.getEntities(
                (Entity) null,
                new AABB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D),
                entity -> {
                    // Note: the isAlive check matches what vanilla does for hoppers in EntitySelector.CONTAINER_ENTITY_SELECTOR
                    if (!entity.isAlive()) {
                        return false;
                    }
                    return entity instanceof Container || entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side) != null;
                });
        if (!list.isEmpty()) {
            var entity = list.get(level.random.nextInt(list.size()));
            if (entity instanceof Container container) {
                return Either.left(container);//new ContainerOrHandler(container, null);
            }
            IResourceHandler<ItemResource> entityCap = entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side);
            if (entityCap != null) { // Could be null even if it wasn't in the entity predicate above.
                return Either.right(entityCap);//new ContainerOrHandler(null, entityCap);
            }
        }
        return null; //Optional.empty();
    }
}
