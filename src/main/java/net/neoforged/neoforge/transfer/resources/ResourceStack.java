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
    //This should be the only instance that has a null instance empty parameter as it can't reference itself.
    @SuppressWarnings("DataFlowIssue")
    private static final ResourceStack<?> EMPTY = new ResourceStack<>(() -> true, 0, null);

    private final ResourceStack<T> emptyInstance;

    /**
     * Creates a resource stack from a given resource, amount, and a reference to the empty stack for the resource type.
     * <p>
     * For custom resources, it is required to have an accessible EMPTY stack instance to use.
     * See {@link ItemResource#EMPTY} for an example:
     * 
     * <pre>{@code
     * public static final ResourceStack<ItemResource> EMPTY_STACK = ResourceStack.constructEmptyReference(ItemResource.EMPTY);
     * }</pre>
     *
     * @param resource      The resource to wrap the stack around.
     * @param amount        The amount of the resource the stack is holding.
     * @param emptyInstance The empty stack reference when operations like {@link #withAmount(int)} are called.
     * @param <T>           The type of resource.
     * @return A new resource stack (or the empty instance if had been empty).
     * @see ItemResource#withAmount(int)
     * @see FluidResource#withAmount(int)
     */
    public static <T extends IResource> ResourceStack<T> of(T resource, int amount, ResourceStack<T> emptyInstance) {
        if (amount == 0 || resource.isEmpty()) {
            return emptyInstance;
        }
        return new ResourceStack<>(resource, amount, emptyInstance);
    }

    /**
     * Used only for initializing your Empty resource reference.
     * For items or fluids, don't construct your own, use {@link ItemResource#EMPTY} and {@link FluidResource#EMPTY} respectively.
     *
     * @return A new reference bound to your resource type.
     */
    public static <T extends IResource> ResourceStack<T> constructEmptyReference(T resource) {
        // noinspection unchecked
        return new ResourceStack<>(resource, 0, (ResourceStack<T>) EMPTY);
    }

    private final T resource;
    private final int amount;

    private ResourceStack(T resource, int amount, ResourceStack<T> emptyInstance) {
        IResourceStack.validate(resource, amount);
        this.resource = resource;
        this.amount = amount;
        this.emptyInstance = emptyInstance;
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
     *         If the newAmount is 0 or the resource is empty, then the EMPTY instance for the resource will be returned.
     */
    @Override
    public ResourceStack<T> withAmount(int newAmount) {
        if (resource.isEmpty()) return this;
        if (newAmount == 0) return emptyInstance;
        if (newAmount == amount) return this;
        return ResourceStack.of(resource, newAmount, emptyInstance);
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
        return ResourceStack.of(operator.apply(resource), amount, emptyInstance);
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
