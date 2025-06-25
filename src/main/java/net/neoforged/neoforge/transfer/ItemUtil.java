/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;

public final class ItemUtil {
    /**
     * Inserts the given {@link ItemStack} into the players inventory. If the player's inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The {@link ItemStack} to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, @Nullable TransactionContext transaction) {
        giveItemToPlayer(player, ItemResource.of(stack), stack.getCount(), transaction);
    }

    /**
     * Inserts the given {@link ItemResource} and {@code amount} into the players inventory. If the player's inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player   The player to give the item to
     * @param resource The {@link ItemResource} to give
     * @param amount   The amount of the resource to give
     */
    public static void giveItemToPlayer(Player player, ItemResource resource, int amount, @Nullable TransactionContext transaction) {
        if (resource.isEmpty()) return;
        IResourceHandler<ItemResource> cap = player.getCapability(Capabilities.ItemHandler.ENTITY);
        if (cap == null) return;

        try (Transaction internalTransaction = TransactionManager.open(transaction)) {
            int inserted = cap.insert(resource, amount, internalTransaction);
            if (inserted == amount) internalTransaction.commit();
        }
    }

    /**
     * Inserts the given {@link ItemStack} into the players inventory.
     * If the player's inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player        The player to give the item to
     * @param stack         The {@link ItemStack} to insert
     * @param preferredSlot slot to start on
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot, @Nullable TransactionContext transaction) {
        giveItemToPlayer(player, ItemResource.of(stack), stack.getCount(), preferredSlot, transaction);
    }

    /**
     * Inserts the given {@link ItemResource} and {@code amount} into the players inventory.
     * If the player's inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player        The player to give the item to
     * @param resource      The {@link ItemResource} to give
     * @param amount        The amount of the resource to give
     * @param preferredSlot slot to start on
     */
    public static void giveItemToPlayer(Player player, ItemResource resource, int amount, int preferredSlot, @Nullable TransactionContext transaction) {
        if (resource.isEmpty()) return;

        PlayerItemContext context = new PlayerItemContext(player, preferredSlot);
        try (Transaction internalTransaction = TransactionManager.open(transaction)) {
            if (amount == context.insert(resource, amount, internalTransaction))
                internalTransaction.commit();
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
     * Drops the contents of a given {@link IResourceHandler} in world
     *
     * @param level   Level to drop the contents in
     * @param pos     Position to drop handlers contents at
     * @param handler The {@link IResourceHandler} that has contents to be dropped
     */
    public static void dropContents(Level level, BlockPos pos, IResourceHandler<ItemResource> handler) {
        dropContents(level, pos.getX(), pos.getY(), pos.getZ(), handler);
    }

    /**
     * Drops the contents of a given {@link IResourceHandler} in world
     *
     * @param level   Level to drop the contents in
     * @param x       The x position to drop handlers contents at
     * @param y       The Y position to drop handlers contents at
     * @param z       The Z position to drop handlers contents at
     * @param handler The {@link IResourceHandler} that has contents to be dropped
     */
    public static void dropContents(Level level, double x, double y, double z, IResourceHandler<ItemResource> handler) {
        int size = handler.size();

        for (int index = 0; index < size; index++) {
            ItemResource resource = handler.getResource(index);
            if (resource.isEmpty()) continue;
            Containers.dropItemStack(level, x, y, z, resource.toStack(handler.getAmount(index)));
        }
    }

    /**
     * Inserts an ItemStack into an {@link IResourceHandler} using stacking logic.
     * ItemStacks will be inserted into filled slot(s) first, then empty slot(s).
     *
     * @param handler     the {@link IResourceHandler} to insert the itemstack into
     * @param stack       the ItemStack to insert
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the stack that was inserted
     * @see ResourceHandlerUtil#insertStacking(IResourceHandler, IResource, int, TransactionContext) ResourceHandlerUtil when already working with ItemResources
     */
    public static int insertStacking(IResourceHandler<ItemResource> handler, ItemStack stack, @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.insertStacking(handler, ItemResource.of(stack), stack.getCount(), transaction);
    }

    /**
     * Inserts an {@link ItemStack} into an {@link IResourceHandler} using non-stacking logic.
     * Resources will be inserted into the first slot(s) that can accept the resource.
     *
     * @param handler     The {@link IResourceHandler} to insert the resource into
     * @param stack       The {@link ItemStack} to insert.
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return the amount of the {@link ItemStack} that was inserted
     */
    public static int insertIndexForced(IResourceHandler<ItemResource> handler, ItemStack stack, @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.insertIndexForced(handler, ItemResource.of(stack), stack.getCount(), transaction);
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param handler     The {@link IResourceHandler} to extract the resource from
     * @param filter      The filter to apply to the resources
     * @param amount      The desired amount of the resource to extract
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return an {@link ItemStack} of the first matching resource of the filter.
     */
    public static ItemStack extractItemStackFiltered(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractFiltered(handler, filter, amount, ItemResource.EMPTY, transaction, ItemResource::toStack);
    }

    /**
     * Extracts the first {@link ItemResource} from an {@link IResourceHandler} that matches the given filter.
     *
     * @param handler     The {@link IResourceHandler} to extract the resource from
     * @param filter      The filter to apply to the resources in the handler.
     * @param amount      The desired amount of the resource to extract
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return a {@link ResourceStack} of the first matching resource for the specified filter; otherwise {@link ItemResource#EMPTY}
     */
    public static ResourceStack<ItemResource> extractResourceStackFiltered(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractFiltered(handler, filter, amount, ItemResource.EMPTY, transaction, ItemResource::withAmount);
    }

    /**
     * Extracts the {@link ItemResource} from an {@link IResourceHandler} that matches the given filter at the specified index.
     *
     * @param index       The index that is being checked in the handler.
     * @param handler     the {@link IResourceHandler} to extract the resource from.
     * @param amount      the desired amount of the resource to extract.
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return an ItemStack that matches both the filter and index specified; otherwise {@link ItemStack#EMPTY}
     */
    public static ItemStack extractItemStackFilteredAtIndex(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int index,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractIndexFiltered(handler, filter, index, amount, ItemResource.EMPTY, transaction, ItemResource::toStack);
    }

    /**
     * Extracts the {@link ItemResource} from an {@link IResourceHandler} that matches the given filter at the specified index.
     *
     * @param index       The index that is being checked in the handler.
     * @param handler     the {@link IResourceHandler} to extract the resource from.
     * @param amount      the desired amount of the resource to extract.
     * @param transaction The transaction context for a given insertion.
     *                    Passing in {@code null} will essentially be the same as doing `execute`,
     *                    whereas passing in a closeable context allows you to choose if it should be committed.
     * @return A {@link ResourceStack} that matches both the filter and index specified; otherwise {@link ItemResource#EMPTY}
     */
    public static ResourceStack<ItemResource> extractResourceStackFilteredAtIndex(
            IResourceHandler<ItemResource> handler,
            Predicate<ItemResource> filter,
            int index,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractIndexFiltered(handler, filter, index, amount, ItemResource.EMPTY, transaction, ItemResource::withAmount);
    }

    /**
     * A helper method to construct a {@link ItemStack} based on what resides at a particular index given a handler
     *
     * @param handler The fluid handler to query.
     * @param index   The index that the fluid is at
     * @return A {@link ItemStack} based on the {@link ItemResource} and {@code amount} at the index
     */
    public static ItemStack getItemStackAt(IResourceHandler<ItemResource> handler, int index) {
        return ResourceHandlerUtil.getStackAt(handler, index, ItemResource::toStack);
    }

    /**
     * A helper method to construct a {@link ResourceStack} based on what resides at a particular index given a handler
     *
     * @param handler The fluid handler to query.
     * @param index   The index that the fluid is at
     * @return A {@link ResourceStack} based on the {@link ItemResource} and {@code amount} at the index
     */
    public static ResourceStack<ItemResource> getResourceStackAt(IResourceHandler<ItemResource> handler, int index) {
        return ResourceHandlerUtil.getStackAt(handler, index, ItemResource::withAmount);
    }
}
