package net.neoforged.neoforge.transfer.storage.wrappers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.TransferUtils;
import net.neoforged.neoforge.transfer.storage.IResourceHandler;
import net.neoforged.neoforge.transfer.storage.IResourceHandlerModifiable;

import java.util.function.Supplier;

public class RangedHandlerWrapper<T extends IResource> implements IResourceHandler<T> {
    Supplier<IResourceHandler<T>> delegate;
    protected int start;
    protected int end;

    public RangedHandlerWrapper(IResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    public RangedHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int start, int end) {
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid range");
        }
        this.delegate = delegate;
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public T getResource(int index) {
        return getDelegate().getResource(index + start);
    }

    @Override
    public int getAmount(int index) {
        return getDelegate().getAmount(index + start);
    }

    @Override
    public int getLimit(int index, T resource) {
        return getDelegate().getLimit(index + start, resource);
    }

    @Override
    public boolean isValid(int index, T resource) {
        return getDelegate().isValid(index + start, resource);
    }

    @Override
    public boolean canInsert() {
        return getDelegate().canInsert();
    }

    @Override
    public boolean canExtract() {
        return getDelegate().canExtract();
    }

    @Override
    public int extract(int index, T resource, int amount, TransferAction action) {
        return getDelegate().extract(index + start, resource, amount, action);
    }

    @Override
    public int insert(int index, T resource, int amount, TransferAction action) {
        return getDelegate().insert(index + start, resource, amount, action);
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return TransferUtils.extractRange(getDelegate(), start, end, resource, amount, action);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return TransferUtils.insertRange(getDelegate(), start, end, resource, amount, action);
    }

    public IResourceHandler<T> getDelegate() {
        return delegate.get();
    }

    public static class Modifiable<T extends IResource> extends RangedHandlerWrapper<T> implements IResourceHandlerModifiable<T> {
        public Modifiable(IResourceHandlerModifiable<T> delegate, int start, int end) {
            super(delegate, start, end);
        }

        public Modifiable(Supplier<IResourceHandlerModifiable<T>> delegate, int start, int end) {
            super(delegate::get, start, end);
        }

        @Override
        public void set(int index, T resource, int amount) {
            getDelegate().set(index + start, resource, amount);
        }

        @Override
        public IResourceHandlerModifiable<T> getDelegate() {
            return (IResourceHandlerModifiable<T>) super.getDelegate();
        }
    }
}
