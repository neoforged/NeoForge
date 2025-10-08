/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

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
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class VanillaInventoryCodeHooks {
    /**
     * Tries to extract items from an item handler and insert them in the hopper.
     *
     * @param handler target item handler
     * @return {@code true} if we moved an item, {@code false} if we moved no items
     */
    public static boolean extractHook(Hopper dest, ResourceHandler<ItemResource> handler) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            var itemResource = handler.getResource(index);
            if (itemResource.isEmpty()) continue;
            try (var tx = Transaction.openRoot()) {
                int extracted = handler.extract(index, itemResource, 1, tx);
                if (extracted == 0) {
                    continue;
                }

                var extractedStack = itemResource.toStack();
                int destSize = dest.getContainerSize();
                for (int j = 0; j < destSize; j++) {
                    ItemStack destStack = dest.getItem(j);
                    if (dest.canPlaceItem(j, extractedStack) && (destStack.isEmpty() || destStack.getCount() < destStack.getMaxStackSize() && destStack.getCount() < dest.getMaxStackSize() && itemResource.matches(destStack))) {
                        if (destStack.isEmpty()) {
                            dest.setItem(j, extractedStack);
                        } else {
                            destStack.grow(1);
                            dest.setItem(j, destStack);
                        }
                        dest.setChanged();
                        tx.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Tries to insert a hopper's items into an item handler.
     *
     * @param itemHandler target item handler
     * @return {@code true} if we moved an item, {@code false} if we moved no items
     */
    public static boolean insertHook(HopperBlockEntity hopper, ResourceHandler<ItemResource> itemHandler) {
        if (ResourceHandlerUtil.isFull(itemHandler)) {
            return false;
        }
        try (var tx = Transaction.openRoot()) {
            int size = hopper.getContainerSize();
            for (int i = 0; i < size; ++i) {
                var hopperItem = hopper.getItem(i);
                if (hopperItem.isEmpty()) {
                    continue;
                }

                ItemStack originalSlotContents = hopperItem.copy();
                ItemStack insertStack = hopper.removeItem(i, 1);

                if (itemHandler.insert(ItemResource.of(insertStack), 1, tx) == 1) {
                    tx.commit();
                    return true;
                } else {
                    // The stack was removed from the hopper but could not be inserted, so add it back
                    hopper.setItem(i, originalSlotContents);
                }
            }
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
                    return entity instanceof Container || entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, side) != null;
                });
        if (!list.isEmpty()) {
            var entity = list.get(level.random.nextInt(list.size()));
            if (entity instanceof Container container) {
                return new ContainerOrHandler(container, null);
            }
            ResourceHandler<ItemResource> entityCap = entity.getCapability(Capabilities.Item.ENTITY_AUTOMATION, side);
            if (entityCap != null) { // Could be null even if it wasn't in the entity predicate above.
                return new ContainerOrHandler(null, entityCap);
            }
        }
        return ContainerOrHandler.EMPTY;
    }
}
