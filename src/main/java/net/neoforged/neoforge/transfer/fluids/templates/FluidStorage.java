package net.neoforged.neoforge.transfer.fluids.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ResourceStorageContents;
import net.neoforged.neoforge.transfer.handlers.templates.ResourceStorage;

public abstract class FluidStorage extends ResourceStorage<FluidResource> {
    public FluidStorage(int size, int indexCapacity) {
        super(FluidResource.NONE, size, indexCapacity);
    }

    public static class Component extends FluidStorage {
        protected final IItemContext context;
        protected final DataComponentType<ResourceStorageContents<FluidResource>> componentType;

        public Component(IItemContext context, DataComponentType<ResourceStorageContents<FluidResource>> componentType, int size, int indexCapacity) {
            super(size, indexCapacity);
            this.context = context;
            this.componentType = componentType;
        }

        @Override
        public ResourceStorageContents<FluidResource> getContents() {
            return context.getResource().getOrDefault(componentType, new ResourceStorageContents<>(size, emptyResource));
        }

        @Override
        public int setAndValidate(ResourceStorageContents<FluidResource> contents, int changedAmount, TransferAction action) {
            return context.exchange(context.getResource().set(componentType, contents), 1, action) == 1 ? changedAmount : 0;
        }
    }

    public static class Attachment extends FluidStorage {
        protected final AttachmentHolder holder;
        protected final AttachmentType<ResourceStorageContents<FluidResource>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<ResourceStorageContents<FluidResource>> attachmentType, int size, int indexCapacity) {
            super(size, indexCapacity);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public ResourceStorageContents<FluidResource> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public int setAndValidate(ResourceStorageContents<FluidResource> contents, int changedAmount, TransferAction action) {
            if (action.isExecuting()) holder.setData(attachmentType, contents);
            return changedAmount;
        }
    }
}
