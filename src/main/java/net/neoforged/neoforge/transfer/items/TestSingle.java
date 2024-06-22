package net.neoforged.neoforge.transfer.items;

import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;

public class TestSingle implements ISingleResourceHandler<FluidResource> {
    @Override
    public FluidResource getResource() {
        return null;
    }

    @Override
    public int getAmount() {
        return 0;
    }

    @Override
    public int getCapacity(FluidResource resource) {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public boolean isValid(FluidResource resource) {
        return false;
    }

    @Override
    public boolean allowsInsertion(int index) {
        return false;
    }

    @Override
    public boolean allowsExtraction(int index) {
        return false;
    }

    @Override
    public int insert(FluidResource resource, int amount, TransferAction action) {
        return 0;
    }

    @Override
    public int extract(FluidResource resource, int amount, TransferAction action) {
        return 0;
    }
}
