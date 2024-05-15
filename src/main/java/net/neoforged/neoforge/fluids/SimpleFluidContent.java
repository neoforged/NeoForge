/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import org.jetbrains.annotations.Nullable;

/**
 * Stock data component class to hold a {@link FluidStack}.
 *
 * <p>A corresponding {@link DataComponentType} must be registered to use this class.
 */
public class SimpleFluidContent implements DataComponentHolder {
    public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);
    public static final Codec<SimpleFluidContent> CODEC = FluidStack.OPTIONAL_CODEC
            .xmap(SimpleFluidContent::new, content -> content.fluidStack);
    public static final StreamCodec<RegistryFriendlyByteBuf, SimpleFluidContent> STREAM_CODEC = FluidStack.OPTIONAL_STREAM_CODEC
            .map(SimpleFluidContent::new, content -> content.fluidStack);

    private final FluidStack fluidStack;

    @Nullable
    private ResourceStack<FluidResource> immutable;

    private SimpleFluidContent(FluidStack stack) {
        this.fluidStack = stack;
    }

    public static SimpleFluidContent copyOf(FluidStack fluidStack) {
        return fluidStack.isEmpty() ? EMPTY : new SimpleFluidContent(fluidStack.copy());
    }

    public static SimpleFluidContent of(FluidResource resource, int amount) {
        return resource.isBlank() || amount <= 0 ? EMPTY : new SimpleFluidContent(resource.toStack(amount));
    }

    public static SimpleFluidContent of(ResourceStack<FluidResource> resourceStack) {
        return resourceStack.isEmpty() ? EMPTY : SimpleFluidContent.of(resourceStack.resource(), resourceStack.amount());
    }

    public FluidStack copy() {
        return this.fluidStack.copy();
    }

    public ResourceStack<FluidResource> getImmutableStack() {
        ResourceStack<FluidResource> stack = immutable;
        if (stack == null) {
            stack = this.immutable = fluidStack.immutable();
        }
        return stack;
    }

    public Fluid getFluid() {
        return this.fluidStack.getFluid();
    }

    public Holder<Fluid> getFluidHolder() {
        return this.fluidStack.getFluidHolder();
    }

    public FluidType getFluidType() {
        return this.fluidStack.getFluidType();
    }

    public int getAmount() {
        return this.fluidStack.getAmount();
    }

    public FluidResource getResource() {
        return getImmutableStack().resource();
    }

    public boolean isEmpty() {
        return this.fluidStack.isEmpty();
    }

    public boolean is(TagKey<Fluid> tag) {
        return this.fluidStack.is(tag);
    }

    public boolean is(Fluid fluid) {
        return this.fluidStack.is(fluid);
    }

    public boolean is(Predicate<Holder<Fluid>> predicate) {
        return this.fluidStack.is(predicate);
    }

    public boolean is(Holder<Fluid> holder) {
        return this.fluidStack.is(holder);
    }

    public boolean is(HolderSet<Fluid> holders) {
        return this.fluidStack.is(holders);
    }

    public boolean is(FluidType fluidType) {
        return this.fluidStack.is(fluidType);
    }

    public boolean matches(FluidStack other) {
        return FluidStack.matches(this.fluidStack, other);
    }

    public boolean isSameFluid(FluidStack other) {
        return FluidStack.isSameFluid(this.fluidStack, other);
    }

    public boolean isSameFluid(FluidResource other) {
        return other.is(this.fluidStack.getFluid());
    }

    public boolean isSameFluidSameComponents(FluidStack other) {
        return FluidStack.isSameFluidSameComponents(this.fluidStack, other);
    }

    public boolean isSameFluidSameComponents(FluidResource other) {
        return other.matches(this.fluidStack);
    }

    public boolean isSameFluidSameComponents(SimpleFluidContent content) {
        return isSameFluidSameComponents(content.fluidStack);
    }

    @Override
    public DataComponentMap getComponents() {
        return fluidStack.getComponents();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SimpleFluidContent o)) {
            return false;
        } else {
            return FluidStack.matches(this.fluidStack, o.fluidStack);
        }
    }

    @Override
    public int hashCode() {
        return this.fluidStack.getAmount() * 31 + FluidStack.hashFluidAndComponents(this.fluidStack);
    }
}
