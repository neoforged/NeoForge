/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.HandlerUtils;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

import java.util.function.Supplier;

public class RangedHandlerWrapper<T extends IResource> extends DelegatingHandlerWrapper<T> {
    protected int start;
    protected int end;

    public RangedHandlerWrapper(IResourceHandler<T> delegate, int start, int end) {
        this(() -> delegate, start, end);
    }

    public RangedHandlerWrapper(Supplier<IResourceHandler<T>> delegate, int start, int end) {
        super(delegate);
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    protected int convertIndex(int index) {
        return index + start;
    }

    @Override
    public int extract(T resource, int amount, TransferAction action) {
        return HandlerUtils.extractRange(getDelegate(), start, end, resource, amount, action);
    }

    @Override
    public int insert(T resource, int amount, TransferAction action) {
        return HandlerUtils.insertRange(getDelegate(), start, end, resource, amount, action);
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
            getDelegate().set(convertIndex(index), resource, amount);
        }

        @Override
        public IResourceHandlerModifiable<T> getDelegate() {
            return (IResourceHandlerModifiable<T>) super.getDelegate();
        }
    }
}
