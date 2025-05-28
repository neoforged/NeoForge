package net.neoforged.neoforge.transfer.fluid.base;

import net.neoforged.neoforge.transfer.fluid.FluidVariant;
import net.neoforged.neoforge.transfer.storage.base.EmptyStorage;

public final class EmptyFluidStorage extends EmptyStorage<FluidVariant> {
    public static final EmptyFluidStorage INSTANCE = new EmptyFluidStorage();

    @Override
    protected FluidVariant getBlankResource() {
        return FluidVariant.EMPTY;
    }
}
