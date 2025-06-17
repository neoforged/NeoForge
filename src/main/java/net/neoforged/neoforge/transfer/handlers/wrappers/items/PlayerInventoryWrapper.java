/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.RangedResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An implementation of {@code IResourceHandler<ItemResource>} for the {@link Inventory} of a {@link Player}.
 */
public final class PlayerInventoryWrapper extends VanillaContainerWrapper {
    public static PlayerInventoryWrapper of(Player player) {
        return of(player.getInventory());
    }

    public static PlayerInventoryWrapper of(Inventory inventory) {
        return (PlayerInventoryWrapper) VanillaContainerWrapper.of(inventory);
    }

    @Override
    public Inventory getContainer() {
        return (Inventory) super.getContainer();
    }

    @Nullable
    private EquipmentSlot getEquipmentSlot(int slot) {
        if (slot < getContainer().getNonEquipmentItems().size()) return null;
        return Inventory.EQUIPMENT_SLOT_MAPPING.get(slot);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        EquipmentSlot slot = getEquipmentSlot(index);
        return slot != null ? resource.canEquip(slot, inventory.player) : super.isValid(index, resource);
    }

    //TODO We likely need to handle the scenario of can Unequip. Considering something like the enchantment. The resource already has the method
    // we just need the context

    private final DroppedItems droppedItems = new DroppedItems();
    private final Inventory inventory;

    PlayerInventoryWrapper(Inventory inventory) {
        super(inventory);
        this.inventory = inventory;
    }

    /**
     * Retrieves a wrapper for a specific slot.
     */
    public IResourceHandler<ItemResource> getSlot(int slot) {
        Objects.checkIndex(slot, size());
        return new RangedResourceHandler<>(this, slot, slot + 1);
    }

    /**
     * Retrieves a wrapper for the slot corresponding to the given hand.
     */
    public IResourceHandler<ItemResource> getHandSlot(InteractionHand hand) {
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

    public IResourceHandler<ItemResource> getArmorSlotForEquipment(EquipmentSlot slot) {
        return getSlot(slot.getIndex(36));
    }

    public IResourceHandler<ItemResource> getArmor() {
        return new RangedResourceHandler<>(this, EquipmentSlot.FEET.getIndex(36), EquipmentSlot.HEAD.getIndex(36));
    }

    /**
     * Retrieves a wrapper around the main slots only.
     */
    public IResourceHandler<ItemResource> getMainSlots() {
        return new RangedResourceHandler<>(this, 0, Inventory.INVENTORY_SIZE);
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

    public void drop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount))
            return;
        // Drop in the world on the server side (will be synced by the game with the client).
        // Dropping items is server-side only because it involves randomness.
        if (!inventory.player.level().isClientSide()) {
            droppedItems.addDrop(resource, amount, dropAround, includeThrowerName, transaction);
        }
    }

    @Override
    public String toString() {
        return "InventoryWrapper{ %s }".formatted(inventory.player);
    }

    private class DroppedItems extends SnapshotJournal<Integer> {
        final List<Entry> entries = new ArrayList<>();

        void addDrop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
            updateSnapshots(transaction);
            entries.add(new Entry(resource, amount, dropAround, includeThrowerName));
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
        protected void onCommit(Integer originalState) {
            // actually drop the stacks
            for (Entry entry : entries) {
                int remainder = entry.amount;

                while (remainder > 0) {
                    int dropped = Math.min(entry.resource.getMaxStackSize(), remainder);
                    inventory.player.drop(entry.resource.toStack(dropped), entry.dropAround, entry.includeThrowerName);
                    remainder -= dropped;
                }
            }

            entries.clear();
        }

        private record Entry(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName) {}
    }
}
