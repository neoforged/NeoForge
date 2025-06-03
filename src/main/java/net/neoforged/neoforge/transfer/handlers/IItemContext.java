/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers;

import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.OneByOneItemContext;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the context of the space an item occupies.
 * <p>
 * This context is primarily used for interaction with {@link ItemCapability ItemCapabilities}. Capabilities with this set
 * as their context will be able to interact with the item as it is stored in the world. You'll be able to insert, extract,
 * and exchange resources with the item. Overflow resources will be automatically inserted into the outer context, allowing
 * for better handling of stacked item capabilities.
 * <p>
 * <h3>Example</h3>
 * Let's take a look at an example of how this context could be utilized:
 * <p>
 * Imagine we have 16 bottles of honey in your inventory. We want to extract 1 bucket's worth of liquid from this stack.
 * First, we create a context of the stack of honey bottles. Let's assume this stack is in your main hand:
 *
 * <pre>{@code
 * IItemContext context = PlayerContext.ofHand(InteractionHand.MAIN_HAND);
 * }</pre>
 *
 * <p>
 * Next, we get the capability for fluid handling. We can use the shortcut method {@link #getCapability(ItemCapability)}
 * to get the capability without needing to get the stack:
 *
 * <pre>{@code
 * IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
 * }</pre>
 *
 * <p>
 * Now we can extract the fluid from the stack:
 *
 * <pre>{@code
 * FluidResource resource = handler.getResource(0);
 * handler.extract(resource, FluidType.BUCKET_VOLUME, transaction);
 * }</pre>
 *
 * <p>
 * And boom! We've successfully extracted a bucket's worth of honey from our stack of honey bottles.
 * <h3>Example Usage in Handler</h3>
 * Let's take a look at how the handler itself would use the provided context for extraction:
 * <p>
 * On the handler end, we know that each bottle of honey is 250mB, so to extract 1000 mB we need to empty 4 bottles of honey.
 * We can do this by exchanging the main item in the context with 4 empty bottles:
 *
 * <pre>{@code
 * // other extraction code
 * context.exchange(Items.BOTTLE.defaultResource, 4, transaction);
 * }</pre>
 *
 * <p>
 * This will remove 4 bottles of honey from the stack and replace them with 4 empty bottles. Since the stack still has
 * 12 bottles of honey, the 4 empty bottles will be inserted into the outer context (the player's inventory).
 */
public interface IItemContext {
    @Nullable
    @ApiStatus.NonExtendable
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

    /**
     * Inserts the given amount of the given resource into the context. Priority is given to the main item, with the
     * remainder being inserted into the outer context.
     *
     * @param resource The resource to insert.
     * @param amount   The amount to insert.
     * @return The amount of the resource that was (or would have been, if simulated) inserted.
     */
    int insert(ItemResource resource, int amount, TransactionContext context);

    /**
     * Extracts the given amount of the given resource from the main item. Extraction will not be performed on the outer
     * context.
     *
     * @param resource The resource to extract.
     * @param amount   The amount to extract.
     * @return The amount of the resource that was (or would have been, if simulated) extracted.
     */
    int extract(ItemResource resource, int amount, TransactionContext context);

    default int exchange(ItemResource resource, int amount, TransactionContext transaction) {
        //do we actually want these checks? Since we likely should be calling them prior to exchanging.
        if (resource.isEmpty() || amount <= 0) return 0;

        try (var subTransaction = Transaction.open(transaction)) {
            int extracted = extract(getResource(), amount, subTransaction);

            if (insert(resource, extracted, subTransaction) == extracted) {
                subTransaction.commit();
                return extracted;
            }
        }

        return 0;
    }

    /**
     * Creates a context object for working with resource handler contained in an item.
     *
     * @param handler The handler containing the item.
     * @param index   The index in {@code handler}, where the item can be found.
     */
    static IItemContext ofIndex(IResourceHandler<ItemResource> handler, int index) {
        return new IItemContext() {
            @Override
            public ItemResource getResource() {
                return index < handler.size() ? handler.getResource(index) : ItemResource.EMPTY;
            }

            @Override
            public int getAmount() {
                return index < handler.size() ? handler.getAmount(index) : 0;
            }

            @Override
            public int insert(ItemResource resource, int amount, TransactionContext transaction) {
                int inserted = handler.insert(index, resource, amount, transaction);
                if (inserted < amount) {
                    inserted += handler.insert(resource, amount - inserted, transaction);
                }
                return inserted;
            }

            @Override
            public int extract(ItemResource itemVariant, int amount, TransactionContext transaction) {
                return handler.extract(index, itemVariant, amount, transaction);
            }
        };
    }

    record ReadOnly(IItemContext context) implements IItemContext {
        @Override
        public ItemResource getResource() {
            return context.getResource();
        }

        @Override
        public int getAmount() {
            return context.getAmount();
        }

        @Override
        public int insert(ItemResource itemVariant, int amount, TransactionContext transaction) {
            try (var subTransaction = Transaction.open(transaction)) {
                return context.insert(itemVariant, amount, subTransaction);
            }
        }

        @Override
        public int extract(ItemResource itemVariant, int amount, TransactionContext transaction) {
            try (var subTransaction = Transaction.open(transaction)) {
                return context.extract(itemVariant, amount, subTransaction);
            }
        }

        @Override
        public int exchange(ItemResource resource, int amount, TransactionContext transaction) {
            try (var subTransaction = Transaction.open(transaction)) {
                return context.exchange(resource, amount, subTransaction);
            }
        }
    }

    /**
     * Creates a context object based on the given context, which will only allow inspection of the contained
     * handler, but no modification. You can still call insert or extract, and as long as the handler is properly setup to handle snapshots, the calls will be reverted
     */
    @ApiStatus.NonExtendable
    default IItemContext asReadOnly() {
        return new ReadOnly(this);
    }

    /**
     * What usecases are there?
     * Creates a wrapper around this context that allows access to a single item at the time.
     */
    @ApiStatus.NonExtendable
    default IItemContext oneByOne() {
        return new OneByOneItemContext(this);
    }
}
