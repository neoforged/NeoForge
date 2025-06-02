///*
// * Copyright (c) NeoForged and contributors
// * SPDX-License-Identifier: LGPL-2.1-only
// */
//
//package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;
//
//import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
//import net.neoforged.neoforge.transfer.TransferAction;
//import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
//import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
//import net.neoforged.neoforge.transfer.handlers.templates.container.IResourceContainer;
//import net.neoforged.neoforge.transfer.resources.IResource;
//import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
//import net.neoforged.neoforge.transfer.resources.ResourceStack;
//import net.neoforged.neoforge.transfer.transaction.SnapshotParticipant;
//
//import java.util.Objects;
//
///**
// * Adapts any arbitrary resource handlers and wraps it into a IResourceContainer. Note, this may have odd behaviour when dealing with other mod's handlers here.
// */
//public final class ResourceHandlerToContainerAdapter<T extends IResource> implements IResourceContainer<T> {
//    private final IResourceHandler<T> wrappedHandler;
//    private final ResourceStack<T> emptyResource;
//
//    private class IndexSnapshot extends SnapshotParticipant<MutableResourceStack<T>> {
//        private final int slot;
//
//        private IndexSnapshot(int slot) {
//            this.slot = slot;
//        }
//
//        @Override
//        protected MutableResourceStack<T> createSnapshot() {
//            return MutableResourceStack.of(get(slot));
//        }
//
//        @Override
//        protected void revertToSnapshot(MutableResourceStack<T> snapshot) {
//            set(slot, snapshot);
//        }
//
//        @Override
//        protected void onCommit(MutableResourceStack<T> originalState) {
//            if(updateCallback != null)
//                updateCallback.run();
//        }
//    }
//
//    /**
//     *
//     */
//    public ResourceHandlerToContainerAdapter(
//            IResourceHandler<T> wrappedHandler, ResourceStack<T> emptyResource) {
//        this.wrappedHandler = wrappedHandler;
//        this.emptyResource = emptyResource;
//    }
//    @Override
//    public int size() {
//        return wrappedHandler.size();
//    }
//
//
//    @Override
//    public SnapshotParticipant<MutableResourceStack<T>> getParticipant(int index) {
//        return null;
//    }
//
//    @Override
//    public MutableResourceStack<T> get(int index) {
//        var resource = wrappedHandler.getResource(index);
//        if (resource.isEmpty()) return emptyResource().mutable();
//        return MutableResourceStack.of(resource, wrappedHandler.getAmount(index));
//    }
//
//    @Override
//    public void set(int index, MutableResourceStack<T> stack) {
//        if (wrappedHandler instanceof IResourceHandlerModifiable<T> modifiable) {
//            modifiable.set(index, stack.resource(), stack.amount());
//            return;
//        }
//
//        var resource = wrappedHandler.getResource(index);
//        if (!resource.isEmpty())
//            wrappedHandler.extract(index, resource, ResourceHandlerUtil.MAX_RESOURCE_SIZE, TransferAction.EXECUTE);
//        wrappedHandler.insert(index, stack.resource(), stack.amount(), TransferAction.EXECUTE);
//    }
//
//    @Override
//    public boolean isValid(int index, T resource) {
//        return wrappedHandler.isValid(index, resource);
//    }
//
//    @Override
//    public int getCapacity(int index, T resource) {
//        return wrappedHandler.getCapacity(index, resource);
//    }
//
//    @Override
//    public int getCapacity(int index) {
//        return wrappedHandler.getCapacity(index);
//    }
//
//    @Override
//    public IResourceHandlerModifiable<T> asHandler() {
//        return wrappedHandler instanceof IResourceHandlerModifiable<T> modifiable ? modifiable : IResourceContainer.super.asHandler();
//    }
//    public IResourceHandler<T> wrappedHandler() {
//        return wrappedHandler;
//    }
//    @Override
//    public ResourceStack<T> emptyResource() {
//        return emptyResource;
//    }
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == this) return true;
//        if (obj == null || obj.getClass() != this.getClass()) return false;
//        var that = (ResourceHandlerToContainerAdapter) obj;
//        return Objects.equals(this.wrappedHandler, that.wrappedHandler) &&
//                Objects.equals(this.emptyResource, that.emptyResource);
//    }
//    @Override
//    public int hashCode() {
//        return Objects.hash(wrappedHandler, emptyResource);
//    }
//    @Override
//    public String toString() {
//        return "ResourceHandlerToContainerAdapter[" +
//                "wrappedHandler=" + wrappedHandler + ", " +
//                "emptyResource=" + emptyResource + ']';
//    }
//
//}
