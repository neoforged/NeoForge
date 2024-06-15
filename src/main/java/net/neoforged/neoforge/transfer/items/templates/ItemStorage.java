/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.items.ItemStorageContents;

import java.util.function.Supplier;

public abstract class ItemStorage implements IResourceHandlerModifiable<ItemResource> {
    protected final int size;

    public ItemStorage(int size) {
        this.size = size;
    }

    public abstract ItemStorageContents getContents();

    public abstract int setAndValidate(ItemStorageContents contents, int changedAmount, TransferAction action);

    @Override
    public int size() {
        return size;
    }

    @Override
    public ItemResource getResource(int index) {
        return getContents().getResource(index);
    }

    @Override
    public int getAmount(int index) {
        return getContents().getAmount(index);
    }

    @Override
    public int getLimit(int index, ItemResource resource) {
        return resource.getMaxStackSize();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return true;
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        ItemStorageContents contents = getContents().set(index, resource, amount);
        setAndValidate(contents, 0, TransferAction.EXECUTE);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank() || !isValid(index, resource)) return 0;
        ItemStorageContents contents = getContents();
        ResourceStack<ItemResource> stack = contents.get(index);
        if (stack.isEmpty()) {
            amount = Math.min(amount, resource.getMaxStackSize());
            contents = contents.set(index, resource, amount);
        } else if (stack.resource().equals(resource) && stack.amount() < resource.getMaxStackSize()) {
            int newAmount = Math.min(stack.amount() + amount, resource.getMaxStackSize());
            amount = newAmount - stack.amount();
            contents = contents.set(index, resource, newAmount);
        } else {
            return 0;
        }

        return setAndValidate(contents, amount, action);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank()) return 0;
        ItemStorageContents contents = getContents();
        int remaining = amount;
        for (int i = 0; i < size; i++) {
            ResourceStack<ItemResource> stack = contents.get(i);
            if (stack.isEmpty() || !stack.resource().equals(resource) || stack.amount() >= resource.getMaxStackSize()) continue;
            int toInsert = Math.min(remaining, resource.getMaxStackSize() - stack.amount());
            contents = contents.set(i, resource, stack.amount() + toInsert);
            remaining -= toInsert;
        }
        for (int i = 0; i < size; i++) {
            ResourceStack<ItemResource> stack = contents.get(i);
            if (!stack.isEmpty()) continue;
            int toInsert = Math.min(remaining, resource.getMaxStackSize());
            contents = contents.set(i, resource, toInsert);
            remaining -= toInsert;
            if (remaining <= 0) {
                break;
            }
        }
        return setAndValidate(contents, amount - remaining, action);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isBlank()) return 0;
        ItemStorageContents contents = getContents();
        ResourceStack<ItemResource> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extracted = Math.min(stack.amount(), amount);
        int newAmount = stack.amount() - extracted;
        contents = contents.set(index, newAmount == 0 ? ItemResource.BLANK : stack.resource(), newAmount);
        return setAndValidate(contents, extracted, action);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        int remaining = amount;
        ItemStorageContents contents = getContents();
        for (int slot = 0; slot < size; slot++) {
            ResourceStack<ItemResource> stack = contents.get(slot);
            if (stack.isEmpty() || !stack.resource().equals(resource)) continue;
            int extracted = Math.min(remaining, stack.amount());
            int newAmount = stack.amount() - extracted;
            contents = contents.set(slot, newAmount == 0 ? ItemResource.BLANK : resource, newAmount);
            remaining -= extracted;
            if (remaining <= 0) {
                break;
            }
        }
        return setAndValidate(contents, amount - remaining, action);
    }

    public static class Attachment extends ItemStorage {
        private final Supplier<AttachmentType<ItemStorageContents>> attachmentType;
        private final AttachmentHolder holder;

        public Attachment(int slotCount, Supplier<AttachmentType<ItemStorageContents>> attachmentType, AttachmentHolder holder) {
            super(slotCount);
            this.attachmentType = attachmentType;
            this.holder = holder;
        }

        @Override
        public ItemStorageContents getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public int setAndValidate(ItemStorageContents contents, int changedAmount, TransferAction action) {
            if (action.isExecuting()) holder.setData(attachmentType.get(), contents);
            return changedAmount;
        }
    }

    public static class Item extends ItemStorage {
        private final Supplier<DataComponentType<ItemStorageContents>> componentType;
        private final IItemContext context;

        public Item(int slotCount, Supplier<DataComponentType<ItemStorageContents>> componentType, IItemContext context) {
            super(slotCount);
            this.componentType = componentType;
            this.context = context;
        }

        @Override
        public ItemStorageContents getContents() {
            return context.getResource().getOrDefault(componentType, new ItemStorageContents(size()));
        }

        @Override
        public int setAndValidate(ItemStorageContents contents, int changedAmount, TransferAction action) {
            return context.exchange(context.getResource().set(componentType, contents), 1, action) == 1 ? changedAmount : 0;
        }
    }
}
