/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import com.google.common.collect.MapMaker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EntityEquipmentItemHandler implements IResourceHandler<ItemResource> {
    /**
     * Global wrapper concurrent map.
     *
     * <p>Note on thread-safety: we assume that Entities are inherently single-threaded, and no attempt is made at synchronization.
     * However, the access to implementations can happen on multiple threads concurrently, which is why we use a thread-safe wrapper map.
     *
     * <p>A note on GC: weak keys alone are not suitable as the {@link EntityEquipmentItemHandler} strongly references the Entity.
     * Weak values are suitable, but we have to ensure that the {@link EntityEquipmentItemHandler} remains strongly reachable as int as
     * one of the index wrappers refers to it, which is true thanks to the parent reference of {@link EquipmentSlotSnapshotJournal}.
     *
     * @see WorldlyContainerWrapper
     * @see PlayerInventoryWrapper
     */
    // TODO: look into promoting the weak reference to a soft reference if building the wrappers becomes a performance bottleneck.
    // TODO: should have identity semantics?
    private static final Map<LivingEntity, EntityEquipmentItemHandler> WRAPPERS = new MapMaker().weakValues().makeMap();

    @SafeVarargs
    public static EntityEquipmentItemHandler of(LivingEntity entity, Predicate<EquipmentSlot>... slotFilter) {
        EntityEquipmentItemHandler wrapper = WRAPPERS.computeIfAbsent(entity, EntityEquipmentItemHandler::new);
        wrapper.resize(slotFilter);
        return wrapper;
    }

    protected final LivingEntity entity;

    private int size;

    protected final List<EquipmentSlot> slots = new ArrayList<>();
    protected final ArrayList<ItemStack> internalStacks = new ArrayList<>();
    private final ArrayList<EquipmentSlotSnapshotJournal> snapshots = new ArrayList<>();

    public static boolean isHands(EquipmentSlot slot) {
        return slot.getType() == EquipmentSlot.Type.HAND;
    }

    private void resize(Predicate<EquipmentSlot>[] slotFilter) {
        //Neo: This may be needed to be redone, but this was to ensure that we have not already assigned this instance
//        TODO A maintainer likely should validate this method; but it should be correct.
//        if (size > 0) return; Always resize?

        ArrayList<EquipmentSlot> list = new ArrayList<>();
        for (Predicate<EquipmentSlot> equipmentSlotPredicate : slotFilter) {
            list.addAll(Arrays.stream(EquipmentSlot.values()).filter(equipmentSlotPredicate).toList());
        }
        this.slots.addAll(list);

        size = list.size();
        internalStacks.ensureCapacity(size);
        for (int i = 0; i < size; i++) {
            internalStacks.add(getStackInSlot(i));
        }

        int handlerSize = slots.size();
        snapshots.ensureCapacity(handlerSize);
        for (int i = 0; i < handlerSize; i++) {
            snapshots.add(new EquipmentSlotSnapshotJournal(snapshots.size()));
        }
    }

    private EntityEquipmentItemHandler(LivingEntity entity) {
        this.entity = entity;
    }

    private class EquipmentSlotSnapshotJournal extends SnapshotJournal<ItemStack> {
        private final int index;

        public EquipmentSlotSnapshotJournal(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack createSnapshot() {
            return getStackInSlot(index).copy();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            set(index, ItemResource.of(snapshot), snapshot.getCount());
        }

        @Override
        protected void onCommit(ItemStack originalState) {
            ItemStack itemStack = internalStacks.get(index);
            set(index, ItemResource.of(itemStack), originalState.getCount());
            entity.setItemSlot(validateSlotIndex(index), itemStack);
        }
    }

    protected EquipmentSlot validateSlotIndex(final int slot) {
        if (slot < 0 || slot >= slots.size())
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");

        return slots.get(slot);
    }

    public void set(int index, ItemResource resource, int amount) {
        internalStacks.set(index, resource.toStack(amount));
    }

    @Override
    public int size() {
        return slots.size();
    }

    protected ItemStack getStackInSlot(int slot) {
        return entity.getItemBySlot(validateSlotIndex(slot));
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public int getAmount(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return validateSlotIndex(index).countLimit;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return resource.canEquip(validateSlotIndex(index), entity);
    }

    @Override
    public boolean supportsInsertion(int index) {
        return true;
    }

    @Override
    public boolean supportsExtraction(int index) {
        return true;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;

        return insertBehaviour(index, resource, amount, transaction);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            handled += insertBehaviour(index, resource, amount - handled, transaction);
        }

        return handled;
    }

    private int insertBehaviour(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack stack = getStackInSlot(index);
        if (!isValid(index, resource)) return 0;

        if (stack.isEmpty()) {
            amount = Math.min(amount, getCapacity(index, resource));
            snapshots.get(index).updateSnapshots(transaction);
            set(index, resource, amount);
            return amount;
        }

        if (!resource.is(stack)) return 0;

        amount = Math.min(amount, getCapacity(index, resource) - stack.getCount());
        if (amount > 0) {
            snapshots.get(index).updateSnapshots(transaction);
            set(index, resource, stack.getCount() + amount);
        }
        return amount;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return extractBehaviour(index, resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        int handled = 0;
        int size = size();
        for (int index = 0; index < size; index++) {
            handled += extractBehaviour(index, resource, amount - handled, transaction);
        }

        return handled;
    }

    private int extractBehaviour(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack stack = getStackInSlot(index);
        EquipmentSlot equipmentSlot = validateSlotIndex(index);

        if (stack.isEmpty() || !resource.is(stack) || (resource.canUnequip() && equipmentSlot.isArmor())) {
            return 0;
        }

        int extracted = Math.min(amount, stack.getCount());
        if (extracted > 0) {
            snapshots.get(index).updateSnapshots(transaction);
            int newValue = stack.getCount() - extracted;
            set(index, newValue == 0 ? ItemResource.EMPTY : resource, newValue);
        }
        return extracted;
    }
}
