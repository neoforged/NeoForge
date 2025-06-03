/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@code IResourceHandler<ItemResource>} for the {@link Inventory} of a {@link Player}.
 */
public final class InventoryWrapper extends VanillaContainerWrapper {
    public static InventoryWrapper of(Player player) {
        return of(player.getInventory());
    }

    public static InventoryWrapper of(Inventory inventory) {
        return (InventoryWrapper) VanillaContainerWrapper.of(inventory);
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

    private final DroppedItems droppedItems = new DroppedItems();
    private final Inventory inventory;

    InventoryWrapper(Inventory inventory) {
        super(inventory);
        this.inventory = inventory;
    }

    public void drop(ItemResource resource, int amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0)
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
