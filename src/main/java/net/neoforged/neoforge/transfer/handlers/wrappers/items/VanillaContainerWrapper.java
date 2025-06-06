/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

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
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.UnsafeResourceUtils;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Range;

public class VanillaContainerWrapper implements IResourceHandlerModifiable<ItemResource> {
    /**
     * Global wrapper concurrent map.
     *
     * <p>Note on thread-safety: we assume that Containers are inherently single-threaded, and no attempt is made at synchronization.
     * However, the access to implementations can happen on multiple threads concurrently, which is why we use a thread-safe wrapper map.
     *
     * <p>A note on GC: weak keys alone are not suitable as the ContainerStorage strongly references the Container.
     * Weak values are suitable, but we have to ensure that the ContainerStorage remains strongly reachable as int as
     * one of the index wrappers refers to it, which is true thanks to the parent reference of {@link SlotWrapper}.
     *
     * @see WorldlyContainerWrapper
     * @see InventoryWrapper
     */
    // TODO: look into promoting the weak reference to a soft reference if building the wrappers becomes a performance bottleneck.
    // TODO: should have identity semantics?
    private static final Map<Container, VanillaContainerWrapper> WRAPPERS = new MapMaker().weakValues().makeMap();

    public static VanillaContainerWrapper of(Container container) {
        VanillaContainerWrapper storage = WRAPPERS.computeIfAbsent(container, inv -> {
            if (inv instanceof Inventory inventory) {
                return new InventoryWrapper(inventory);
            } else {
                return new VanillaContainerWrapper(inv);
            }
        });
        storage.resize();
        return storage;
    }

    private final Container container;
    private int size;
    private final List<SlotWrapper> slotWrappers = new ArrayList<>();
    private final SetChangedParticipant setChangedParticipant = new SetChangedParticipant();

    VanillaContainerWrapper(Container container) {
        this.container = container;
    }

    protected Container getContainer() {
        return container;
    }

    private void resize() {
        size = container.getContainerSize();
        while (slotWrappers.size() < size) {
            slotWrappers.add(new SlotWrapper(slotWrappers.size()));
        }
    }

    private SlotWrapper get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Slot index out of bounds: " + index + " (size: " + size + ")");
        }
        return slotWrappers.get(index);
    }

    @Override
    public int size() {
        return size;
    }

    //This is not called from the index-less insert as the checks are done at different times.
    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        if (!isValid(index, resource)) return 0;

        return get(index).insert(0, resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        var handled = 0;
        var size = size();

        for (var index = 0; index < size; index++) {
            if (!isValid(index, resource)) continue;

            handled += get(index).insert(0, resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return get(index).extract(0, resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        var handled = 0;
        var size = size();

        for (var index = 0; index < size; index++) {
            handled += get(index).extract(0, resource, amount - handled, transaction);
            if (handled == amount) break;
        }
        return handled;
    }

    @Override
    public ItemResource getResource(int index) {
        return get(index).getResource(0);
    }

    @Override
    public int getAmount(int index) {
        return get(index).getAmount(0);
    }

    @Override
    public boolean supportsInsertion(int index) {
        return true;
    }

    @Override
    public boolean supportsExtraction(int index) {
        return true;
    }

    /**
     * Special cases because vanilla checks the current stack in the following functions (which it shouldn't):
     * <ul>
     * <li>{@link AbstractFurnaceBlockEntity#canPlaceItem(int, ItemStack)}.</li>
     * <li>{@link BrewingStandBlockEntity#canPlaceItem(int, ItemStack)}.</li>
     * </ul>
     */
    @Override
    public int getCapacity(int index, ItemResource resource) {
        return get(index).getCapacity(0, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return get(index).isValid(0, resource);
    }

    @Override
    public String toString() {
        return "AlternateVanillaContainerWrapper{%s}".formatted(container);
    }

    @Override
    public void set(int index, ItemResource resource, @Range(from = 0, to = ResourceHandlerUtil.MAX) int amount) {
        get(index).set(resource.toStack(amount));
    }

    // Boolean is used to prevent allocation. Null values are not allowed by SnapshotParticipant.
    private class SetChangedParticipant extends SnapshotJournal<Boolean> {
        @Override
        protected Boolean createSnapshot() {
            return Boolean.TRUE;
        }

        @Override
        protected void revertToSnapshot(Boolean snapshot) {}

        @Override
        protected void onCommit(Boolean originalState) {
            container.setChanged();
        }
    }

    private class SlotWrapper extends ItemStackJournal {
        private final int index;

        private SlotWrapper(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack get() {
            return container.getItem(index);
        }

        @Override
        protected void set(ItemStack item) {
            container.setItem(index, item/*, false*/);
        }

        @Override
        protected boolean canInsert(ItemResource resource) {
            return container.canPlaceItem(index, UnsafeResourceUtils.innerStackOf(resource));
        }

        //I assume canExtract doesn't really have the info it needs here for canTakeItem?

        @Override
        protected int getCapacity(ItemResource resource) {
            // Special case to limit buckets to 1 in furnace fuel inputs.
            if (index == 1 && resource.is(Items.BUCKET) && container instanceof AbstractFurnaceBlockEntity) {
                return 1;
            }

            // Special case to limit brewing stand "bottle inputs" to 1.
            if (index < 3 && container instanceof BrewingStandBlockEntity) {
                return 1;
            }

            return container.getMaxStackSize(UnsafeResourceUtils.innerStackOf(resource));
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int ret = super.insert(index, resource, amount, transaction);
            if (ret > 0) {
                container.onTransfer(this.index, transaction);
            }
            return ret;
        }

        @Override
        public int extract(int index, ItemResource variant, int maxAmount, TransactionContext transaction) {
            int ret = super.extract(index, variant, maxAmount, transaction);
            if (ret > 0) {
                container.onTransfer(this.index, transaction);
            }
            return ret;
        }

        // We override updateSnapshots to also schedule a setChanged call for the backing container.
        @Override
        public void updateSnapshots(TransactionContext transaction) {
            setChangedParticipant.updateSnapshots(transaction);
            super.updateSnapshots(transaction);

            // For chests: also schedule a setChanged call for the other half
            if (container instanceof ChestBlockEntity chest && chest.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));
                var level = chest.getLevel();
                if (level != null && level.getBlockEntity(otherChestPos) instanceof ChestBlockEntity otherChest) {
                    VanillaContainerWrapper.of(otherChest).setChangedParticipant.updateSnapshots(transaction);
                }
            }
        }

        @Override
        protected void onCommit(ItemStack original) {
            // Try to apply the change to the original stack
            ItemStack currentStack = get();

            container.onCommit(index, original);

            if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
                if (!ItemStack.matches(currentStack, original)) {
                    // Components have changed, we need to copy the stack.
                    set(currentStack.copy());
                } else {
                    // None is empty and the items and components match: just update the amount, and reuse the original stack.
                    original.setCount(currentStack.getCount());
                    set(original);
                }
            } else {
                // Otherwise assume everything was taken from original so empty it.
                original.setCount(0);
            }
        }
    }
}
