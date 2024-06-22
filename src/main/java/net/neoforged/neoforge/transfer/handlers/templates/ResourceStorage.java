package net.neoforged.neoforge.transfer.handlers.templates;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;

public abstract class ResourceStorage<T extends IResource> implements IResourceHandler<T> {
    protected final T emptyResource;
    protected final int size;
    protected final int indexCapacity;

    public ResourceStorage(T emptyResource, int size, int indexCapacity) {
        this.emptyResource = emptyResource;
        this.size = size;
        this.indexCapacity = indexCapacity;
    }

    public abstract ResourceStorageContents<T> getContents();

    public abstract int setAndValidate(ResourceStorageContents<T> contents, int changedAmount, TransferAction action);

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = insertBehavior(contents, index, resource, amount, action);
        return setAndValidate(contents.build(), changedAmount, action);
    }

    protected int insertBehavior(ResourceStorageContents.Builder<T> contents, int index, T resource, int amount, TransferAction action) {
        if (!isValid(index, resource) || !allowsInsertion(index)) return 0;
        ResourceStack<T> stack = contents.get(index);
        if (!stack.isEmpty() && !stack.resource().equals(resource)) return 0;
        int insertAmount = Math.min(amount, getCapacity(index, resource) - stack.amount());
        contents.set(index, resource, stack.amount() + insertAmount);
        return insertAmount;
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = extractBehavior(contents, index, resource, amount, action);
        return setAndValidate(contents.build(), changedAmount, action);
    }

    protected int extractBehavior(ResourceStorageContents.Builder<T> contents, int index, T resource, int amount, TransferAction action) {
        if (!isValid(index, resource) || !allowsExtraction(index)) return 0;
        ResourceStack<T> stack = contents.get(index);
        if (stack.isEmpty() || !stack.resource().equals(resource)) return 0;
        int extractAmount = Math.min(amount, stack.amount());
        contents.set(index, resource, stack.amount() - extractAmount);
        return extractAmount;
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += insertBehavior(contents, i, resource, amount - changedAmount, action);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents.build(), changedAmount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (amount <= 0 || resource.isEmpty()) return 0;
        var contents = getContents().builder();
        int changedAmount = 0;
        for (int i = 0; i < size(); i++) {
            changedAmount += extractBehavior(contents, i, resource, amount - changedAmount, action);
            if (changedAmount >= amount) break;
        }
        return setAndValidate(contents.build(), changedAmount, action);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T getResource(int index) {
        return getContents().get(index).resource();
    }

    @Override
    public int getAmount(int index) {
        return getContents().get(index).amount();
    }

    @Override
    public int getCapacity(int index, T resource) {
        return getCapacity(index);
    }

    @Override
    public int getCapacity(int index) {
        return indexCapacity;
    }

    @Override
    public boolean isValid(int index, T resource) {
        return true;
    }

    @Override
    public boolean allowsInsertion(int index) {
        return true;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return true;
    }

    public static class ResourceStorageContents<T extends IResource> {
        private final NonNullList<ResourceStack<T>> stacks;

        public static <T extends IResource> Codec<ResourceStorageContents<T>> codec(Codec<T> resourceCodec) {
            return NonNullList.codecOf(ResourceStack.codec(resourceCodec)).xmap(ResourceStorageContents::new, contents -> contents.stacks);
        }

        public ResourceStorageContents(int size, T emptyResource) {
            stacks = NonNullList.withSize(size, new ResourceStack<>(emptyResource, 0));
        }

        private ResourceStorageContents(NonNullList<ResourceStack<T>> stacks) {
            this.stacks = stacks;
        }

        public ResourceStack<T> get(int index) {
            return stacks.get(index);
        }

        public ResourceStorageContents<T> set(int index, T resource, int amount) {
            NonNullList<ResourceStack<T>> newStacks = NonNullList.copyOf(stacks);
            newStacks.set(index, new ResourceStack<>(resource, amount));
            return new ResourceStorageContents<>(newStacks);
        }

        public Builder<T> builder() {
            return new Builder<>(stacks);
        }

        public static class Builder<T extends IResource> {
            private final NonNullList<ResourceStack<T>> stacks;

            public Builder(NonNullList<ResourceStack<T>> stacks) {
                this.stacks = NonNullList.copyOf(stacks);
            }

            public Builder<T> set(int index, T resource, int amount) {
                stacks.set(index, new ResourceStack<>(resource, amount));
                return this;
            }

            public ResourceStack<T> get(int index) {
                return stacks.get(index);
            }

            public ResourceStorageContents<T> build() {
                return new ResourceStorageContents<>(stacks);
            }
        }
    }

    public static class Item<T extends IResource> extends ResourceStorage<T> {
        protected final IItemContext context;
        protected final DataComponentType<ResourceStorageContents<T>> componentType;

        public Item(IItemContext context, DataComponentType<ResourceStorageContents<T>> componentType, T emptyResource, int size, int indexCapacity) {
            super(emptyResource, size, indexCapacity);
            this.context = context;
            this.componentType = componentType;
        }

        @Override
        public ResourceStorageContents<T> getContents() {
            return context.getResource().getOrDefault(componentType, new ResourceStorageContents<>(size, emptyResource));
        }

        @Override
        public int setAndValidate(ResourceStorageContents<T> contents, int changedAmount, TransferAction action) {
            return context.exchange(context.getResource().set(componentType, contents), 1, action) == 1 ? changedAmount : 0;
        }
    }

    public static class Attachment<T extends IResource> extends ResourceStorage<T> {
        protected final AttachmentHolder holder;
        protected final AttachmentType<ResourceStorageContents<T>> attachmentType;

        public Attachment(AttachmentHolder holder, AttachmentType<ResourceStorageContents<T>> attachmentType, T emptyResource, int size, int indexCapacity) {
            super(emptyResource, size, indexCapacity);
            this.holder = holder;
            this.attachmentType = attachmentType;
        }

        @Override
        public ResourceStorageContents<T> getContents() {
            return holder.getData(attachmentType);
        }

        @Override
        public int setAndValidate(ResourceStorageContents<T> contents, int changedAmount, TransferAction action) {
            if (action.isExecuting()) holder.setData(attachmentType, contents);
            return changedAmount;
        }
    }
}
