package net.neoforged.neoforge.transfer.fluids.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.ResourceStorage;

public abstract class FluidStorage extends ResourceStorage<FluidResource> {
    public FluidStorage(int size, int indexCapacity) {
        super(FluidResource.NONE, size, indexCapacity);
    }

    public static class Item extends ResourceStorage.Item<FluidResource> {
        public Item(IItemContext context, DataComponentType<ResourceStorageContents<FluidResource>> componentType, int size, int indexCapacity) {
            super(context, componentType, FluidResource.NONE, size, indexCapacity);
        }
    }

    public static class Attachment extends ResourceStorage.Attachment<FluidResource> {
        public Attachment(AttachmentHolder holder, AttachmentType<ResourceStorageContents<FluidResource>> attachmentType, int size, int indexCapacity) {
            super(holder, attachmentType, FluidResource.NONE, size, indexCapacity);
        }
    }
}
