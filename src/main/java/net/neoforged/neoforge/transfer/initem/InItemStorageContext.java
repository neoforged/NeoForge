/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.initem;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.item.InventoryStorage;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.item.base.CarriedSlotStorage;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A context object that is used when a {@link Storage} is
 * located inside an item (or potentially multiple, when it is stacked).
 *
 * <p>This is typically used as the context type for an {@link ItemCapability}.
 * The capability can be queried using the {@link #getCapability} method as a shorthand.
 */
public interface InItemStorageContext {
    /**
     * Returns a context for interaction with a player's hand. This is recommended for item use interactions.
     *
     * <p>In creative mode, {@link #forCreativeInteraction} is used with the hand stack.
     * Otherwise, {@link #ofPlayerHand} is used.
     * This matches the behavior of {@link ItemUtils#createFilledResult}.
     */
    static InItemStorageContext forPlayerInteraction(Player player, InteractionHand hand) {
        if (player.hasInfiniteMaterials()) {
            return forCreativeInteraction(player, player.getItemInHand(hand));
        } else {
            return ofPlayerHand(player, hand);
        }
    }

    /**
     * Returns a context for creative interaction.
     *
     * <p>The stack will never be modified, and any updated stack will only be added to the player's inventory
     * if the player's inventory doesn't already contain it.
     * This matches the creative behavior of {@link ItemUtils#createFilledResult}.
     */
    static InItemStorageContext forCreativeInteraction(Player player, ItemStack interactingStack) {
        return new CreativePlayerStorageContext(InventoryStorage.of(player), ItemVariant.of(interactingStack), interactingStack.getCount());
    }

    /**
     * Return a context for the passed player's hand.
     */
    static InItemStorageContext ofPlayerHand(Player player, InteractionHand hand) {
        return new PlayerStorageContext(player, InventoryStorage.of(player).getHandSlot(hand));
    }

    /**
     * Return a context for the passed player's cursor slot. This is recommended for menu click interactions.
     */
    static InItemStorageContext ofPlayerCursor(Player player, AbstractContainerMenu menu) {
        return ofPlayerSlot(player, CarriedSlotStorage.of(menu));
    }

    /**
     * Return a context for a slot, with the passed player as fallback.
     * Only slot 0 of the passed storage is ever used.
     */
    static InItemStorageContext ofPlayerSlot(Player player, Storage<ItemVariant> slot) {
        return new PlayerStorageContext(player, slot);
    }

    /**
     * Creates a context object based on the given itemstack, which will only allow inspection of the contained
     * storage, but no modification.
     */
    static InItemStorageContext ofReadOnly(ItemStack stack) {
        return new ReadOnlyItemStorageContext(ItemVariant.of(stack), stack.getCount());
    }

    /**
     * Creates a context object for working with storage contained in an item that is itself stored in the slot
     * of a storage.
     *
     * <p>Overflow will be sent to the rest of the storage via the slotless {@link Storage#insert(Object, long, TransactionContext)} method.
     *
     * @param storage The storage containing the item.
     * @param slot    The slot in {@code storage}, where the item can be found.
     */
    static InItemStorageContext ofStorageSlot(Storage<ItemVariant> storage, int slot) {
        return new StorageSlotContext(storage, slot);
    }

    /**
     * Creates a wrapper around this context that allows access to a single item at the time.
     */
    @ApiStatus.NonExtendable
    default InItemStorageContext oneByOne() {
        return new OneByOneStorageContext(this);
    }

    /**
     * Retrieve a capability for the contents of this context.
     */
    @ApiStatus.NonExtendable
    @Nullable
    default <T> T getCapability(ItemCapability<T, InItemStorageContext> capability) {
        return getCurrent().toStack().getCapability(capability, this);
    }

    /**
     * @return The current variant of the item that contains the storage that should be accessed. This may
     *         be blank or become blank if {@link #extract} is used.
     */
    ItemVariant getCurrent();

    /**
     * @return The current amount of {@linkplain #getCurrent() the item} containing the storage. Always returns 0 if
     *         {@linkplain #getCurrent() the current item} is blank.
     */
    long getCurrentAmount();

    /**
     * @return True if any attempts to modify the items in this context will not work. Storage implementations can
     *         use this to return appropriate information from {@link Storage#supportsInsertion()}
     *         or {@link Storage#supportsExtraction()}.
     */
    default boolean supportsModification() {
        return true;
    }

    /**
     * Transactionally insert an item into the "slot" representing the location of the item containing the storage.
     * <p>
     * If the inserted item is not stackable with the current item, it may be inserted in a place that is inaccessible
     * for {@link #extract}, such as the player inventory.
     *
     * @see Storage#insert
     */
    long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction);

    /**
     * Transactionally extract some of the current item.
     *
     * @see Storage#extract
     */
    long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction);

    /**
     * Try to exchange as many items as possible of {@linkplain #getCurrent() the current item} with another.
     * <p>
     * That is, {@link #extract} the given amount of the current item, and transactionally {@link #insert} the same amount of the given variant instead.
     * <p>
     * This method is particularly useful if the contained storage is being mutated and the resulting changes to the item
     * should be persisted.
     *
     * @param newVariant  The variant of the items after the conversion. May not be blank.
     * @param maxAmount   The maximum amount of items to convert. May not be negative.
     * @param transaction The transaction this operation is part of.
     * @return A non-negative integer not greater than maxAmount: the amount that was transformed.
     */
    default long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);

        try (var nested = Transaction.open(transaction)) {
            long extracted = extract(getCurrent(), maxAmount, nested);

            if (insert(newVariant, extracted, nested) == extracted) {
                nested.commit();
                return extracted;
            }
        }

        return 0;
    }
}
