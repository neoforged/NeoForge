package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.ResourceAmount;

/**
 * Immutable combination of a {@link Fluid} and data components.
 * Similar to a {@link FluidStack}, but immutable and without amount information.
 */
public final class FluidResource implements IResource, DataComponentHolder {
    /**
     * Codec for a fluid resource.
     * Same format as {@link FluidStack#fixedAmountCodec}.
     * Does <b>not</b> accept blank resources.
     */
    public static final Codec<FluidResource> CODEC = FluidStack.fixedAmountCodec(1)
            .xmap(FluidResource::of, r -> r.toStack(1));
    /**
     * Codec for a fluid resource. Same format as {@link #CODEC}, and also accepts blank resources.
     */
    public static final Codec<FluidResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(o -> o.orElse(FluidResource.EMPTY), r -> r.isBlank() ? Optional.of(FluidResource.EMPTY) : Optional.of(r));
    /**
     * Codec for a fluid resource and an amount. Does <b>not</b> accept empty stacks.
     */
    public static final Codec<ResourceAmount<FluidResource>> WITH_AMOUNT_CODEC = FluidStack.CODEC
            .xmap(FluidStack::immutable, FluidStack::of);
    /**
     * Codec for a fluid resource and an amount. Accepts empty stacks.
     */
    public static final Codec<ResourceAmount<FluidResource>> OPTIONAL_WITH_AMOUNT_CODEC = FluidStack.OPTIONAL_CODEC
            .xmap(FluidStack::immutable, FluidStack::of);
    /**
     * Stream codec for a fluid resource. Accepts blank resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> OPTIONAL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID),
            FluidResource::getFluidHolder,
            DataComponentPatch.STREAM_CODEC,
            FluidResource::getComponentsPatch,
            FluidResource::of);

    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

    public static FluidResource of(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? EMPTY : new FluidResource(fluidStack.copyWithAmount(1));
    }

    public static FluidResource of(Fluid fluid) {
        return fluid == Fluids.EMPTY ? EMPTY : new FluidResource(new FluidStack(fluid, 1));
    }

    public static FluidResource of(Holder<Fluid> fluid, DataComponentPatch patch) {
        return fluid.value() == Fluids.EMPTY ? EMPTY : new FluidResource(new FluidStack(fluid, 1, patch));
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

    public Holder<Fluid> getFluidHolder() {
        return innerStack.getFluidHolder();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
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
