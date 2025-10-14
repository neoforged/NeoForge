/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An implementation of {@code ResourceHandler<ItemResource>} for vanilla's {@link Container}.
 *
 * <p><b>Important note:</b> This wrapper assumes that the container owns its slots.
 * If the container does not own its slots, for example because it delegates to another container, this wrapper should not be used!
 *
 * @see PlayerInventoryWrapper
 * @see WorldlyContainerWrapper
 */
public class VanillaContainerWrapper implements ResourceHandler<ItemResource> {
    /**
     * Global wrapper concurrent map.
     *
     * <p>Note on thread-safety: we assume that Containers are inherently single-threaded, and no attempt is made at synchronization.
     * However, the access to implementations can happen on multiple threads concurrently, which is why we use a thread-safe wrapper map.
     *
     * <p>We use weak keys and values to avoid keeping a strong reference to the Container until the next time the map is cleaned.
     * As long as a slot wrapper is used, there is a strong reference to the outer {@link VanillaContainerWrapper} class,
     * which also references the container. This ensures that the entries remain in the map at least as long as the wrappers are in use.
     *
     * <p>Note that {@link MapMaker#weakKeys()} makes the map use identity semantics for the keys, which is desirable here.
     */
    // TODO: look into promoting the weak reference to a soft reference if building the wrappers becomes a performance bottleneck.
    private static final Map<Container, VanillaContainerWrapper> wrappers = new MapMaker().weakKeys().weakValues().makeMap();

    /**
     * Wraps a vanilla container into a {@link ResourceHandler} of {@link ItemResource}s.
     *
     * <p>If the container is a player {@link Inventory}, use {@link PlayerInventoryWrapper} instead which adds convenience methods for players.
     *
     * <p>If the container is a {@link WorldlyContainer}, use {@link WorldlyContainerWrapper} instead which checks the extra methods of worldy containers.
     */
    public static ResourceHandler<ItemResource> of(Container container) {
        // Only expose a ResourceHandler in this method.
        return internalOf(container);
    }

    static VanillaContainerWrapper internalOf(Container container) {
        VanillaContainerWrapper wrapper = wrappers.computeIfAbsent(container, cont -> {
            if (cont instanceof Inventory inventory) {
                return new PlayerInventoryWrapper(inventory);
            } else {
                return new VanillaContainerWrapper(cont);
            }
        });
        wrapper.resize();
        return wrapper;
    }

    private final Container container;
    int size;
    final List<SlotWrapper> slotWrappers = new ArrayList<>();
    private final RootCommitJournal setChangedJournal;

    VanillaContainerWrapper(Container container) {
        this.container = container;
        this.setChangedJournal = new RootCommitJournal(this::onRootCommit);
    }

    void resize() {
        size = container.getContainerSize();
        while (slotWrappers.size() < size) {
            slotWrappers.add(new SlotWrapper(slotWrappers.size()));
        }
    }

    SlotWrapper getSlotWrapper(int index) {
        Objects.checkIndex(index, size());
        return slotWrappers.get(index);
    }

    void onRootCommit() {
        container.setChanged();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return getSlotWrapper(index).insert(0, resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return getSlotWrapper(index).extract(0, resource, amount, transaction);
    }

    @Override
    public ItemResource getResource(int index) {
        return getSlotWrapper(index).getResource(0);
    }

    @Override
    public long getAmountAsLong(int index) {
        return getSlotWrapper(index).getAmountAsLong(0);
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return getSlotWrapper(index).getCapacityAsLong(0, resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return getSlotWrapper(index).isValid(0, resource);
    }

    @Override
    public String toString() {
        return "VanillaContainerWrapper{container=%s}".formatted(container);
    }

    class SlotWrapper extends ItemStackResourceHandler {
        private final int index;

        SlotWrapper(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack getStack() {
            return container.getItem(index);
        }

        @Override
        protected void setStack(ItemStack item) {
            // We pass insideTransaction = true to disable all non-transactional actions.
            container.setItem(index, item, true);
        }

        @Override
        protected boolean isValid(ItemResource resource) {
            return container.canPlaceItem(index, resource.toStack());
        }

        /**
         * Special cases because vanilla checks the current stack in the following functions (which it shouldn't):
         * <ul>
         * <li>{@link AbstractFurnaceBlockEntity#canPlaceItem(int, ItemStack)}.</li>
         * <li>{@link BrewingStandBlockEntity#canPlaceItem(int, ItemStack)}.</li>
         * </ul>
         *
         * <p>Should these extra checks cause performance issues, we can switch to subclasses.
         */
        @Override
        protected int getCapacity(ItemResource resource) {
            // Special case to limit buckets to 1 in furnace fuel inputs.
            if (index == /* AbstractFurnaceBlockEntity.SLOT_FUEL */ 1 && resource.is(Items.BUCKET) && container instanceof AbstractFurnaceBlockEntity) {
                return 1;
            }

            // Special case to limit brewing stand "bottle inputs" to 1.
            if (index < 3 && container instanceof BrewingStandBlockEntity) {
                return 1;
            }

            return resource.isEmpty() ? container.getMaxStackSize() : container.getMaxStackSize(resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int inserted = super.insert(index, resource, amount, transaction);
            if (inserted > 0) {
                container.onTransfer(this.index, inserted, transaction);
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int extracted = super.extract(index, resource, amount, transaction);
            if (extracted > 0) {
                container.onTransfer(this.index, -extracted, transaction);
            }
            return extracted;
        }

        // We override updateSnapshots to also schedule a setChanged call for the backing container.
        @Override
        public void updateSnapshots(TransactionContext transaction) {
            super.updateSnapshots(transaction);
            setChangedJournal.updateSnapshots(transaction);

            // For chests: also schedule a setChanged call for the other half
            if (container instanceof ChestBlockEntity chest && chest.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
                BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));

                if (chest.getLevel().getBlockEntity(otherChestPos) instanceof ChestBlockEntity otherChest) {
                    VanillaContainerWrapper.internalOf(otherChest).setChangedJournal.updateSnapshots(transaction);
                }
            }
        }

        @Override
        protected void onRootCommit(ItemStack original) {
            // Try to apply the change to the original stack
            ItemStack currentStack = getStack();

            // TODO: we should maybe re-evaluate this, it might make sense to keep it for item capabilities only
            if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
                // The item matches: set the original stack in the container, with updated components and count.
                ((PatchedDataComponentMap) original.getComponents()).restorePatch(currentStack.getComponentsPatch());
                original.setCount(currentStack.getCount());
                setStack(original);
            } else {
                // Otherwise assume everything was taken from original so empty it.
                original.setCount(0);
            }
        }

        @Override
        public String toString() {
            return "vanilla container wrapper[container=" + container + ",slot=" + index + "]";
        }
    }
}
