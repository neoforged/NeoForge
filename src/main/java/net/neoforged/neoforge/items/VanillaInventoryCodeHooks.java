/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

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
    public static boolean extractHook(Hopper dest, IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack extractItem = handler.extractItem(i, 1, true);
            if (!extractItem.isEmpty()) {
                for (int j = 0; j < dest.getContainerSize(); j++) {
                    ItemStack destStack = dest.getItem(j);
                    if (dest.canPlaceItem(j, extractItem) && (destStack.isEmpty() || destStack.getCount() < destStack.getMaxStackSize() && destStack.getCount() < dest.getMaxStackSize() && ItemStack.isSameItemSameComponents(extractItem, destStack))) {
                        extractItem = handler.extractItem(i, 1, false);
                        if (destStack.isEmpty())
                            dest.setItem(j, extractItem);
                        else {
                            destStack.grow(1);
                            dest.setItem(j, destStack);
                        }
                        dest.setChanged();
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
    public static boolean insertHook(HopperBlockEntity hopper, IItemHandler itemHandler) {
        if (isFull(itemHandler)) {
            return false;
        }
        for (int i = 0; i < hopper.getContainerSize(); ++i) {
            if (!hopper.getItem(i).isEmpty()) {
                ItemStack originalSlotContents = hopper.getItem(i).copy();
                ItemStack insertStack = hopper.removeItem(i, 1);
                ItemStack remainder = ItemHandlerHelper.insertItem(itemHandler, insertStack, false);

                if (remainder.isEmpty()) {
                    return true;
                }

                hopper.setItem(i, originalSlotContents);
            }
        }

        return false;
    }

    private static boolean isFull(IItemHandler itemHandler) {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || stackInSlot.getCount() < itemHandler.getSlotLimit(slot)) {
                return false;
            }
        }
        return true;
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
            var entity = list.get(level.random.nextInt(list.size()));
            if (entity instanceof Container container) {
                return new ContainerOrHandler(container, null);
            }
            IItemHandler entityCap = entity.getCapability(Capabilities.ItemHandler.ENTITY_AUTOMATION, side);
            if (entityCap != null) { // Could be null even if it wasn't in the entity predicate above.
                return new ContainerOrHandler(null, entityCap);
            }
        }
        return ContainerOrHandler.EMPTY;
    }
}
