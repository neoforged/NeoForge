package net.neoforged.neoforge.transfer.handlers.templates;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

public class SingleResourceStorageAttachment<T extends IResource> implements ISingleResourceHandler<T> {
    protected final AttachmentHolder holder;
    protected final AttachmentType<ResourceStack<T>> attachmentType;

    protected final int capacity;

    protected final T emptyResource;
    protected final ResourceStack<T> emptyStack;

    protected Predicate<T> validator = r -> true;

    public SingleResourceStorageAttachment(AttachmentHolder holder, AttachmentType<ResourceStack<T>> attachmentType, T emptyResource, int capacity) {
        this.holder = holder;
        this.attachmentType = attachmentType;

        this.emptyResource = emptyResource;
        this.emptyStack = new ResourceStack<>(emptyResource, 0);

        this.capacity = capacity;
    }

    public SingleResourceStorageAttachment<T> setValidator(Predicate<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public T getResource() {
        return holder.getData(attachmentType).resource();
    }

    @Override
    public int getAmount() {
        return holder.getData(attachmentType).amount();
    }

    @Override
    public int getCapacity(T resource) {
        return capacity;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean isValid(T resource) {
        return validator.test(resource);
    }

    public boolean isEmpty() {
        return holder.getData(attachmentType).isEmpty();
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !isValid(resource) || (!isEmpty() && !getResource().equals(resource))) return 0;
        int inserted = Math.min(amount, getCapacity(resource) - getAmount());
        if (inserted > 0 && action.isExecuting()) {
            holder.setData(attachmentType, new ResourceStack<>(resource, getAmount() + inserted));
        }
        return inserted;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !isValid(resource) || (isEmpty() || !getResource().equals(resource))) return 0;
        int extracted = Math.min(amount, getAmount());
        if (extracted > 0 && action.isExecuting()) {
            int newAmount = getAmount() - extracted;
            holder.setData(attachmentType, newAmount <= 0 ? emptyStack : new ResourceStack<>(resource, newAmount));
        }
        return extracted;
    }

    @Override
    public boolean allowsInsertion() {
        return true;
    }

    @Override
    public boolean allowsExtraction() {
        return true;
    }
}
