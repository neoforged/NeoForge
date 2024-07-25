package net.neoforged.neoforge.transfer.items.templates;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.ResourceStorageContents;
import net.neoforged.neoforge.transfer.handlers.templates.ResourceStorage;
import net.neoforged.neoforge.transfer.items.ItemResource;

public abstract class ItemStorage extends ResourceStorage<ItemResource> {
    public ItemStorage(int size) {
        super(ItemResource.NONE, size, Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    @Override
    public int getCapacity(int index, ItemResource resource) {
        return resource.getMaxStackSize();
    }

    public static class Component extends ItemStorage {
        protected final IItemContext context;
        protected final DataComponentType<ResourceStorageContents<ItemResource>> componentType;

        public Component(IItemContext context, DataComponentType<ResourceStorageContents<ItemResource>> componentType, int size) {
            super(size);
            this.context = context;
            this.componentType = componentType;
        }

        @Override
        public ResourceStorageContents<ItemResource> getContents() {
            return context.getResource().getOrDefault(componentType, new ResourceStorageContents<>(size, emptyResource));
        }

        @Override
        public int setAndValidate(ResourceStorageContents<ItemResource> contents, int changedAmount, TransferAction action) {
            return context.exchange(context.getResource().set(componentType, contents), 1, action) == 1 ? changedAmount : 0;
        }
    }

    public static class Attachment extends ItemStorage {
        protected final AttachmentHolder holder;
        protected final AttachmentType<ResourceStorageContents<ItemResource>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<ResourceStorageContents<ItemResource>> attachmentType, int size) {
            super(size);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public ResourceStorageContents<ItemResource> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public int setAndValidate(ResourceStorageContents<ItemResource> contents, int changedAmount, TransferAction action) {
            if (action.isExecuting()) holder.setData(attachmentType, contents);
            return changedAmount;
        }
    }
}
