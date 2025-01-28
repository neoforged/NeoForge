package net.neoforged.neoforge.transfer.handlers.templates.container.templates;

import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import org.jetbrains.annotations.Nullable;

public class SimpleFluidResourceContainer extends ResourceContainer<FluidResource> {
    public SimpleFluidResourceContainer(NonNullList<MutableResourceStack<FluidResource>> stacks, int capacity, @Nullable Runnable updateCallback) {
        super(stacks, FluidResource.EMPTY_STACK, capacity, updateCallback);
    }

    public static SimpleFluidResourceContainer.Builder builder(int size) {
        return new Builder().size(size).capacity(FluidType.BUCKET_VOLUME);
    }

    public static SimpleFluidResourceContainer.Builder from(NonNullList<MutableResourceStack<FluidResource>> stacks) {
        return new Builder().from(stacks).capacity(FluidType.BUCKET_VOLUME);
    }

    public static class Builder extends ResourceContainer.Builder<FluidResource, Builder> {
        public Builder() {
            super(FluidResource.EMPTY_STACK);
        }

        @Override
        public SimpleFluidResourceContainer build() {
            return new SimpleFluidResourceContainer(stacks, capacity, updateCallback);
        }
    }
}
