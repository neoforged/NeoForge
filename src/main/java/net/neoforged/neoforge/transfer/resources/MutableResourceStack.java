/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable {@link IResource} and a <b>mutable</b> amount.
 * This is intended to be used when you know the amount is going be in flux, but the backing resource will be the same.
 * As an example, when storing a list of stacks, instead of creating a new object on heap, the backing int value can be mutated.
 * This, however, should not be used on things like {@link net.minecraft.core.component.DataComponentType DataComponents} and instead, for that use a {@link ResourceStack} or fully immutable structure.
 */
public final class MutableResourceStack<T extends IResource> implements IResourceStack<T> {
    public static <T extends IResource> MutableResourceStack<T> of(IResourceStack<T> stack) {
        return MutableResourceStack.of(stack.resource(), stack.amount());
    }

    public static <T extends IResource> MutableResourceStack<T> of(T resource, int amount) {
        return new MutableResourceStack<>(resource, amount);
    }

    private final T resource;
    private int amount;

    private MutableResourceStack(T resource, int amount) {
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
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> withAmount(int newAmount) {
        if (isEmpty()) return this;
        amount = isEmpty() ? 0 : newAmount;
        return this;
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> shrink(int amount) {
        if (isEmpty()) return this;
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> grow(int amount) {
        if (isEmpty()) return this;
        return withAmount(this.amount + amount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    @Override
    public MutableResourceStack<T> with(UnaryOperator<T> operator) {
        if (isEmpty()) return this;
        return new MutableResourceStack<>(operator.apply(resource), amount);
    }

    @Override
    public MutableResourceStack<T> mutable() {
        return this; // Already mutable, so we can return this
    }

    /**
     * @return a new immutable copy of this resource stack.
     */
    @Override
    public ResourceStack<T> immutable() {
        return ResourceStack.of(resource, amount); // Creates a new immutable resource stack with the backing data
    }

    @Override
    public MutableResourceStack<T> copy() {
        return MutableResourceStack.of(resource, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof IResourceStack<?> that)) return false;
        return Objects.equals(resource, that.resource()) && amount == that.amount();
    }

    @Override
    public int hashCode() {
        return resource.hashCode();
    }

    @Override
    public String toString() {
        return "%s(%d)".formatted(resource, amount);
    }
}
