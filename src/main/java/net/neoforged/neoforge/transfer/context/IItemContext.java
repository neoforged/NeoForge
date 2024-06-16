/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.context;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.items.ItemResource;

/**
 * Represents the context of the space an item occupies.
 */
public interface IItemContext {
    default <T> T getCapability(ItemCapability<T, IItemContext> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    /**
     * @return The resource of the main item.
     */
    ItemResource getResource();

    /**
     * @return The amount of the main item.
     */
    int getAmount();

    // ResourceStack<ItemResource> getMainStack() instead of the 2 methods?

    /**
     * Inserts the given amount of the given resource into the context. Priority is given to the main item, with the
     * remainder being inserted into the outer context.
     *
     * @param resource The resource to insert.
     * @param amount The amount to insert.
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    int insert(ItemResource resource, int amount, TransferAction action);

    /**
     * Extracts the given amount of the given resource from the main item. Extraction will not be performed on the outer
     * context.
     *
     * @param resource The resource to extract.
     * @param amount The amount to extract.
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    int extract(ItemResource resource, int amount, TransferAction action);

    /**
     * Exchanges the given amount of the given resource with the main item. If the amount to be exchanged is less than
     * the given amount, the main stack shrinks and the remainder is inserted into the outer context.
     *
     * @param resource The resource to exchange.
     * @param amount The amount to exchange.
     * @param action   the kind of action being performed. {@link TransferAction#SIMULATE} will simulate the action
     *                 while {@link TransferAction#EXECUTE} will actually perform the action.
     * @return The amount of the resource that was (or would have been, if simulated) exchanged.
     */
    int exchange(ItemResource resource, int amount, TransferAction action);

    /**
     * @return The ItemStack representation of the main item.
     */
    default ItemStack toStack() {
        return getResource().toStack(getAmount());
    }
}
