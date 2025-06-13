/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ItemUtil;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use {@link ItemUtil} instead
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class ItemHandlerHelper {
    /**
     * @deprecated This is now possible to do directly on the {@link net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler ResourceHandler}. No more util needed
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static ItemStack insertItem(@Nullable IResourceHandler<ItemResource> dest, ItemStack stack, boolean simulate) {
        if (dest == null) return stack;
        if (stack.isEmpty()) return stack;
        ItemStack workingStack = stack.copy();
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {

            int inserted = dest.insert(ItemResource.of(stack), stack.getCount(), transaction);
            workingStack.shrink(inserted);
            TransferAction.get(!simulate).commit(transaction);
            return workingStack;
        }
    }

    /**
     * Inserts the ItemStack into the inventory, filling up already present stacks first.
     * This is equivalent to the behaviour of a player picking up an item.
     * Note: This function stacks items without subtypes with different metadata together.
     * 
     * @deprecated Use {@link ItemUtil#insertStacking(IResourceHandler, ItemStack, TransactionContext)} instead with some context
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static ItemStack insertItemStacked(@Nullable IResourceHandler<ItemResource> inventory, ItemStack stack, boolean simulate) {
        if (inventory == null || stack.isEmpty())
            return stack;
        ItemStack workingStack = stack.copy();
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            int inserted = ItemUtil.insertStacking(inventory, stack, transaction);
            workingStack.shrink(inserted);
            TransferAction.get(!simulate).commit(transaction);
            return workingStack;
        }
    }

    /**
     * Inserts the given itemstack into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Use {@link ItemUtil#giveItemToPlayer(Player, ItemStack)} instead
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        ItemUtil.giveItemToPlayer(player, stack);
    }

    /**
     * Inserts the given itemstack into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The itemstack to insert
     * @deprecated Use {@link ItemUtil#giveItemToPlayer(Player, ItemStack, int)} instead
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        ItemUtil.giveItemToPlayer(player, stack, preferredSlot);
    }

    /**
     * This method uses the standard vanilla algorithm to calculate a comparator output for how "full" the inventory is.
     * This method is an adaptation of Container#calcRedstoneFromInventory(IInventory).
     *
     * @param inv The inventory handler to test.
     * @return A redstone value in the range [0,15] representing how "full" this inventory is.
     * @deprecated use {@link ResourceHandlerUtil#getRedstoneSignalStrength(IResourceHandler)} instead and ensuring the passed in handler is not null
     */
    @Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
    public static int calcRedstoneFromInventory(@Nullable IResourceHandler<ItemResource> inv) {
        if (inv == null) return 0;
        return ResourceHandlerUtil.getRedstoneSignalStrength(inv);
    }
}
