package net.neoforged.neoforge.transfer.fluid;

import net.neoforged.neoforge.transfer.storage.EmptyStorage;

public final class EmptyFluidStorage extends EmptyStorage<FluidVariant> {
    public static final EmptyFluidStorage INSTANCE = new EmptyFluidStorage();

    @Override
    protected FluidVariant getBlankResource() {
        return FluidVariant.EMPTY;
    }
}
