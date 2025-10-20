/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.resource.DataComponentHolderResource;

/**
 * Immutable combination of a {@link Fluid} and data components.
 * Similar to a {@link FluidStack}, but immutable and without amount information.
 */
public final class FluidResource implements DataComponentHolderResource<Fluid> {
    /**
     * The empty resource instance of a {@link FluidResource}
     */
    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);

    /**
     * Codec for a fluid resource.
     * Same format as {@link FluidStack#fixedAmountCodec}.
     * Does <b>not</b> accept empty resources.
     */
    public static final Codec<FluidResource> CODEC = FluidStack.fixedAmountCodec(FluidType.BUCKET_VOLUME).xmap(FluidResource::of, resource -> resource.toStack(FluidType.BUCKET_VOLUME));

    /**
     * Codec for a fluid resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<FluidResource> OPTIONAL_CODEC = ExtraCodecs.optionalEmptyMap(CODEC).xmap(
            optional -> optional.orElse(FluidResource.EMPTY),
            resource -> resource.isEmpty() ? Optional.empty() : Optional.of(resource));

    /**
     * Stream codec for a fluid resource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID), FluidResource::getHolder,
            DataComponentPatch.STREAM_CODEC, FluidResource::getComponentsPatch,
            FluidResource::of);

    /**
     * Creates a {@link FluidResource} for the given fluid stack. Note the amount is lost.
     *
     * @param stack stack to copy with a size of 1
     * @return If there were no patches on the stack's data components, the fluid's default resource will be returned, otherwise a new instance with the copied stack.
     */
    public static FluidResource of(FluidStack stack) {
        if (stack.isEmpty() || stack.isComponentsPatchEmpty()) {
            return of(stack.getFluid());
        }
        return new FluidResource(stack.copyWithAmount(FluidType.BUCKET_VOLUME));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static FluidResource of(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return EMPTY;
        return fluid.computeDefaultResource(f -> new FluidResource(new FluidStack(f, FluidType.BUCKET_VOLUME)));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     *
     * @param fluid Fluid to create the resource with.
     * @param patch Data components that should be on the resource instance.
     * @return a new {@link FluidResource}. If the fluid is empty, then {@link #EMPTY} will be returned; If the patch matches the default values the default instance of that fluid will be provided.
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static FluidResource of(Fluid fluid, DataComponentPatch patch) {
        return of(fluid.builtInRegistryHolder(), patch);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static FluidResource of(Holder<Fluid> fluid) {
        return of(fluid.value());
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     *
     * @param holder Fluid holder to create the resource with.
     * @param patch  Data components that should be on the resource instance.
     * @return a new {@link FluidResource}. If the fluid is empty, then {@link #EMPTY} will be returned; If the patch matches the default values the default instance of that fluid will be provided.
     * @throws IllegalStateException If the backing registry is unavailable or not yet ready.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     */
    public static FluidResource of(Holder<Fluid> holder, DataComponentPatch patch) {
        if (holder.value() == Fluids.EMPTY || patch.isEmpty()) {
            return of(holder.value());
        }
        return new FluidResource(new FluidStack(holder, FluidType.BUCKET_VOLUME, patch));
    }

    /**
     * A wrapped {@link FluidStack} which must never be modified or exposed. This will be a size of 1 so that we can make use
     * of the fact it is already an instance with a data component map.
     */
    private final FluidStack innerStack;

    private FluidResource(FluidStack stack) {
        this.innerStack = stack;
    }

    @Override
    public Fluid value() {
        return innerStack.getFluid();
    }

    /**
     * @return The {@link Fluid} of this resource from the inner {@link FluidStack}
     */
    public Fluid getFluid() {
        return value();
    }

    /**
     * @return the fluid holder of this resource
     */
    @Override
    public Holder<Fluid> getHolder() {
        return innerStack.getFluidHolder();
    }

    /**
     * @return the fluid type of this resource
     */
    public FluidType getFluidType() {
        return innerStack.getFluidType();
    }

    /**
     * Checks if this resource is empty. The resource will be empty if the fluid is {@link Fluids#EMPTY}.
     *
     * @return if this resource is empty
     */
    @Override
    public boolean isEmpty() {
        return innerStack.isEmpty();
    }

    @Override
    public FluidResource withMergedPatch(DataComponentPatch patch) {
        if (isEmpty() || patch.isEmpty())
            return this;

        FluidStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return FluidResource.of(stack);
    }

    @Override
    public <D> FluidResource with(DataComponentType<D> type, D data) {
        if (isEmpty()) return FluidResource.EMPTY;
        if (Objects.equals(get(type), data)) return this;

        FluidStack stack = innerStack.copy();
        stack.set(type, data);
        return FluidResource.of(stack);
    }

    //This is overridden to return FluidResource to allow method chaining
    @Override
    public <D> FluidResource with(Supplier<? extends DataComponentType<D>> type, D data) {
        return with(type.get(), data);
    }

    @Override
    public FluidResource without(DataComponentType<?> type) {
        if (isEmpty()) return FluidResource.EMPTY;
        if (get(type) == null) return this;

        FluidStack stack = innerStack.copy();
        stack.remove(type);
        return FluidResource.of(stack);
    }

    //This is overridden to return FluidResource to allow method chaining
    @Override
    public FluidResource without(Supplier<? extends DataComponentType<?>> type) {
        return without(type.get());
    }

    @Override
    public DataComponentMap getComponents() {
        return innerStack.immutableComponents();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    /**
     * Creates a {@link FluidStack} of the specified amount.
     *
     * @param amount The amount of the fluid the stack should have. Must be non-negative.
     * @throws IllegalArgumentException when amount is negative.
     */
    public FluidStack toStack(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) return FluidStack.EMPTY;
        return this.innerStack.copyWithAmount(amount);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    /**
     * @param fluidType Fluid type to check
     * @return {@code true} if the inner stack's fluid type is the same as the specified fluid type.
     */
    public boolean is(FluidType fluidType) {
        return innerStack.is(fluidType);
    }

    /**
     * {@return true if this resource matches the fluid and components of the passed stack}
     *
     * @param stack the fluid stack to check
     */
    public boolean matches(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(stack, innerStack);
    }

    /**
     * Tests a {@link FluidStack} predicate with the inner stack.
     *
     * @param predicate Predicate to perform the test with
     * @return {@code true} if the test passed
     */
    public boolean test(Predicate<FluidStack> predicate) {
        return predicate.test(innerStack);
    }

    /**
     * @return The hover name of the {@link FluidStack}
     */
    public Component getHoverName() {
        return innerStack.getHoverName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        FluidResource other = (FluidResource) obj;
        return FluidStack.isSameFluidSameComponents(this.innerStack, other.innerStack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(innerStack);
    }

    @Override
    public String toString() {
        //Fluid type string with patch count
        return getFluidType() + " [" + getComponentsPatch().size() + "]";
    }
}
