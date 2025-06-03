/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

//Unfortunately, this was not really feasible from what I could tell while also providing a fully generalized transaction snapshot.
// It may be best to just have IFluidHandler be deprecated and intended to be removed rather than phase out slowly.
// Leaving the classes in allows for a quick check on the migration path, but it shouldn't be expected to be used

///*
// * Copyright (c) NeoForged and contributors
// * SPDX-License-Identifier: LGPL-2.1-only
// */
//
//package net.neoforged.neoforge.transfer.handlers.adapters;
//
//import net.neoforged.neoforge.fluids.FluidStack;
//import net.neoforged.neoforge.fluids.capability.IFluidHandler;
//import net.neoforged.neoforge.transfer.TransferAction;
//import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
//import net.neoforged.neoforge.transfer.resources.FluidResource;
//import net.neoforged.neoforge.transfer.transaction.TransactionContext;
//
///**
// * A wrapper for devs who are still using the legacy IFluidHandler interface. This should not be relied on and should be
// * replaced with the new IResourceHandler interface. This wrapper will be removed alongside the legacy IFluidHandler
// * interface in 1.22.
// */
//public final class LegacyFluidHandlerAdapter implements IResourceHandler<FluidResource> {
//    private final IFluidHandler handler;
//
//    /**
//     * @param handler The legacy IFluidHandler to wrap
//     */
//    public static LegacyFluidHandlerAdapter of(IFluidHandler handler) {
//        return new LegacyFluidHandlerAdapter(handler);
//    }
//
//    private LegacyFluidHandlerAdapter(IFluidHandler handler) {
//        this.handler = handler;
//    }
//
//    @Override
//    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
//        FluidStack fluidInTank = handler.getFluidInTank(index);
//        if (resource.isEmpty() || amount <= 0 || !(fluidInTank.isEmpty() || resource.matches(fluidInTank))) return 0;
//        return insert(resource, amount, transaction);
//    }
//
//    @Override
//    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
//        if (resource.isEmpty() || amount <= 0 || !resource.matches(handler.getFluidInTank(index))) return 0;
//        return extract(resource, amount, transaction);
//    }
//
//    @Override
//    public int insert(FluidResource resource, int amount, TransactionContext transaction) {
//        if (resource.isEmpty() || amount <= 0) return 0;
//
//        return handler.fill(resource.toStack(amount), action.isExecuting() ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
//    }
//
//    @Override
//    public int extract(FluidResource resource, int amount, TransactionContext action) {
//        if (resource.isEmpty() || amount <= 0) return 0;
//        var test = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
//        if (test.isEmpty() || !resource.matches(test)) return 0;
//        return handler.drain(resource.toStack(amount), action.isExecuting() ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE).getAmount();
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
//    public int getCapacity(int index) {
//        return handler.getTankCapacity(index);
//    }
//
//    @Override
//    public boolean isValid(int index, FluidResource resource) {
//        return handler.isFluidValid(index, resource.toStack());
//    }
//
//    @Override
//    public boolean allowsInsertion(int index) {
//        return true;
//    }
//
//    @Override
//    public boolean allowsExtraction(int index) {
//        return true;
//    }
//
//    public IFluidHandler handler() {
//        return handler;
//    }
//}
