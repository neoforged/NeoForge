/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Represents an immutable {@link IResource} and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 */
public final class ResourceStack<T extends IResource> implements IResourceStack<T> {
    public static <T extends IResource> ResourceStack<T> of(IResourceStack<T> stack) {
        return ResourceStack.of(stack.resource(), stack.amount());
    }

    public static <T extends IResource> ResourceStack<T> of(T resource, int amount) {
        return new ResourceStack<>(resource, amount);
    }

    private final T resource;
    private final int amount;

    private ResourceStack(T resource, int amount) {
        IResourceStack.validate(resource, amount);
        this.resource = resource;
        this.amount = amount;
    }

    @Override
    public T resource() {
        return resource;
    }

    @Override
    public int amount() {
        return amount;
    }

    /**
     * @return a copy of this instance with an updated amount.
     */
    @Override
    public ResourceStack<T> withAmount(int newAmount) {
        if (newAmount == amount) return this;
        return ResourceStack.of(resource, newAmount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    @Override
    public ResourceStack<T> shrink(int amount) {
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    @Override
    public ResourceStack<T> grow(int amount) {
        return withAmount(this.amount + amount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    @Override
    public ResourceStack<T> with(UnaryOperator<T> operator) {
        return ResourceStack.of(operator.apply(resource), amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof IResourceStack<?> that)) return false;
        return Objects.equals(resource, that.resource()) && amount == that.amount();
    }

    //TODO verify if we should hash without amount. This was to match FluidStack and ItemStack implementations
    @Override
    public int hashCode() {
        return 31 * (31 + resource.hashCode()) + Integer.hashCode(amount);
    }

    @Override
    public String toString() {
        return "%s(%d)".formatted(resource, amount);
    }
}
