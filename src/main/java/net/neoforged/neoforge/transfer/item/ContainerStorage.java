/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@code Storage<ItemVariant>} for vanilla's {@link Container}.
 *
 * <p><b>Important note:</b> This wrapper assumes that the container owns its slots.
 * If the container does not own its slots, for example because it delegates to another container, this wrapper should not be used!
 *
 * @see InventoryStorage
 * @see WorldlyContainerStorage
 */
public class ContainerStorage implements Storage<ItemVariant> {
    /**
     * Global wrapper concurrent map.
     *
     * <p>Note on thread-safety: we assume that Containers are inherently single-threaded, and no attempt is made at synchronization.
     * However, the access to implementations can happen on multiple threads concurrently, which is why we use a thread-safe wrapper map.
     *
     * <p>A note on GC: weak keys alone are not suitable as the ContainerStorage strongly references the Container.
     * Weak values are suitable, but we have to ensure that the ContainerStorage remains strongly reachable as long as
     * one of the slot wrappers refers to it, which is true thanks to the parent reference of {@link SlotWrapper}.
     */
    // TODO: look into promoting the weak reference to a soft reference if building the wrappers becomes a performance bottleneck.
    // TODO: should have identity semantics?
    private static final Map<Container, ContainerStorage> WRAPPERS = new MapMaker().weakValues().makeMap();

    public static ContainerStorage of(Container container) {
        ContainerStorage storage = WRAPPERS.computeIfAbsent(container, inv -> {
            if (inv instanceof Inventory inventory) {
                return new InventoryStorage(inventory);
            } else {
                return new ContainerStorage(inv);
            }
        });
        // TODO resize?
        return storage;
    }

    private final Container container;
    private final List<SlotWrapper> slotWrappers;
    private final SetChangedParticipant setChangedParticipant = new SetChangedParticipant();

    ContainerStorage(Container container) {
        this.container = container;
        int size = container.getContainerSize();
        this.slotWrappers = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            slotWrappers.add(new SlotWrapper(i));
        }
    }

    @Override
    public int size() {
        return container.getContainerSize();
    }

    @Override
    public long insert(int index, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return slotWrappers.get(index).insert(resource, maxAmount, transaction);
    }

    @Override
    public long extract(int index, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return slotWrappers.get(index).extract(resource, maxAmount, transaction);
    }

    @Override
    public boolean isResourceBlank(int index) {
        return container.getItem(index).isEmpty();
    }

    @Override
    public ItemVariant getResource(int index) {
        return ItemVariant.of(container.getItem(index));
    }

    @Override
    public long getAmount(int index) {
        return container.getItem(index).getCount();
    }

    /**
     * Special cases because vanilla checks the current stack in the following functions (which it shouldn't):
     * <ul>
     * <li>{@link AbstractFurnaceBlockEntity#canPlaceItem(int, ItemStack)}.</li>
     * <li>{@link BrewingStandBlockEntity#canPlaceItem(int, ItemStack)}.</li>
     * </ul>
     */
    @Override
    public long getCapacity(int slot, ItemVariant resource) {
        // Special case to limit buckets to 1 in furnace fuel inputs.
        if (container instanceof AbstractFurnaceBlockEntity && slot == 1 && resource.is(Items.BUCKET)) {
            return 1;
        }

        // Special case to limit brewing stand "bottle inputs" to 1.
        if (container instanceof BrewingStandBlockEntity && slot < 3) {
            return 1;
        }

        return container.getMaxStackSize(resource.innerStack);
    }

    @Override
    public boolean isValid(int index, ItemVariant resource) {
        return container.canPlaceItem(index, resource.innerStack);
    }

    @Override
    public String toString() {
        return "ContainerStorage{" +
                "container=" + container
                + "}";
    }

    // Boolean is used to prevent allocation. Null values are not allowed by SnapshotParticipant.
    private class SetChangedParticipant extends SnapshotParticipant<Boolean> {
        @Override
        protected Boolean createSnapshot() {
            return Boolean.TRUE;
        }

        @Override
        protected void revertToSnapshot(Boolean snapshot) {}

        @Override
        protected void onFinalCommit(Boolean originalState) {
            container.setChanged();
        }
    }

    private class SlotWrapper extends SnapshotParticipant<ItemStack> {
        private final int slot;

        private SlotWrapper(int slot) {
            this.slot = slot;
        }

        private void setStack(ItemStack item) {
            // TODO: special logic inventory
            container.setItem(slot, item);
        }

        private ItemStack getStack() {
            return container.getItem(slot);
        }

        public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
//            StoragePreconditions.notBlankNotNegative(insertedVariant, maxAmount);

            ItemStack currentStack = getStack();

            if ((insertedVariant.matches(currentStack) || currentStack.isEmpty()) && isValid(slot, insertedVariant)) {
                int insertedAmount = (int) Math.min(maxAmount, getCapacity(slot, insertedVariant) - currentStack.getCount());

                if (insertedAmount > 0) {
                    updateSnapshots(transaction);
                    currentStack = getStack();

                    if (currentStack.isEmpty()) {
                        currentStack = insertedVariant.toStack(insertedAmount);
                    } else {
                        currentStack.grow(insertedAmount);
                    }

                    setStack(currentStack);

                    // TODO: special logic inventory onTransfer
                    return insertedAmount;
                }
            }

            return 0;
        }

        public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
//            StoragePreconditions.notBlankNotNegative(variant, maxAmount);

            ItemStack currentStack = getStack();

            if (variant.matches(currentStack)) {
                int extracted = (int) Math.min(currentStack.getCount(), maxAmount);

                if (extracted > 0) {
                    updateSnapshots(transaction);
                    currentStack = getStack();
                    currentStack.shrink(extracted);
                    setStack(currentStack);

                    // TODO: special logic inventory onTransfer
                    return extracted;
                }
            }

            return 0;
        }

        // We override updateSnapshots to also schedule a setChanged call for the backing container.
        @Override
        public void updateSnapshots(TransactionContext transaction) {
            setChangedParticipant.updateSnapshots(transaction);
            super.updateSnapshots(transaction);

            // For chests: also schedule a setChanged call for the other half
            if (container instanceof ChestBlockEntity chest && chest.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));

                if (chest.getLevel().getBlockEntity(otherChestPos) instanceof ChestBlockEntity otherChest) {
                    ContainerStorage.of(otherChest).setChangedParticipant.updateSnapshots(transaction);
                }
            }
        }

        @Override
        protected ItemStack createSnapshot() {
            ItemStack original = getStack();
            setStack(original.copy());
            return original;
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            setStack(snapshot);
        }

        @Override
        protected void onFinalCommit(ItemStack original) {
            // Try to apply the change to the original stack
            ItemStack currentStack = getStack();

            // TODO: special logic inventory
//            if (storage.inventory instanceof SpecialLogicInventory specialLogicInv) {
//                specialLogicInv.fabric_onFinalCommit(slot, original, currentStack);
//            }

            if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
                // Components have changed, we need to copy the stack.
                // TODO: here we need to copy exactly the components from currentStack to original

                // None is empty and the items and components match: just update the amount, and reuse the original stack.
                original.setCount(currentStack.getCount());
                setStack(original);
            } else {
                // Otherwise assume everything was taken from original so empty it.
                original.setCount(0);
            }
        }
    }
}
