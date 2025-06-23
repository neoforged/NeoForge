/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceFilters;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.ContainerOrHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;

public class VanillaInventoryCodeHooks {
    /**
     * Tries to extract items from an item handler and insert them in the hopper.
     *
     * @param handler target item handler
     * @return {@code true} if we moved an item, {@code false} if we moved no items
     */
    public static boolean extractHook(Hopper dest, IResourceHandler<ItemResource> handler) {
        int size = handler.size();
        int containerSize = dest.getContainerSize();

        for (int i = 0; i < size; i++) {
            ItemStack extractedItemStack;
            //Simulates the extraction
            try (Transaction simulate = TransactionManager.open(null)) {
                extractedItemStack = ItemUtil.extractItemStackFilteredAtIndex(handler, ResourceFilters.any(), i, 1, simulate);
                if (extractedItemStack.isEmpty()) continue; // Next index in the handler
            }

            //Item found, now to iterate the hopper indices
            for (int j = 0; j < containerSize; j++) {
                //Item currently in the hopper slot.
                ItemStack destStack = dest.getItem(j);

                // If the item can't be placed in, skip. This is slightly different from normal vanilla behaviour.
                if (!dest.canPlaceItem(j, extractedItemStack)) continue;

                //Change logic based on if there is something there or not.
                //All simulations are done, we now just need to follow the path based on what is there.
                if (destStack.isEmpty()) {
                    extractedItemStack = ItemUtil.extractItemStackFiltered(handler, ResourceFilters.any(), 1, null);
                    if (extractedItemStack.isEmpty()) continue;//Should be unneeded
                    dest.setItem(j, extractedItemStack);
                    dest.setChanged();
                    return true;
                }

                int destCount = destStack.getCount();
                boolean canStackHoldMore = destCount < destStack.getMaxStackSize();
                boolean canSlotHoldMore = destCount < dest.getMaxStackSize();

                if (canStackHoldMore && canSlotHoldMore && ItemStack.isSameItemSameComponents(extractedItemStack, destStack)) {
                    extractedItemStack = ItemUtil.extractItemStackFiltered(handler, ResourceFilters.any(), 1, null);
                    if (extractedItemStack.isEmpty()) continue;//Should be unneeded
                    destStack.grow(1);
                    dest.setItem(j, destStack);
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

        int size = hopper.getContainerSize();
        for (int i = 0; i < size; ++i) {
            ItemStack item = hopper.getItem(i);
            if (item.isEmpty())
                continue;

            ItemStack originalSlotContents = item.copy();
            int accepted = ItemUtil.insertIndexForced(handler, hopper.removeItem(i, 1), null);
            if (accepted > 0)
                return true;

            hopper.setItem(i, originalSlotContents);
        }

        return false;
    }

    public static ContainerOrHandler getEntityContainerOrHandler(Level level, double x, double y, double z, @Nullable Direction side) {
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
            Entity entity = list.get(level.random.nextInt(list.size()));
            if (entity instanceof Container container) {
                return ContainerOrHandler.container(container);
            }
            IResourceHandler<ItemResource> entityCap = entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side);
            if (entityCap != null) { // Could be null even if it wasn't in the entity predicate above.
                return ContainerOrHandler.handler(entityCap);
            }
        }
        return ContainerOrHandler.EMPTY;
    }
}
