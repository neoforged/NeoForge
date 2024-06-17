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
 * <p>
 * This context is primarily used for interaction with {@link ItemCapability ItemCapabilities}. Capabilities with this set
 * as their context will be able to interact with the item as it is stored in the world. You'll be able to insert, extract,
 * and exchange resources with the item. Overflow resources will be automatically inserted into the outer context, allowing
 * for better handling of stacked item capabilities.
 * <p>
 * <h3>Example</h3>
 * Lets take a look at an example of how this context could be utilized:
 * <p>
 * Imagine we have 16 bottles of honey in your inventory. We want to extract 1 bucket's worth of liquid from this stack.
 * First, we create a context of the stack of honey bottles. Lets assume this stack is in your mainhand:
 * <pre>{@code
 * IItemContext context = PlayerContext.ofHand(InteractionHand.MAIN_HAND);
 * }</pre>
 * Next, we get the capability for fluid handling. We can use the shortcut method {@link #getCapability(ItemCapability)}
 * to get the capability without needing to get the stack:
 * <pre>{@code
 * IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
 * }</pre>
 * Now we can extract the fluid from the stack:
 * <pre>{@code
 * FluidResource resource = handler.getResource(0);
 * handler.extract(resource, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE);
 * }</pre>
 * And boom! We've successfully extracted a bucket's worth of honey from our stack of honey bottles.
 * <h3>Example Usage in Handler</h3>
 * Let's take a look at how the handler itself would use the provided context for extraction:
 * <p>
 * On the handler end, we know that each bottle of honey is 250mB, so to extrat 1000 mb we need to empty 4 bottles of honey.
 * We can do this by exchanging the main item in the context with 4 empty bottles:
 * <pre>{@code
 * // other extraction code
 * context.exchange(Items.BOTTLE.defaultResource, 4, TransferAction.EXECUTE);
 * }</pre>
 * This will remove 4 bottles of honey from the stack and replace them with 4 empty bottles. Since the stack still has
 * 12 bottles of honey, the 4 empty bottles will be inserted into the outer context (the player's inventory).
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
