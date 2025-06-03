/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.handlers.IItemCapabilityContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.IResourceStorageData;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ResourceStorageHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A generalized resource handler prioritized for storing fluids. There is a {@link Component component} and an {@link Attachment attachment} variant to choose from.
 * Both utilize a similar backing component structure, but vary slightly with how mutable they may be based on the implementation.
 * <p>
 * ItemStacks for example require their component data to be immutable, so changing or mutating the amount is out of the equation.
 * On the other hand an attachment that lives on a BlockEntity can be mutable and thus reduces its GC presence.
 */
public abstract class FluidStorageHandler extends ResourceStorageHandler<FluidResource> {
    public FluidStorageHandler(int size, int indexCapacity) {
        super(size, indexCapacity, FluidResource.EMPTY);
    }

    public static class Component extends FluidStorageHandler {
        protected final IItemCapabilityContext itemContext;
        protected final DataComponentType<ResourceStorageComponent<FluidResource>> componentType;

        public Component(IItemCapabilityContext context, DataComponentType<ResourceStorageComponent<FluidResource>> componentType, int size, int indexCapacity) {
            super(size, indexCapacity);
            this.itemContext = context;
            this.componentType = componentType;
        }

        @Override
        public IResourceStorageData<FluidResource> getContents() {
            return itemContext.getResource().getOrDefault(componentType, new ResourceStorageComponent<>(size, FluidResource.EMPTY));
        }

        @Override
        public void setContents(IResourceStorageData<FluidResource> contents) {
            itemContext.getResource().with(componentType, (ResourceStorageComponent<FluidResource>) contents);
        }

        @Override
        public int modifyContents(IResourceStorageData<FluidResource> contents, int requestedAmount, int changedAmount, TransactionContext context) {
            if (changedAmount == 0) return 0;
            var exchangeCount = requestedAmount / changedAmount;
            var resourceToExchange = itemContext.getResource().with(componentType, contents.component());
            var result = itemContext.exchange(resourceToExchange, exchangeCount, context);
            return result * changedAmount;
        }
    }

    public static class Attachment extends FluidStorageHandler {
        protected final AttachmentHolder holder;
        protected final AttachmentType<IResourceStorageData<FluidResource>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<IResourceStorageData<FluidResource>> attachmentType, int size, int indexCapacity) {
            super(size, indexCapacity);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public IResourceStorageData<FluidResource> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public void setContents(IResourceStorageData<FluidResource> contents) {
            holder.setData(attachmentType, contents);
        }

        @Override
        protected void onContentsChanged() {
            holder.setData(attachmentType, getContents());
        }
    }
}
