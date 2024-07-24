/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class ItemHandlerHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ItemStack insertItem(IItemHandler dest, ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty())
            return stack;

        for (int i = 0; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    /**
     * Inserts the ItemStack into the inventory, filling up already present stacks first.
     * This is equivalent to the behaviour of a player picking up an item.
     * Note: This function stacks items without subtypes with different metadata together.
     */
    public static ItemStack insertItemStacked(IItemHandler inventory, ItemStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty())
            return stack;

        // not stackable -> just insert into a new slot
        if (!stack.isStackable()) {
            return insertItem(inventory, stack, simulate);
        }

        int sizeInventory = inventory.getSlots();

        // go through the inventory and try to fill up already existing items
        for (int i = 0; i < sizeInventory; i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            if (ItemStack.isSameItemSameComponents(slot, stack)) {
                stack = inventory.insertItem(i, stack, simulate);

                if (stack.isEmpty()) {
                    break;
                }
            }
        }

        // insert remainder into empty slots
        if (!stack.isEmpty()) {
            // find empty slot
            for (int i = 0; i < sizeInventory; i++) {
                if (inventory.getStackInSlot(i).isEmpty()) {
                    stack = inventory.insertItem(i, stack, simulate);
                    if (stack.isEmpty()) {
                        break;
                    }
                }
            }
        }

        return stack;
    }

    /** giveItemToPlayer without preferred slot */
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        giveItemToPlayer(player, stack, -1);
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (stack.isEmpty()) return;

        IItemHandler inventory = new PlayerMainInvWrapper(player.getInventory());
        Level level = player.level();

        // try adding it into the inventory
        ItemStack remainder = stack;
        // insert into preferred slot first
        if (preferredSlot >= 0 && preferredSlot < inventory.getSlots()) {
            remainder = inventory.insertItem(preferredSlot, stack, false);
        }
        // then into the inventory in general
        if (!remainder.isEmpty()) {
            remainder = insertItemStacked(inventory, remainder, false);
        }

        // play sound if something got picked up
        if (remainder.isEmpty() || remainder.getCount() != stack.getCount()) {
            level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                    SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
        }

        // drop remaining itemstack into the level
        if (!remainder.isEmpty() && !level.isClientSide) {
            ItemEntity entityitem = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), remainder);
            entityitem.setPickUpDelay(40);
            entityitem.setDeltaMovement(entityitem.getDeltaMovement().multiply(0, 1, 0));

            level.addFreshEntity(entityitem);
        }
    }

    /**
     * This method uses the standard vanilla algorithm to calculate a comparator output for how "full" the inventory is.
     * This method is an adaptation of Container#calcRedstoneFromInventory(IInventory).
     * 
     * @param inv The inventory handler to test.
     * @return A redstone value in the range [0,15] representing how "full" this inventory is.
     */
    public static int calcRedstoneFromInventory(@Nullable IItemHandler inv) {
        if (inv == null) {
            return 0;
        } else {
            int itemsFound = 0;
            float proportion = 0.0F;

            for (int j = 0; j < inv.getSlots(); ++j) {
                ItemStack itemstack = inv.getStackInSlot(j);

                if (!itemstack.isEmpty()) {
                    proportion += (float) itemstack.getCount() / (float) Math.min(inv.getSlotLimit(j), itemstack.getMaxStackSize());
                    ++itemsFound;
                }
            }

            proportion = proportion / (float) inv.getSlots();
            return Mth.floor(proportion * 14.0F) + (itemsFound > 0 ? 1 : 0);
        }
    }

    /**
     * Try to move items from one inventory to another.
     *
     * @param from          source inventory
     * @param to            destination inventory
     * @param filter        a filter to select which items to move; if the filter returns {@code false} for a stack, it won't be moved
     * @param maxAmount     maximum total amount of items to move
     * @param stackInTarget {@code true} to try to stack with existing items in the target inventory,
     *                      {@code false} to insert in the first slot that fits
     * @return the total amount of moved items
     */
    public static int moveItems(
            @Nullable IItemHandler from,
            @Nullable IItemHandler to,
            Predicate<ItemStack> filter,
            int maxAmount,
            boolean stackInTarget) {
        Objects.requireNonNull(filter, "filter");
        if (from == null || to == null || maxAmount <= 0) {
            return 0;
        }

        int totalMoved = 0;

        int fromSlots = from.getSlots();
        slotsLoop:
        for (int i = 0; i < fromSlots; ++i) {
            // Check filter
            if (!filter.test(from.getStackInSlot(i))) {
                continue;
            }

            // Repeated extraction because extractItem limits to max stack size
            while (true) {
                // Simulate extraction
                var available = from.extractItem(i, Integer.MAX_VALUE, true);
                if (available.isEmpty()) {
                    continue slotsLoop;
                }
                int availableCount = available.getCount();

                // Simulate insertion
                var simulationLeftover = stackInTarget ? insertItemStacked(to, available, true) : insertItem(to, available, true);
                int canFit = availableCount - simulationLeftover.getCount();
                if (canFit <= 0) {
                    continue slotsLoop;
                }

                // Perform extraction
                var extracted = from.extractItem(i, canFit, false);
                if (extracted.isEmpty()) {
                    continue slotsLoop;
                }
                int extractedCount = extracted.getCount();

                // Perform insertion
                var leftover = stackInTarget ? insertItemStacked(to, extracted, false) : insertItem(to, extracted, false);
                int movedThisTime = extractedCount - leftover.getCount();
                totalMoved += movedThisTime;

                if (!leftover.isEmpty()) {
                    // Try to give overflow back
                    leftover = from.insertItem(i, leftover, false);

                    if (!leftover.isEmpty()) {
                        String message = String.format(
                                Locale.ROOT,
                                "Trying to move up to %d items from %s to %s, but destination rejected %s that could not be inserted back in the source.",
                                maxAmount, from, to, leftover);
                        if (FMLEnvironment.production) {
                            LOGGER.warn(message);
                        } else {
                            LOGGER.warn(message, new Throwable());
                        }
                    }
                }

                if (movedThisTime <= 0) {
                    break;
                }
            }
        }

        return totalMoved;
    }
}
