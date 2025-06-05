package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
import net.neoforged.neoforge.transfer.storage.StorageUtil;
import net.neoforged.neoforge.transfer.storage.base.SlotRangeStorage;
import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@code Storage<ItemVariant>} for the {@link Inventory} of a {@link Player}.
 *
 * @see ContainerStorage
 * @see WorldlyContainerStorage
 */
// TODO: do we need to handle the pop time and change broadcasting that PlayerMainInvWrapper was doing?
public final class InventoryStorage extends ContainerStorage {
    public static InventoryStorage of(Player player) {
        return of(player.getInventory());
    }

    public static InventoryStorage of(Inventory inventory) {
        return (InventoryStorage) ContainerStorage.of(inventory);
    }

    private final DroppedItems droppedItems = new DroppedItems();
    private final Inventory inventory;

    InventoryStorage(Inventory inventory) {
        super(inventory);
        this.inventory = inventory;
    }

    /**
     * Retrieves a wrapper for a specific slot.
     */
    public Storage<ItemVariant> getSlot(int slot) {
        StoragePreconditions.checkSlot(slot, size());
        return new SlotRangeStorage<>(this, slot, slot+1);
    }

    /**
     * Retrieves a wrapper for the slot corresponding to the given hand.
     */
    public Storage<ItemVariant> getHandSlot(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> {
                if (Inventory.isHotbarSlot(inventory.selected)) {
                    yield getSlot(inventory.selected);
                } else {
                    throw new RuntimeException("Unexpected player selected slot: " + inventory.selected);
                }
            }
            case OFF_HAND -> getSlot(Inventory.SLOT_OFFHAND);
        };
    }

    /**
     * Retrieves a wrapper around the main slots only.
     */
    public Storage<ItemVariant> getMainSlots() {
        return new SlotRangeStorage<>(this, 0, Inventory.INVENTORY_SIZE);
    }

    /**
     * Inserts items into this player inventory, trying to place items
     * following the logic of {@link Inventory#placeItemBackInInventory}.
     */
    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        long inserted = 0;

        // Stack into the main stack first and the offhand stack second.
        for (InteractionHand hand : InteractionHand.values()) {
            var handSlot = getHandSlot(hand);

            if (handSlot.getResource(0).equals(resource)) {
                inserted += handSlot.insert(resource, maxAmount - inserted, transaction);
                if (inserted == maxAmount) {
                    return inserted;
                }
            }
        }

        // Otherwise insert into the main slots, stacking first.
        inserted += StorageUtil.insertStacking(getMainSlots(), resource, maxAmount - inserted, transaction);

        return inserted;
    }

    /**
     * Transactional version of {@link Inventory#placeItemBackInInventory}:
     * tries to insert as much as possible into the player inventory, and drops the remainder.
     *
     * <p>Another name for this method could have been {@code insertOrDrop}.
     */
    public void placeItemBackInInventory(ItemVariant resource, long amount, TransactionContext transactionContext) {
        long inserted = insert(resource, amount, transactionContext);
        if (inserted < amount) {
            // If we couldn't insert all of it, drop the remainder.
            drop(resource, amount - inserted, true, false, transactionContext);
        }
    }

    /**
     * Transactionally drops an item in the world.
     */
    public void drop(ItemVariant variant, long amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(variant, amount);

        // Drop in the world on the server side (will be synced by the game with the client).
        // Dropping items is server-side only because it involves randomness.
        if (amount > 0 && !inventory.player.level().isClientSide()) {
            droppedItems.addDrop(variant, amount, dropAround, includeThrowerName, transaction);
        }
    }

    @Override
    public String toString() {
        return "InventoryStorage{" +
                "player=" + inventory.player +
                "}";
    }

    private class DroppedItems extends SnapshotParticipant<Integer> {
        final List<Entry> entries = new ArrayList<>();

        void addDrop(ItemVariant variant, long amount, boolean dropAround, boolean includeThrowerName, TransactionContext transaction) {
            updateSnapshots(transaction);
            entries.add(new Entry(variant, amount, dropAround, includeThrowerName));
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
                entries.remove(entries.size() - 1);
            }
        }

        @Override
        protected void onFinalCommit(Integer originalState) {
            // actually drop the stacks
            for (Entry entry : entries) {
                long remainder = entry.amount;

                while (remainder > 0) {
                    int dropped = (int) Math.min(entry.variant.getMaxStackSize(), remainder);
                    inventory.player.drop(entry.variant.toStack(dropped), entry.dropAround, entry.includeThrowerName);
                    remainder -= dropped;
                }
            }

            entries.clear();
        }

        private record Entry(ItemVariant variant, long amount, boolean dropAround, boolean includeThrowerName) {
        }
    }
}
