/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.fluids.FluidStorageHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resource.IResourceStorageData;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageAttachment;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A generalized resource handler prioritized for storing items. There is a {@link FluidStorageHandler.Component component} and an {@link FluidStorageHandler.Attachment attachment} variant to choose from.
 * Both utilize a similar backing component structure, but vary slightly with how mutable they may be based on the implementation.
 * <p>
 * ItemStacks for example require their component data to be immutable, so changing or mutating the amount is out of the equation.
 * On the other hand an attachment that lives on a BlockEntity can be mutable and thus reduces its GC presence.
 */
public abstract class ItemStorageHandler extends ResourceStorageHandler<ItemResource> {
    public ItemStorageHandler(int size) {
        super(size, Item.ABSOLUTE_MAX_STACK_SIZE, ItemResource.EMPTY);
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return Math.min(resource.getMaxStackSize(), capacity);
    }

    public static class Component extends ItemStorageHandler {
        protected final IItemContext itemContext;
        protected final DataComponentType<ResourceStorageComponent<ItemResource>> componentType;

        public Component(IItemContext itemContext, DataComponentType<ResourceStorageComponent<ItemResource>> componentType, int size) {
            super(size);
            this.itemContext = itemContext;
            this.componentType = componentType;
        }

        @Override
        public IResourceStorageData<ItemResource> getContents() {
            return itemContext.getResource().getOrDefault(componentType, ResourceStorageAttachment.of(size, defaultResource));
        }

        @Override
        public void setContents(IResourceStorageData<ItemResource> contents) {
            itemContext.getResource().with(componentType, (ResourceStorageComponent<ItemResource>) contents);
        }

        @Override
        public int modifyContents(IResourceStorageData<ItemResource> contents, int requestedAmount, int changedAmount, TransactionContext action) {
            if (changedAmount == 0) return 0;
            var exchangeCount = requestedAmount / changedAmount;
            //                            var partial = requestedAmount % changedAmount; // This in theory isn't actually handle here very well.
            var resourceToExchange = itemContext.getResource().with(componentType, contents.component());
            var result = itemContext.exchange(resourceToExchange, exchangeCount, action);
            return result * changedAmount;
        }
    }

    public static class Attachment extends ItemStorageHandler {
        protected final AttachmentHolder holder;
        protected final AttachmentType<IResourceStorageData<ItemResource>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<IResourceStorageData<ItemResource>> attachmentType, int size) {
            super(size);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public IResourceStorageData<ItemResource> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        protected void onContentsChanged() {
            //essentially setChanged, but we can't necessarily assume the holder is a block entity
            holder.setData(attachmentType, getContents());
        }
    }
}
