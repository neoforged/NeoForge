package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.IResourceData;
import net.neoforged.neoforge.transfer.handlers.templates.ResourceStorageComponent;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.ResourceStorageHandler;

public abstract class FluidStorageHandler extends ResourceStorageHandler<FluidResource> {
    public FluidStorageHandler(int size, int indexCapacity) {
        super(size, indexCapacity, FluidResource.NONE);
    }

    public static class Component extends FluidStorageHandler {
        protected final IItemContext context;
        protected final DataComponentType<ResourceStorageComponent<FluidResource>> componentType;

        public Component(IItemContext context, DataComponentType<ResourceStorageComponent<FluidResource>> componentType, int size, int indexCapacity) {
            super(size, indexCapacity);
            this.context = context;
            this.componentType = componentType;
        }

        @Override
        public IResourceData<FluidResource> getContents() {
            return context.getResource().getOrDefault(componentType, new ResourceStorageComponent<>(size, FluidResource.NONE));
        }

        @Override
        public int setAndValidate(IResourceData<FluidResource> contents, int requestedAmount, int changedAmount, TransferAction action) {
            if(changedAmount == 0) return 0;
            var exchangeCount = requestedAmount / changedAmount;
            var result = context.exchange(context.getResource().with(componentType, contents.component()), exchangeCount, action);
            return result*changedAmount;
        }
    }

    public static class Attachment extends FluidStorageHandler {
        protected final AttachmentHolder holder;
        protected final AttachmentType<IResourceData<FluidResource>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<IResourceData<FluidResource>> attachmentType, int size, int indexCapacity) {
            super(size, indexCapacity);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public IResourceData<FluidResource> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public int setAndValidate(IResourceData<FluidResource> contents, int requestedAmount, int changedAmount, TransferAction action) {
            if (action.isExecuting()) holder.setData(attachmentType, contents);
            return changedAmount;
        }
    }
}
