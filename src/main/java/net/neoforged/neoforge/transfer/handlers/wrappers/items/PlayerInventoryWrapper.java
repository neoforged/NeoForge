/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.RangedResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@code ResourceHandler<ItemResource>} for the {@link Inventory} of a {@link Player}.
 *
 * @see VanillaContainerWrapper
 * @see WorldlyContainerWrapper
 */
// TODO: do we want to "animate" inserted items just like in PlayerMainInvWrapper?
public final class PlayerInventoryWrapper extends VanillaContainerWrapper {
    /**
     * Gets the inventory wrapper for a {@link Player}.
     */
    public static PlayerInventoryWrapper of(Player player) {
        return of(player.getInventory());
    }

    /**
     * Gets the inventory wrapper for a player's {@link Inventory}.
     */
    public static PlayerInventoryWrapper of(Inventory inventory) {
        return (PlayerInventoryWrapper) VanillaContainerWrapper.of(inventory);
    }

    private final DroppedItems droppedItems = new DroppedItems();
    private final Inventory inventory;

    PlayerInventoryWrapper(Inventory inventory) {
        super(inventory);
        this.inventory = inventory;
    }

    /**
     * Retrieves a wrapper for a specific slot.
     */
    public ResourceHandler<ItemResource> getSlot(int slot) {
        return RangedResourceHandler.ofSingleIndex(this, slot);
    }

    /**
     * Retrieves a wrapper for the slot corresponding to the given hand.
     */
    public ResourceHandler<ItemResource> getHandSlot(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> {
                if (Inventory.isHotbarSlot(inventory.getSelectedSlot())) {
                    yield getSlot(inventory.getSelectedSlot());
                } else {
                    throw new RuntimeException("Unexpected player selected slot: " + inventory.getSelectedSlot());
                }
            }
            case OFF_HAND -> getSlot(Inventory.SLOT_OFFHAND);
        };
    }

    // TODO: weird, this will silently fail for equipment slots that are not armor slots?
    public ResourceHandler<ItemResource> getArmorSlotForEquipment(EquipmentSlot slot) {
        return getSlot(slot.getIndex(Inventory.INVENTORY_SIZE));
    }

    public ResourceHandler<ItemResource> getArmor() {
        return RangedResourceHandler.of(this, EquipmentSlot.FEET.getIndex(Inventory.INVENTORY_SIZE), EquipmentSlot.HEAD.getIndex(Inventory.INVENTORY_SIZE));
    }

    /**
     * Retrieves a wrapper around the main slots only.
     */
    public ResourceHandler<ItemResource> getMainSlots() {
        return RangedResourceHandler.of(this, 0, Inventory.INVENTORY_SIZE);
    }

    /**
     * Inserts items into this player inventory, trying to place items
     * following the logic of {@link Inventory#placeItemBackInInventory}.
     */
    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        int inserted = 0;

        // Stack into the main stack first and the offhand stack second.
        for (InteractionHand hand : InteractionHand.values()) {
            var handSlot = getHandSlot(hand);

            if (handSlot.getResource(0).equals(resource)) {
                inserted += handSlot.insert(resource, amount - inserted, transaction);
                if (inserted == amount) {
                    return inserted;
                }
            }
        }

        // Otherwise insert into the main slots, stacking first.
        inserted += ResourceHandlerUtil.insertStacking(getMainSlots(), resource, amount - inserted, transaction);

        return inserted;
    }

    /**
     * Transactional version of {@link Inventory#placeItemBackInInventory}:
     * tries to insert as much as possible into the player inventory, and drops the remainder.
     *
     * <p>Another name for this method could have been {@code insertOrDrop}.
     */
    public void placeItemBackInInventory(ItemResource resource, int amount, TransactionContext transactionContext) {
        int inserted = insert(resource, amount, transactionContext);
        if (inserted < amount) {
            // If we couldn't insert all of it, drop the remainder.
            drop(resource, amount - inserted, true, false, transactionContext);
        }
    }

    /**
     * Transactionally drops an item in the world.
     */
    public void drop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (amount == 0) return;

        // Drop in the world on the server side (will be synced by the game with the client).
        // Dropping items is server-side only because it involves randomness.
        if (!inventory.player.level().isClientSide()) {
            droppedItems.addDrop(resource, amount, dropAround, includeThrowerName, transaction);
        }
    }

    // TODO: suspicious method
    @Override
    public Inventory getContainer() {
        return (Inventory) super.getContainer();
    }

    @Nullable
    private EquipmentSlot getEquipmentSlot(int slot) {
        if (slot < getContainer().getNonEquipmentItems().size()) return null;
        return Inventory.EQUIPMENT_SLOT_MAPPING.get(slot);
    }

    // TODO: overriding isValid is weird given that the InventoryWrapper delegates everything to the SlotWrapper
    // TODO: there might be a way to share the slot identity with the entity equipment wrapper?
    @Override
    public boolean isValid(int index, ItemResource resource) {
        EquipmentSlot slot = getEquipmentSlot(index);
        if (resource.isEmpty()) return true;
        // TODO: restore canEquip?
//        return slot != null ? resource.canEquip(slot, inventory.player) : super.isValid(index, resource);
        return true;
    }

    //TODO We likely need to handle the scenario of can Unequip. Considering something like the enchantment. The resource already has the method
    // we just need the context

    @Override
    public String toString() {
        return "PlayerInventoryWrapper{player=%s}".formatted(inventory.player);
    }

    private class DroppedItems extends SnapshotJournal<Integer> {
        final List<DropInfo> entries = new ArrayList<>();

        void addDrop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
            updateSnapshots(transaction);
            entries.add(new DropInfo(resource, amount, dropAround, includeThrowerName));
        }

        @Override
        protected Integer createSnapshot() {
            return entries.size();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            // effectively cancel dropping the stacks
            int previousSize = snapshot;

            while (entries.size() > previousSize) {
                entries.removeLast();
            }
        }

        @Override
        protected void onRootCommit(Integer originalState) {
            // actually drop the stacks
            for (DropInfo dropInfo : entries) {
                int remainder = dropInfo.amount;

                var maxStackSize = dropInfo.resource.getMaxStackSize();
                while (remainder > 0) {
                    int dropped = Math.min(maxStackSize, remainder);
                    inventory.player.drop(dropInfo.resource.toStack(dropped), dropInfo.dropAround, dropInfo.includeThrowerName);
                    remainder -= dropped;
                }
            }
        }

        private record DropInfo(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName) {}
    }
}
