package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.transfer.storage.StoragePreconditions;
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
        protected void readSnapshot(Integer snapshot) {
            // effectively cancel dropping the stacks
            int previousSize = snapshot;

            while (entries.size() > previousSize) {
                entries.remove(entries.size() - 1);
            }
        }

        @Override
        protected void onFinalCommit() {
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
