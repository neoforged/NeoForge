/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.storage.IResourceData;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ResourceStorageAttachment;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.storage.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public abstract class ItemStorageHandler extends ResourceStorageHandler<ItemResource> {
    public ItemStorageHandler(int size) {
        super(size, Item.ABSOLUTE_MAX_STACK_SIZE, ItemResource.NONE);
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return Math.min(resource.getMaxStackSize(), getCapacity(index));
    }

    public static class Component extends ItemStorageHandler {
        protected final IItemContext context;
        protected final DataComponentType<ResourceStorageComponent<ItemResource>> componentType;

        public Component(IItemContext context, DataComponentType<ResourceStorageComponent<ItemResource>> componentType, int size) {
            super(size);
            this.context = context;
            this.componentType = componentType;
        }

        @Override
        public IResourceData<ItemResource> getContents() {
            return context.getResource().getOrDefault(componentType, ResourceStorageAttachment.of(size, emptyResource));
        }

        @Override
        public int setAndValidate(IResourceData<ItemResource> contents, int requestedAmount, int changedAmount, TransferAction action) {
            if (changedAmount == 0) return 0;
            var exchangeCount = requestedAmount / changedAmount;
//            var partial = requestedAmount % changedAmount; // This in theory isn't actually handle here very well.
            var result = context.exchange(context.getResource().with(componentType, contents.component()), exchangeCount, action);
            return result * changedAmount;
        }
    }

    public static class Attachment extends ItemStorageHandler {
        protected final AttachmentHolder holder;
        protected final AttachmentType<IResourceData<ItemResource>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<IResourceData<ItemResource>> attachmentType, int size) {
            super(size);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public IResourceData<ItemResource> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public int setAndValidate(IResourceData<ItemResource> contents, int requestedAmount, int changedAmount, TransferAction action) {
            if (action.isExecuting()) holder.setData(attachmentType, contents);
            return changedAmount;
        }
    }
}
