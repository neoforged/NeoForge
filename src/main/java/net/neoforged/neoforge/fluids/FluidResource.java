package net.neoforged.neoforge.fluids;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.IResource;

/**
 * Immutable combination of a {@link Fluid} and data components.
 * Similar to a {@link FluidStack}, but immutable and without amount information.
 */
public final class FluidResource implements IResource, DataComponentHolder {
    // TODO: we need codecs and stream codecs...
    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

    public static FluidResource of(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? EMPTY : new FluidResource(fluidStack.copyWithAmount(1));
    }

    /**
     * We wrap a fluid stack which must never be exposed and/or modified.
     */
    private final FluidStack innerStack;

    private FluidResource(FluidStack innerStack) {
        this.innerStack = innerStack;
    }

    @Override
    public boolean isBlank() {
        return innerStack.isEmpty();
    }

    public Fluid getFluid() {
        return innerStack.getFluid();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(stack, innerStack);
    }

    public FluidStack toStack(int amount) {
        return this.innerStack.copyWithAmount(amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof FluidResource v && FluidStack.isSameFluidSameComponents(v.innerStack, innerStack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(innerStack);
    }
}
