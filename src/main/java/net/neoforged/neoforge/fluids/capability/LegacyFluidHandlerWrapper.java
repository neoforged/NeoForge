package net.neoforged.neoforge.fluids.capability;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;

/**
 * A wrapper for devs who are still using the legacy IFluidHandler interface. This should not be relied on and should be
 * replaced with the new IResourceHandler interface. This wrapper will be removed alongside the legacy IFluidHandler
 * interface in 1.22.
 * @param handler The legacy IFluidHandler to wrap
 */
public record LegacyFluidHandlerWrapper(IFluidHandler handler) implements IResourceHandler<FluidResource> {
    @Override
    public int insert(int index, FluidResource resource, int amount, TransferAction action) {
        FluidStack fluidInTank = handler.getFluidInTank(index);
        if (resource.isEmpty() || amount <= 0 || !(fluidInTank.isEmpty() || resource.matches(fluidInTank))) return 0;
        return insert(resource, amount, action);
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0 || !resource.matches(handler.getFluidInTank(index))) return 0;
        return extract(resource, amount, action);
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0) return 0;
        return handler.fill(resource.toStack(amount), action.isExecuting() ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE);
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        if (resource.isEmpty() || amount <= 0) return 0;
        var test = handler.drain(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
        if (test.isEmpty() || !resource.matches(test)) return 0;
        return handler.drain(resource.toStack(amount), action.isExecuting() ? IFluidHandler.FluidAction.EXECUTE : IFluidHandler.FluidAction.SIMULATE).getAmount();
    }

    @Override
    public int size() {
        return handler.getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(handler.getFluidInTank(index));
    }

    @Override
    public int getAmount(int index) {
        return handler.getFluidInTank(index).getAmount();
    }

    @Override
    public int getCapacity(int index, FluidResource resource) {
        return handler.getTankCapacity(index);
    }

    @Override
    public int getCapacity(int index) {
        return handler.getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return handler.isFluidValid(index, resource.toStack());
    }

    @Override
    public boolean allowsInsertion(int index) {
        return true;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return true;
    }
}
