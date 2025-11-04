/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.access;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.CarriedSlotWrapper;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Provides access to an item storage location, like a slot in an inventory or a player's hand,
 * such that the current item resource and amount can be read,
 * and the stored item can be changed.
 *
 * <p>This interface is primarily used as the context type {@code C} for {@linkplain ItemCapability item capabilities}.
 * This allows the returned capability instance to modify the current item or even swap out the item entirely,
 * for example to replace an empty bucket by a filled bucket.
 * Use the {@link #getCapability(ItemCapability)} method to query a capability for the item referenced by an item access.
 */
public interface ItemAccess {
    /**
     * Creates an item access instance for interaction with a player's hand.
     *
     * <p>In creative mode, {@link #forInfiniteMaterials(Player, ItemStack)} is used with the hand stack.
     * Otherwise, {@link #forPlayerSlot} with the hand slot is used.
     * This matches the behavior of {@link ItemUtils#createFilledResult}.
     */
    static ItemAccess forPlayerInteraction(Player player, InteractionHand hand) {
        if (player.hasInfiniteMaterials()) {
            return forInfiniteMaterials(player, player.getItemInHand(hand));
        } else {
            return forPlayerSlot(player, switch (hand) {
                case MAIN_HAND -> player.getInventory().getSelectedSlot();
                case OFF_HAND -> Inventory.SLOT_OFFHAND;
            });
        }
    }

    /**
     * Creates an item access instance for a player
     * {@linkplain Player#hasInfiniteMaterials() with infinite materials}, i.e. in creative mode.
     *
     * <p>The passed stack serves as a reference for the contents of the referenced location, and is never modified.
     * Any {@linkplain #insert insertion} is always accepted, but only one item is actually added
     * to the player's inventory, and only if the player does not already have the item.
     * This matches the behavior of {@link ItemUtils#createFilledResult}.
     *
     * @see #forPlayerInteraction(Player, InteractionHand) the recommended method for player interaction
     */
    static ItemAccess forInfiniteMaterials(Player player, ItemStack contents) {
        if (!player.hasInfiniteMaterials()) {
            // Check to avoid accidental usage of the method for players that are not in creative mode.
            // Can be removed in the future if a use case comes up.
            throw new IllegalArgumentException("Player " + player + " does not have infinite materials");
        }
        return new InfiniteMaterialsItemAccess(player, ItemResource.of(contents), contents.getCount());
    }

    /**
     * Creates an item access instance for a player's cursor in a menu.
     */
    static ItemAccess forPlayerCursor(Player player, AbstractContainerMenu menu) {
        return new PlayerItemAccess(PlayerInventoryWrapper.of(player), CarriedSlotWrapper.of(menu));
    }

    /**
     * Creates an item access instance for a specific slot of a player.
     */
    static ItemAccess forPlayerSlot(Player player, int slot) {
        var inventoryWrapper = PlayerInventoryWrapper.of(player);
        return new PlayerItemAccess(inventoryWrapper, RangedResourceHandler.ofSingleIndex(inventoryWrapper, slot));
    }

    /**
     * Creates an item access instance for a specific slot of an item resource handler,
     * with any overflow being sent to the rest of the handler.
     *
     * <p>Overflow on insertion will be sent to the rest of the handler via
     * {@linkplain ResourceHandler#insert(Resource, int, TransactionContext) the slotless insert} method.
     * If this is not desired, use the {@link #forHandlerIndexStrict} method instead.
     */
    static ItemAccess forHandlerIndex(ResourceHandler<ItemResource> handler, int index) {
        return new HandlerItemAccess(handler, index);
    }

    /**
     * Creates an item access instance for a specific slot of an item resource handler.
     *
     * <p>To send overflow on insertion to the rest of the handler, use the {@link #forHandlerIndex} method instead.
     */
    static ItemAccess forHandlerIndexStrict(ResourceHandler<ItemResource> handler, int index) {
        return new HandlerItemAccess(RangedResourceHandler.ofSingleIndex(handler, index), 0);
    }

    /**
     * Creates an item access instance that will mutate a stack directly,
     * possibly changing the components and the count, but never the underlying Item as it's final.
     *
     * <p>This can be used when it is known that the underlying Item will not change.
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    static ItemAccess forStack(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Expected stack to be non-empty.");
        }
        return new StackItemAccess(stack);
    }

    /**
     * Creates a wrapper around this access that allows access to a single item at the time.
     */
    @ApiStatus.NonExtendable
    default ItemAccess oneByOne() {
        return new OneByOneItemAccess(this);
    }

    /**
     * Retrieves a capability from this item location.
     *
     * @implNote This method is a convenient shorthand for {@code location.getResource().toStack().getCapability(capability, location)}.
     */
    @Nullable
    @ApiStatus.NonExtendable
    default <T> T getCapability(ItemCapability<T, ItemAccess> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    /**
     * Returns the currently stored item resource.
     */
    ItemResource getResource();

    /**
     * Returns the currently stored amount of the {@linkplain #getResource current resource}.
     *
     * <p>The returned amount must be <strong>non-negative</strong>.
     * If the {@linkplain #getResource stored resource} is empty, the amount must be 0.
     *
     * @apiNote The returned amount may be larger than the {@linkplain ItemResource#getMaxStackSize() max stack size} of the current resource.
     */
    int getAmount();

    /**
     * Inserts up to the given amount of an item resource into the accessed location.
     * <p>
     * If the inserted item is not stackable with the current item, it may be inserted in a place that is inaccessible
     * by {@link #extract}, such as the player inventory.
     *
     * <p>Changes to the accessed location are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param resource    The resource to insert. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to insert. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return The amount that was inserted. Between {@code 0} (inclusive, nothing was inserted) and {@code amount} (inclusive, everything was inserted).
     * @throws IllegalArgumentException If the resource is empty or the amount is negative. See also {@link TransferPreconditions#checkNonEmptyNonNegative} to help perform this check.
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     *           Note that {@link SnapshotJournal} can serve as the base class for a transaction-aware item access.
     */
    int insert(ItemResource resource, int amount, TransactionContext transaction);

    /**
     * Extracts up to the given amount of an item resource from the accessed location.
     *
     * <p>Changes to the accessed location are made in the context of a {@linkplain Transaction transaction}.
     *
     * @param resource    The resource to extract. <strong>Must be non-empty.</strong>
     * @param amount      The maximum amount of the resource to extract. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @return The amount that was extracted. Between {@code 0} (inclusive, nothing was extracted) and {@code amount} (inclusive, everything was extracted).
     * @throws IllegalArgumentException If the resource is empty or the amount is negative. See also {@link TransferPreconditions#checkNonEmptyNonNegative} to help perform this check.
     * @implSpec Implementations must properly support {@linkplain Transaction transactions}.
     *           Note that {@link SnapshotJournal} can serve as the base class for a transaction-aware item access.
     */
    int extract(ItemResource resource, int amount, TransactionContext transaction);

    /**
     * Exchanges up to the given amount of the {@linkplain #getAmount the current resource} with another.
     *
     * <p>That is, {@link #extract} up to the given amount of the current item,
     * and transactionally {@link #insert} the same amount of the given resource instead.
     *
     * @param newResource The resource of the items after the exchange. <strong>Must be non-empty.</strong>
     * @param amount      The amount of items to exchange. <strong>Must be non-negative.</strong>
     * @param transaction The transaction that this operation is part of.
     * @throws IllegalArgumentException If the given resource is empty or the amount is negative. See also {@link TransferPreconditions#checkNonEmptyNonNegative} to help perform this check.
     * @throws IllegalStateException    If the current resource is empty.
     * @return The amount that was exchanged. Between {@code 0} (inclusive, nothing was exchanged) and {@code amount} (inclusive, everything was exchanged).
     */
    @ApiStatus.NonExtendable
    default int exchange(ItemResource newResource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(newResource, amount);
        var currentResource = getResource();
        TransferPreconditions.checkNonEmpty(currentResource);

        try (Transaction subTransaction = Transaction.open(transaction)) {
            int extracted = extract(currentResource, amount, subTransaction);
            if (extracted > 0) {
                var inserted = insert(newResource, extracted, subTransaction);
                if (inserted == extracted) {
                    subTransaction.commit();
                    return extracted;
                }
            }
        }

        return 0;
    }
}
