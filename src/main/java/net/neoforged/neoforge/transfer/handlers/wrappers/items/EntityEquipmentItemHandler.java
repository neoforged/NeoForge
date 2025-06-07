/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class EntityEquipmentItemHandler implements IResourceHandlerModifiable<ItemResource> {
    protected final LivingEntity entity;
    protected final List<EquipmentSlot> slots;
    private final ArrayList<EquipmentSlotSnapshotEntry> snapshots = new ArrayList<>();

    public static boolean isHands(EquipmentSlot slot) {
        return slot.getType() == EquipmentSlot.Type.HAND;
    }

    @SafeVarargs
    public EntityEquipmentItemHandler(LivingEntity entity, Predicate<EquipmentSlot>... slotFilter) {
        this.entity = entity;
        var list = new ArrayList<EquipmentSlot>();
        for (var equipmentSlotPredicate : slotFilter) {
            list.addAll(Arrays.stream(EquipmentSlot.values()).filter(equipmentSlotPredicate).toList());
        }
        this.slots = List.copyOf(list);

        var handlerSize = slots.size();
        snapshots.ensureCapacity(handlerSize);
        for (var i = 0; i < handlerSize; i++) {
            snapshots.add(new EquipmentSlotSnapshotEntry(snapshots.size()));
        }
    }

    private class EquipmentSlotSnapshotEntry extends SnapshotJournal<ItemStack> {
        private final int index;

        public EquipmentSlotSnapshotEntry(int index) {
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
    }

    protected EquipmentSlot validateSlotIndex(final int slot) {
        if (slot < 0 || slot >= slots.size())
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");

        return slots.get(slot);
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        //TODO NEO: probably want to look into handling setting equipment without equip sounds. Right now, the assumption is that this will play even on canceled transactions and reverted snapshots
        entity.setItemSlot(validateSlotIndex(index), resource.toStack(amount));
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
        return validateSlotIndex(index).getCountLimit();
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
        var handled = 0;
        var size = size();
        for (var index = 0; index < size; index++) {
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
        var handled = 0;
        var size = size();
        for (var index = 0; index < size; index++) {
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
