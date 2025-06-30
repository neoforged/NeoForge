/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.TransferCharacteristics;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * An attachment to a living entity that allows resource handler access and mutation to its equipment.
 * Multiple accesses to the same entity's capability will provide this as only the first request will
 * instantiate a new instance.
 */
public final class LivingEntityEquipmentHandlerAttachment implements IResourceHandler<ItemResource> {
    private final List<EquipmentSlot> slots;
    private final ArrayList<ItemStack> internalStacks;
    private final ArrayList<Journal> snapshots = new ArrayList<>();
    private final int size;
    private final LivingEntity entity;

    /**
     * A non-serialized builder of the living entity.
     */
    public static AttachmentType.Builder<LivingEntityEquipmentHandlerAttachment> BUILDER = AttachmentType.builder(LivingEntityEquipmentHandlerAttachment::new);

    private LivingEntityEquipmentHandlerAttachment(IAttachmentHolder holder) {
        if (!(holder instanceof LivingEntity livingEntity)) {
            throw new IllegalArgumentException("Holder must be a LivingEntity");
        }

        this.entity = livingEntity;
        slots = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (entity.canUseSlot(slot))
                slots.add(slot);
        }

        size = slots.size();
        internalStacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            internalStacks.add(getStackInSlot(i));
        }

        int handlerSize = slots.size();
        snapshots.ensureCapacity(handlerSize);
        snapshots.clear();
        for (int i = 0; i < handlerSize; i++) {
            snapshots.add(new Journal(snapshots.size()));
        }
    }

    @Override
    public int size() {
        return size;
    }

    private ItemStack getStackInSlot(int slot) {
        Objects.checkIndex(slot, size());
        return entity.getItemBySlot(slots.get(slot));
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public int getAmount(int index) {
        Objects.checkIndex(index, size());
        return getStackInSlot(index).getCount();
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return slots.get(index).countLimit;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        if (resource.isEmpty()) return true;
        return resource.canEquip(slots.get(index), entity);
    }

    @Override
    public int characteristics() {
        return TransferCharacteristics.DEFAULT;
    }

    @Override
    public int characteristics(int index) {
        return TransferCharacteristics.DEFAULT;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
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
            if (handled == amount) break;
        }
        return handled;
    }

    private int insertBehaviour(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (!isValid(index, resource)) return 0;

        ItemStack stack = getStackInSlot(index);
        if (!stack.isEmpty() && !resource.is(stack)) return 0;

        amount = Math.min(amount, getCapacity(index, resource) - stack.getCount());
        if (amount > 0) {
            snapshots.get(index).updateSnapshots(transaction);
            internalStacks.set(index, resource.toStack(stack.getCount() + amount));
        }
        return amount;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
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
            if (handled == amount) break;
        }

        return handled;
    }

    private int extractBehaviour(int index, ItemResource resource, int amount, TransactionContext transaction) {
        ItemStack stack = getStackInSlot(index);
        EquipmentSlot equipmentSlot = slots.get(index);

        if (stack.isEmpty() || !resource.is(stack)) return 0;
        if (equipmentSlot.isArmor() && !resource.canUnequip()) return 0;

        int extracted = Math.min(amount, stack.getCount());
        if (extracted > 0) {
            snapshots.get(index).updateSnapshots(transaction);
            int newValue = stack.getCount() - extracted;
            internalStacks.set(index, resource.toStack(newValue));
        }
        return extracted;
    }

    private class Journal extends SnapshotJournal<ItemStack> {
        private final int index;

        public Journal(int index) {
            this.index = index;
        }

        @Override
        protected ItemStack createSnapshot() {
            return getStackInSlot(index).copy();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            ItemResource resource = ItemResource.of(snapshot);
            internalStacks.set(index, resource.toStack(snapshot.getCount()));
            entity.setItemSlot(slots.get(index), snapshot);
        }

        @Override
        protected void onCommit(ItemStack originalState) {
            entity.setItemSlot(slots.get(index), internalStacks.get(index));
        }
    }
}
