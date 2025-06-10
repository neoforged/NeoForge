/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

///*
// * Copyright (c) NeoForged and contributors
// * SPDX-License-Identifier: LGPL-2.1-only
// */
//
//package net.neoforged.neoforge.transfer.handlers.resources.migration;
//
//import net.neoforged.neoforge.fluids.capability.IFluidHandler;
//import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
//import net.neoforged.neoforge.transfer.resources.FluidResource;
//import net.neoforged.neoforge.transfer.transaction.TransactionContext;
//
///**
// * This is a temporary wrapper to help some functionality of the resource handlers, but do note,
// * that this can't handle snapshotting and assumes there is no simulation.
// * This should only be used as a last resort when migrating as it will be removed.
// */
//@Deprecated
//public class FluidHandlerWrapper implements IResourceHandler<FluidResource> {
//    private final IFluidHandler handler;
//
//    public static FluidHandlerWrapper wrap(IFluidHandler handler) {
//        return new FluidHandlerWrapper(handler);
//    }
//
//    private FluidHandlerWrapper(IFluidHandler handler) {
//        this.handler = handler;
//    }
//
//    @Override
//    public int size() {
//        return handler.getTanks();
//    }
//
//    @Override
//    public FluidResource getResource(int index) {
//        return FluidResource.of(handler.getFluidInTank(index));
//    }
//
//    @Override
//    public int getAmount(int index) {
//        return handler.getFluidInTank(index).getAmount();
//    }
//
//    @Override
//    public int getCapacity(int index, FluidResource resource) {
//        return handler.getTankCapacity(index);
//    }
//
//    @Override
//    public boolean isValid(int index, FluidResource resource) {
//        return true;
//    }
//
//    @Override
//    public boolean supportsInsertion(int index) {
//        return true;
//    }
//
//    @Override
//    public boolean supportsExtraction(int index) {
//        return true;
//    }
//
//    @Override
//    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
//        return insert(resource, amount, transaction);
//    }
//
//    @Override
//    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
//        //assume commit
//        return handler.fill(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
//    }
//
//    @Override
//    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
//        return extract(resource, amount, transaction);
//    }
//
//    @Override
//    public int extract(FluidResource resource, int amount, TransactionContext transaction) {
//        var f = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.EXECUTE);
//        //This is very clearly wrong which is why I am suspecting we shouldn't really provide these classes.
//        // The fluid returned in the drain actually could be a different one in the old pattern despite it not being the intended result
//        return f.getAmount();
//    }
//}
