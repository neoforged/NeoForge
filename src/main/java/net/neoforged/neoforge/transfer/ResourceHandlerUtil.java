/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.function.Predicate;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerContext;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryHandler;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;

public final class ResourceHandlerUtil {
    /**
     * PR NOTES: This is likely going to be a point of contention; do we limit a dev to be locked in at a readable number or do we let them have ~147million more points of data?
     * Realistically, it should be in favor of the player to present them the best options for readability; without sacrificing the functional aspects. The effective max limit is still an integer max,
     * but this is to try to encourage a more human approach for the player's sake. It should be assumed these numbers are very much visible to the player. At the point where 2 billion is not enough for a single call, something has gone awry earlier than just "insert" or "extract".
     * <p>
     * A near max int value intended to be easier to view in normal gameplay. (2E9)
     * While {@link Integer#MAX_VALUE} does tend to make sense as a structural upper bound it is far to often used at the player's expense of reading.
     * Anything breaking past this boundary (whether it is by storing a long internally or otherwise), the main request is to maintain rapid human readability.
     * <p>
     * <strong>Key point:</strong> Human readable
     */
    public static final int PRETTY_MAX_INT = 2000000000;

    /**
     * Checks if an {@link IResourceHandler} is empty.
     *
     * <p>An {@link IResourceHandler} is considered empty if all of its indices
     * contain either a blank resource or have an amount less than or equal to zero.
     *
     * @param handler the {@link IResourceHandler} to check for emptiness
     * @return {@code true} if the {@link IResourceHandler} is empty, {@code false} otherwise
     */
    public static boolean isEmpty(IResourceHandler<? extends IResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            if (!isIndexEmpty(handler, i))
                return false;
        }
        return true;
    }

    /**
     * Checks if an {@link IResourceHandler} is full.
     *
     * <p>An {@code IResourceHandler} is considered full if all of its indices contain resources with amounts
     * greater than or equal to their respective limits.
     *
     * @param handler the {@link IResourceHandler} to check
     * @return {@code true} if the {@link IResourceHandler} is full, {@code false} otherwise
     */
    public static boolean isFull(IResourceHandler<? extends IResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            if (!isIndexFull(handler, i))
                return false;
        }
        return true;
    }

    /**
     * Checks if a specific index of an {@link IResourceHandler} is empty.
     *
     * <p>An index is considered empty if the resource at the specified index is either blank or
     * the amount of the resource is less than or equal to zero.
     *
     * @param handler the {@link IResourceHandler} to check
     * @param index   the index of the resource to check
     * @return {@code true} if the resource at the specified index is empty, {@code false} otherwise
     */
    public static boolean isIndexEmpty(IResourceHandler<? extends IResource> handler, int index) {
        return handler.getResource(index).isEmpty() || handler.getAmount(index) == 0;
    }

    /**
     * Checks if a specific index of an {@link IResourceHandler} is full.
     *
     * <p>An index is considered full if the amount of the resource at the specified index is greater than or equal to
     * the limit of the resource at the specified index.
     *
     * @param handler the {@link IResourceHandler} to check
     * @param index   the index of the resource to check
     * @return {@code true} if the resource at the specified index is full, {@code false} otherwise
     */
    public static <T extends IResource> boolean isIndexFull(IResourceHandler<T> handler, int index) {
        return handler.getAmount(index) >= handler.getCapacity(index, handler.getResource(index));
    }

    public static <T extends IResource> boolean resourceAndCountMatches(IResourceHandler<T> handler, int index, T resource, int amount) {
        return resourceMatches(handler, index, resource) && handler.getAmount(index) == amount;
    }

    public static <T extends IResource> boolean resourceMatches(IResourceHandler<T> handler, int index, T resource) {
        return handler.getResource(index).equals(resource);
    }

    public static <T extends IResource> boolean isValid(IResourceHandler<T> handler, T resource) {
        var size = handler.size();
        for (int i = 0; i < size; i++) {
            if (handler.isValid(i, resource))
                return true;
        }
        return false;
    }

    /**
     * Calculates the redstone signal strength based on the given resource handler. This value is between 0 and 15.
     * This method is based off of {@link AbstractContainerMenu#getRedstoneSignalFromContainer(Container)}
     *
     * @param handler the resource handler to calculate the signal from
     * @param <T>     the type of resource handled by the handler
     * @return the redstone signal strength
     */
    public static <T extends IResource> int getRedstoneSignalFromHandler(IResourceHandler<T> handler) {
        float proportion = 0.0F;
        int size = handler.size();

        for (int index = 0; index < size; ++index) {
            int indexFill = handler.getAmount(index);
            if (indexFill > 0)
                proportion += (float) indexFill / handler.getCapacity(index, handler.getResource(index));
        }

        proportion /= size;
        return Mth.lerpDiscrete(proportion, 0, 15);
    }

    /**
     * Inserts a resource into an {@link IResourceHandler} using stacking logic.
     * Resources will be inserted into filled slot(s) first, then empty slot(s).
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to insert the resource into
     * @param resource the resource to insert
     * @param amount   the desired amount of the resource to insert
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) inserted
     */
    public static <T extends IResource> int insertStacking(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        int inserted = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            if (ResourceHandlerUtil.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount)
                return inserted;
        }

        for (int index = 0; index < size; index++) {
            if (!ResourceHandlerUtil.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount)
                return inserted;
        }

        return inserted;
    }

    /**
     * Inserts a resource into an {@link IResourceHandler} using non-stacking logic.
     * Resources will be inserted into the first slot(s) that can accept the resource.
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to insert the resource into
     * @param resource the resource to insert
     * @param amount   the desired amount of the resource to insert
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) inserted
     */
    public static <T extends IResource> int insertIndexForced(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        int inserted = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount)
                return inserted;
        }

        return inserted;
    }

    /**
     * Extracts a resource from an {@link IResourceHandler}
     * Resources will be extracted from the first slot(s) that contain the resource.
     *
     * @param <T>      the type of resource handled by the handler
     * @param handler  the {@link IResourceHandler} to extract the resource from
     * @param resource the resource to extract
     * @param amount   the desired amount of the resource to extract
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> int extract(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        int extracted = 0;
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount)
                return extracted;
        }

        return extracted;
    }

    public static <T extends IResource> int getTotalAmountOf(IResourceHandler<T> handler, T resource) {
        return extract(handler, resource, ResourceHandlerUtil.PRETTY_MAX_INT, TransferAction.SIMULATE);
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param <T>     the type of resource handled by the handler
     * @param handler the {@link IResourceHandler} to extract the resource from
     * @param filter  the filter to apply to the resources
     * @param amount  the desired amount of the resource to extract
     * @param action  the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> ResourceStack<T> extractFiltered(IResourceHandler<T> handler, Predicate<T> filter, int amount, TransferAction action, T emptyResource) {
        int size = handler.size();
        for (int index = 0; index < size; index++) {
            T resource = handler.getResource(index);
            if (!filter.test(resource)) continue;
            int extract = handler.extract(resource, amount, action);
            if (extract > 0)
                return new ResourceStack<>(resource, extract);
        }
        return new ResourceStack<>(emptyResource, 0);
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that matches the given filter.
     *
     * @param <T>     the type of resource handled by the handler
     * @param handler the {@link IResourceHandler} to extract the resource from
     * @param index   the index to use for the handler
     * @param filter  the filter to apply to the resources
     * @param amount  the desired amount of the resource to extract
     * @param action  the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> ResourceStack<T> extractIndexFiltered(IResourceHandler<T> handler, int index, Predicate<T> filter, int amount, TransferAction action, T emptyResource) {
        T resource = handler.getResource(index);
        if (!filter.test(resource)) return new ResourceStack<>(resource, amount);
        int extract = handler.extract(resource, amount, action);
        if (extract > 0)
            return new ResourceStack<>(resource, extract);
        return new ResourceStack<>(emptyResource, 0);
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that is not blank.
     *
     * @param <T>     the type of resource handled by the handler
     * @param handler the {@link IResourceHandler} to extract the resource from
     * @param amount  the desired amount of the resource to extract
     * @param action  the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> ResourceStack<T> extractIndexedAny(IResourceHandler<T> handler, int index, int amount, TransferAction action, T emptyResource) {
        return extractIndexFiltered(handler, index, Predicate.not(IResource::isEmpty), amount, action, emptyResource);
    }

    /**
     * Extracts the first resource from an {@link IResourceHandler} that is not blank.
     *
     * @param <T>     the type of resource handled by the handler
     * @param handler the {@link IResourceHandler} to extract the resource from
     * @param amount  the desired amount of the resource to extract
     * @param action  the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) extracted
     */
    public static <T extends IResource> ResourceStack<T> extractAny(IResourceHandler<T> handler, int amount, TransferAction action, T emptyResource) {
        return extractFiltered(handler, Predicate.not(IResource::isEmpty), amount, action, emptyResource);
    }

    /**
     * Moves a resource from one {@link IResourceHandler} to another.
     *
     * @param <T>    the type of resource handled by the handlers
     * @param from   the {@link IResourceHandler} to move the resource from
     * @param to     the {@link IResourceHandler} to move the resource to
     * @param amount the desired amount of the resource to move
     * @param action the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *               while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) moved
     */
    public static <T extends IResource> ResourceStack<T> moveFiltered(IResourceHandler<T> from, IResourceHandler<T> to, Predicate<T> filter, int amount, TransferAction action, T emptyResource) {
        for (int index = 0; index < from.size(); index++) {
            T resource = from.getResource(index);
            if (!filter.test(resource)) continue;
            int extracted = from.extract(resource, amount, TransferAction.SIMULATE);
            int inserted = to.insert(resource, extracted, TransferAction.SIMULATE);
            if (extracted == 0 || inserted == 0)
                continue;

            while (extracted != inserted) {
                extracted = from.extract(resource, inserted, TransferAction.SIMULATE);
                inserted = to.insert(resource, extracted, TransferAction.SIMULATE);
                if (extracted == 0 || inserted == 0)
                    break;
            }
            if (inserted == 0)
                continue;

            if (action.isExecuting()) {
                from.extract(resource, inserted, TransferAction.EXECUTE);
                to.insert(resource, inserted, TransferAction.EXECUTE);
            }
            return new ResourceStack<>(resource, inserted);
        }

        return new ResourceStack<>(emptyResource, 0);
    }

    /**
     * Moves a resource from one {@link IResourceHandler} to another.
     *
     * @param <T>    the type of resource handled by the handlers
     * @param from   the {@link IResourceHandler} to move the resource from
     * @param to     the {@link IResourceHandler} to move the resource to
     * @param amount the desired amount of the resource to move
     * @param action the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *               while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return the amount of the resource and the resource itself that was (or would have been, if simulated) moved
     */
    public static <T extends IResource> ResourceStack<T> moveAny(IResourceHandler<T> from, IResourceHandler<T> to, int amount, TransferAction action, T emptyResource) {
        return moveFiltered(from, to, Predicate.not(IResource::isEmpty), amount, action, emptyResource);
    }

    public static <T extends IResource> boolean hasResource(IResourceHandler<T> handler, T resource) {
        return handler.extract(resource, 1, TransferAction.SIMULATE) > 0;
    }

    // Look into if we should use resource or item Stacks here.
    /**
     * Inserts the given {@link ItemStack} into the players inventory. If the inventory can't hold it, the item will be dropped
     * in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The {@link ItemStack} to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;

        PlayerInventoryHandler inventory = new PlayerInventoryHandler(player);
        inventory.insertOrDrop(ItemResource.of(stack), stack.getCount());
    }

    /**
     * Inserts the given {@link ItemStack} into the players inventory.
     * If the inventory can't hold it, the item will be dropped in the world at the players position.
     *
     * @param player The player to give the item to
     * @param stack  The {@link ItemStack} to insert
     */
    public static void giveItemToPlayer(Player player, ItemStack stack, int preferredSlot) {
        if (stack.isEmpty()) return;

        PlayerContext context = new PlayerContext(player, preferredSlot);
        context.insert(ItemResource.of(stack), stack.getCount(), TransferAction.EXECUTE);
    }

    private ResourceHandlerUtil() {}
}
