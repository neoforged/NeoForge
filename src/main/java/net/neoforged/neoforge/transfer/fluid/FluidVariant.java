package net.neoforged.neoforge.transfer.fluid;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.storage.RegistryObjectVariant;

import java.util.Optional;

/**
 * Represents a transferable variant of a fluid, which will be defined by the {@link Fluid base fluid}
 * and an optional {@link DataComponentPatch data component patch}.
 */
public final class FluidVariant implements RegistryObjectVariant<Fluid> {
    /**
     * Codec for a fluid variant, which <b>cannot</b> encode or decode variants of {@link Fluids#EMPTY}.
     * <p>
     * Same encoding as {@link FluidStack#fixedAmountCodec}.
     */
    public static final Codec<FluidVariant> CODEC = FluidStack.fixedAmountCodec(1)
            .xmap(FluidVariant::of, v -> v.toStack(1));

    /**
     * Codec for a fluid variant, which can also encode variants of {@link Fluids#EMPTY}.
     * <p>
     * This uses the same encoding as {@link #CODEC}.
     */
    public static final Codec<FluidVariant> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC)
            .xmap(o -> o.orElse(FluidVariant.EMPTY), r -> r.isBlank() ? Optional.of(FluidVariant.EMPTY) : Optional.of(r));

    /**
     * Stream codec for a fluid variant. Accepts blank variants.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidVariant> OPTIONAL_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID),
            FluidVariant::getFluidHolder,
            DataComponentPatch.STREAM_CODEC,
            FluidVariant::getComponentsPatch,
            FluidVariant::of);

    public static final FluidVariant EMPTY = new FluidVariant(FluidStack.EMPTY);


    public static FluidVariant of(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? EMPTY : new FluidVariant(fluidStack.copyWithAmount(1));
    }

    public static FluidVariant of(Holder<Fluid> fluid, DataComponentPatch patch) {
        return fluid.value() == Fluids.EMPTY ? EMPTY : new FluidVariant(new FluidStack(fluid, 1, patch));
    }

    /**
     * We wrap a fluid stack which must never be exposed and/or modified.
     */
    final FluidStack innerStack;

    private FluidVariant(FluidStack innerStack) {
        this.innerStack = innerStack;
    }

    /**
     * @return The base fluid.
     * @see #getBaseObject()
     */
    public Fluid getFluid() {
        return innerStack.getFluid();
    }

    public Holder<Fluid> getFluidHolder() {
        return innerStack.getFluidHolder();
    }

    @Override
    public boolean isBlank() {
        return innerStack.isEmpty();
    }

    @Override
    public Fluid getBaseObject() {
        return getFluid();
    }

    @Override
    public Holder<Fluid> getBaseObjectHolder() {
        return getFluidHolder();
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.getComponents();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
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
        return obj instanceof FluidVariant v && FluidStack.isSameFluidSameComponents(v.innerStack, innerStack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(innerStack);
    }

    @Override
    public String toString() {
        if (isComponentsPatchEmpty()) {
            return getBaseObject().toString();
        } else {
            return getBaseObject() + "[" + getComponentsPatch().size() + " patches]";
        }
    }
}
