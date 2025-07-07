/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Immutable combination of a {@link Fluid} and data components.
 * Similar to a {@link FluidStack}, but immutable and without amount information.
 */
public final class FluidResource implements IDataComponentHolderResource<Fluid, FluidResource> {
    //TODO provide documentation on all methods
    public static final FluidResource EMPTY = new FluidResource(FluidStack.EMPTY);
    public static final ResourceStack<FluidResource> EMPTY_STACK = ResourceStack.of(FluidResource.EMPTY, 0);

    /**
     * Codec for a fluid resource.
     * Same format as {@link FluidStack#fixedAmountCodec}.
     * Does <b>not</b> accept empty resources.
     */
    public static final Codec<FluidResource> CODEC = Codec.lazyInitialized(() -> FluidStack.fixedAmountCodec(FluidType.BUCKET_VOLUME).xmap(FluidResource::of, FluidResource::toStack));

    /**
     * Codec for a fluid resource. Same format as {@link #CODEC}, and also accepts empty resources.
     */
    public static final Codec<FluidResource> OPTIONAL_CODEC = Codec.lazyInitialized(() -> ExtraCodecs.optionalEmptyMap(CODEC).xmap(FluidResource::fromOptional, FluidResource::asOptional));

    /**
     * A codec for a {@code ResourceStack<FluidResource>} serializing the resource and the amount. Can accept empty resources.
     */
    public static final Codec<ResourceStack<FluidResource>> RESOURCE_STACK_CODEC = Codec.lazyInitialized(() -> ResourceStack.codec(OPTIONAL_CODEC, FluidResource::withAmount));

    /**
     * Stream codec for a fluid resource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.FLUID), FluidResource::getHolder,
            DataComponentPatch.STREAM_CODEC, FluidResource::getComponentsPatch,
            FluidResource::of);

    /**
     * Stream codec for a resource stack backed by an FluidResource. Accepts empty resources.
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceStack<FluidResource>> RESOURCE_STACK_STREAM_CODEC = ResourceStack.streamCodec(FluidResource.STREAM_CODEC, FluidResource::withAmount);

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static FluidResource fromOptional(Optional<FluidResource> optional) {
        return optional.orElse(FluidResource.EMPTY);
    }

    public Optional<FluidResource> asOptional() {
        return isEmpty() ? Optional.empty() : Optional.of(this);
    }

    /**
     * A helper method to quickly construct a {@link FluidStack} from a ResourceStack
     * 
     * @param resourceStack The resource resourceStack with the fluid resource and amount
     * @return A new fluid stack with the same size as the resourceStack.
     */
    public static FluidStack fluidStackOf(ResourceStack<FluidResource> resourceStack) {
        return resourceStack.resource().toStack(resourceStack.amount());
    }

    /**
     * This is used only for registry, you should not use this method!
     */
    @ApiStatus.Internal
    public static FluidResource invalidateDefault(Fluid fluid) {
        if (fluid == Fluids.EMPTY) return EMPTY;
        return new FluidResource(new FluidStack(fluid, 1));
    }

    /**
     * Creates an {@link FluidResource} using the default or copy of the passed in fluid stack.
     *
     * @param fluidStack stack to copy
     * @return If there were no patches on the stack's data components, the fluid's default resource will be returned, otherwise a new instance with the copied stack.
     */
    public static FluidResource of(FluidStack fluidStack) {
        if (fluidStack.isEmpty()) return FluidResource.EMPTY;

        if (fluidStack.isComponentsPatchEmpty())
            return fluidStack.getFluid().getDefaultResource();

        return new FluidResource(fluidStack.copyWithAmount(1));
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     *
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     * @throws IllegalStateException If the underlying default FluidResource has not been yet initialized.
     */
    public static FluidResource of(Holder<Fluid> fluid) {
        return of(fluid.value());
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     * 
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     * @throws IllegalStateException If the underlying default FluidResource has not been yet initialized.
     */
    public static FluidResource of(Fluid fluid) {
        return of(fluid, DataComponentPatch.EMPTY);
    }

    /**
     * <strong>Note:</strong> This cannot be called before your fluid is registered
     * 
     * @throws IllegalStateException If the backing registry is unavailable.
     * @throws NullPointerException  If the underlying Holder has not been populated (the target object is not registered).
     * @throws IllegalStateException If the underlying default FluidResource when used has not been yet initialized.
     */
    public static FluidResource of(Holder<Fluid> fluid, DataComponentPatch patch) {
        return of(fluid.value(), patch);
    }

    public static FluidResource of(Fluid fluid, DataComponentPatch patch) {
        if (fluid == Fluids.EMPTY) return EMPTY;
        if (patch.isEmpty()) return fluid.getDefaultResource();
        return new FluidResource(new FluidStack(fluid, 1, patch));
    }

    /**
     * We wrap a fluid stack which must never be modified.
     */
    //This is package private to provide Unsafe access in the scenarios of avoiding allocation
    // when being used in readonly context. A getInnerStack method could be done, but serves
    // no functional difference in this case since the field is marked final.
    private final FluidStack innerStack;

    /**
     * Lazily initialized.
     */
    @Nullable
    private ItemResource filledBucket;

    private FluidResource(FluidStack innerStack) {
        this.innerStack = innerStack;
    }

    /**
     * @return The {@link Fluid} of this resource from the inner {@link FluidStack}
     */
    @Override
    public Fluid getInstanceValue() {
        return innerStack.getFluid();
    }

    /**
     * @return the fluid holder of this resource
     */
    @Override
    public Holder<Fluid> getHolder() {
        return innerStack.getFluidHolder();
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

    /**
     * Returns a copy of this resource with the patch applied.
     *
     * @param patch the patch to apply
     * @return the new resource
     */
    @Override
    public FluidResource withPatch(DataComponentPatch patch) {
        FluidStack stack = innerStack.copy();
        stack.applyComponents(patch);
        return new FluidResource(stack);
    }

    /**
     * Returns a copy of this resource with the set data component.
     *
     * @param type the type of data component
     * @param data the data to set
     * @param <D>  the type of data component
     * @return the new resource
     */
    @Override
    public <D> FluidResource with(DataComponentType<D> type, D data) {
        FluidStack stack = innerStack.copy();
        stack.set(type, data);
        return new FluidResource(stack);
    }

    /**
     * Returns a copy of this resource with the data component removed.
     *
     * @param type The type of data component
     * @return The new resource
     */
    @Override
    public FluidResource without(DataComponentType<?> type) {
        FluidStack stack = innerStack.copy();
        stack.remove(type);
        return new FluidResource(stack);
    }

    /**
     * @return the fluid type of this resource
     */
    public FluidType getFluidType() {
        return innerStack.getFluidType();
    }

    @Override
    public DataComponentMap getComponents() {
        if (innerStack.isEmpty()) return DataComponentMap.EMPTY;
        return innerStack.getComponents().toImmutableMap();
    }

    @Override
    public DataComponentPatch getComponentsPatch() {
        return innerStack.getComponentsPatch();
    }

    public FluidStack toStack(int amount) {
        return amount == 0 || this.isEmpty() ? FluidStack.EMPTY : this.innerStack.copyWithAmount(amount);
    }

    /**
     * @return A {@link FluidStack} copy of the inner stack. The size is by default {@value FluidType#BUCKET_VOLUME}
     */
    public FluidStack toStack() {
        return toStack(FluidType.BUCKET_VOLUME);
    }

    @Override
    public boolean isComponentsPatchEmpty() {
        return innerStack.isComponentsPatchEmpty();
    }

    public boolean is(FluidType fluidType) {
        return innerStack.is(fluidType);
    }

    public boolean is(FluidStack stack) {
        return FluidStack.isSameFluidSameComponents(stack, innerStack);
    }

    public boolean test(Predicate<FluidStack> predicate) {
        return predicate.test(innerStack);
    }

    public ItemResource getFilledBucket() {
        if (filledBucket == null) {
            filledBucket = ItemResource.of(innerStack.getFluidType().getBucket(innerStack));
        }
        return filledBucket;
    }

    /**
     * Returns a sound to play when a certain action is performed. If no
     * sound is present, then the sound will be {@code null}.
     *
     * @param action the action being performed
     * @return the sound to play when performing the action
     */
    @Nullable
    public SoundEvent getSound(SoundAction action) {
        return innerStack.getFluidType().getSound(innerStack, action);
    }

    /**
     * @return The hover name of the {@link FluidStack}
     */
    public Component getHoverName() {
        return innerStack.getHoverName();
    }

    @Override
    public ResourceStack<FluidResource> getEmptyResourceStackInstance() {
        return FluidResource.EMPTY_STACK;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof FluidResource other && other.is(innerStack);
    }

    @Override
    public int hashCode() {
        return FluidStack.hashFluidAndComponents(innerStack);
    }

    @Override
    public String toString() {
        return innerStack.getFluid().getFluidType().toString();
    }

    /**
     * @return the full value and data components in string form
     */
    public String toExpandedString() {
        if (isComponentsPatchEmpty()) {
            return toString();
        } else {
            return "%s %s".formatted(getInstanceValue(), getComponentsPatch().toString());
        }
    }
}
