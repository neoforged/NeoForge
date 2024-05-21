package net.neoforged.neoforge.transfer.storage.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.storage.ISingleStorage;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class InfiniteStorage<T extends IResource> implements ISingleStorage<T> {
    private boolean autoSetResource = false;

    public InfiniteStorage<T> autoSetResource() {
        autoSetResource = true;
        return this;
    }

    public InfiniteStorage<T> setResource(T resource) {
        return this;
    }

    @Override
    public int getAmount() {
        return isEmpty() ? 0 : getLimit();
    }

    @Override
    public int getLimit() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isResourceValid(T resource) {
        return false;
    }

    public boolean isEmpty() {
        return getResource().isBlank();
    }

    @Override
    public boolean canInsert() {
        return autoSetResource;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (autoSetResource && !isEmpty()) setResource(resource);
        return Objects.equals(resource, getResource()) ? amount : 0;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return Objects.equals(resource, getResource()) ? amount : 0;
    }

    public static class Attachment<T extends IResource> extends InfiniteStorage<T> {
        private final Supplier<AttachmentType<T>> attachmentType;
        private final AttachmentHolder holder;

        public Attachment(Supplier<AttachmentType<T>> attachmentType, AttachmentHolder holder) {
            this.attachmentType = attachmentType;
            this.holder = holder;
        }

        @Override
        public InfiniteStorage<T> setResource(T resource) {
            holder.setData(attachmentType, resource);
            return this;
        }

        @Override
        public T getResource() {
            return holder.getData(attachmentType);
        }
    }

    public static class Item<T extends IResource> extends InfiniteStorage<T> {
        private final Supplier<DataComponentType<T>> componentType;
        private final IItemContext context;
        private final T initialResource;

        public Item(Supplier<DataComponentType<T>> componentType, IItemContext context, T initialResource) {
            this.componentType = componentType;
            this.context = context;
            this.initialResource = initialResource;
        }

        @Override
        public InfiniteStorage<T> setResource(T resource) {
            context.exchange(context.getResource().set(componentType, resource), context.getAmount(), TransferAction.EXECUTE);
            return this;
        }

        @Override
        public T getResource() {
            return context.getResource().getOrDefault(componentType, initialResource);
        }
    }

    // TODO Give better name
    public static class Static<T extends IResource> extends InfiniteStorage<T> {
        private final T resource;

        public Static(T resource) {
            this.resource = resource;
        }

        @Override
        public T getResource() {
            return resource;
        }
    }
}
