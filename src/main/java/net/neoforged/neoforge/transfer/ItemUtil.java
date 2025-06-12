/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.function.Predicate;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

public final class ItemUtil {
    /**
     * Inserts the given {@link ItemStack} into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The {@link ItemStack} to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        giveItemToPlayer(player, ItemResource.of(stack), stack.getCount());
    }

    /**
     * Inserts the given {@link ItemStack} into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player   The player to give the item to
     * @param resource The {@link ItemResource} to give
     * @param amount   The amount of the resource to give
     */
    public static void giveItemToPlayer(Player player, ItemResource resource, int amount) {
        if (resource.isEmpty()) return;
        var cap = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (cap == null) return;

        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            var inserted = cap.insert(resource, amount, transaction);
            if (inserted == amount) transaction.commit();
        }
    }

    /**
     * Inserts the given {@link ItemStack} into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player        The player to give the item to
     * @param stack         The {@link ItemStack} to insert
     * @param preferredSlot slot to start on
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        giveItemToPlayer(player, ItemResource.of(stack), stack.getCount(), preferredSlot);
    }

    /**
     * Inserts the given {@link ItemStack} into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player        The player to give the item to
     * @param resource      The {@link ItemResource} to give
     * @param amount        The amount of the resource to give
     * @param preferredSlot slot to start on
     */
    public static void giveItemToPlayer(Player player, ItemResource resource, int amount, int preferredSlot) {
        if (resource.isEmpty()) return;

        PlayerItemContext context = new PlayerItemContext(player, preferredSlot);
        try (var transaction = Transaction.open(TransactionContext.ROOT)) {
            context.insert(resource, amount, transaction);
        }
    }

    /**
     * Drops an {@link ItemResource} at the player's feet.
     *
     * @param player   The player to drop from
     * @param resource The resource to drop
     * @param amount   The amount of the resource. Any amount greater than the stack size will result in multiple stacks to drop
     */
    public static void dropFromPlayer(Player player, ItemResource resource, int amount) {
        for (ItemStack stack : resource.toStacks(amount)) {
            player.drop(stack, false);
        }
    }

    /**
     * Inserts an ItemStack into an {@link IResourceHandler} using stacking logic. It is advised to use the {@link ResourceHandlerUtil#insertStacking(IResourceHandler, IResource, int, TransactionContext) ResourceHandlerUtil} specific one instead when already working with {@link ItemResource}.
     * ItemStacks will be inserted into filled slot(s) first, then empty slot(s).
     *
     * @param handler     the {@link IResourceHandler} to insert the itemstack into
     * @param stack       the ItemStack to insert
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the stack that was (or would have been, if simulated) inserted
     */
    public static int insertStacking(IResourceHandler<ItemResource> handler, ItemStack stack, @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), transaction);
    }

    public static ItemStack extractItemStackFiltered(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractFiltered(handler, filter, amount, ItemResource.EMPTY, transaction, ItemResource::toStack);
    }

    public static ResourceStack<ItemResource> extractResourceStackFiltered(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractFiltered(handler, filter, amount, ItemResource.EMPTY, transaction, ItemResource::withAmount);
    }

    public static ItemStack extractItemStackFilteredAtIndex(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int index,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractIndexFiltered(handler, filter, index, amount, ItemResource.EMPTY, transaction, ItemResource::toStack);
    }

    public static ResourceStack<ItemResource> extractResourceStackFilteredAtIndex(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int index,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractIndexFiltered(handler, filter, index, amount, ItemResource.EMPTY, transaction, ItemResource::withAmount);
    }
}
