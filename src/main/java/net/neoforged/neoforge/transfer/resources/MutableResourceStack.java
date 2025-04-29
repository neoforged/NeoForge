/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import org.jetbrains.annotations.Range;

/**
 * Represents an immutable {@link IResource} and a mutable amount.
 * This is intended to be used when you know the amount is going be in flux, but the backing resource will be the same.
 * As an example, when storing a list of stacks, instead of creating a new object on heap, the backing int value can be mutated.
 * This, however, should not be used on things like {@link net.minecraft.core.component.DataComponentType DataComponents} and instead, for that use a {@link ResourceStack}.
 */
public final class MutableResourceStack<T extends IResource> implements IResourceStack<T> {
    /**
     * Creates a codec with the resource being a field in the object.
     * 
     * <pre>{@code
     * {
     *     "resource": {
     *         "id": "minecraft:water",
     *         "components": { ... }
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     *
     * @param resourceCodec a codec for the resource
     * @param <T>           The Resource type
     * @return A codec for a MutableResourceStack
     */
    public static <T extends IResource> Codec<MutableResourceStack<T>> codec(Codec<T> resourceCodec) {
        return IResourceStack.codec(resourceCodec, MutableResourceStack::new);
    }

    /**
     * Creates a codec where the fields for the resource are at the same level as the amount
     * 
     * <pre>{@code
     * {
     *    "id": "minecraft:water",
     *    "components": { ... },
     *    "amount": 1000
     * }
     * }</pre>
     *
     * @param resourceCodec a codec for the resource
     * @param <T>           The Resource type
     * @return A codec for a MutableResourceStack
     */
    public static <T extends IResource> Codec<MutableResourceStack<T>> flatCodec(Codec<T> resourceCodec) {
        return IResourceStack.flatCodec(resourceCodec, MutableResourceStack::new);
    }

    /**
     * Creates a standard stream codec for a <strong>mutable</strong> resource stack of the specified resource type.
     */
    public static <B extends FriendlyByteBuf, T extends IResource> StreamCodec<B, MutableResourceStack<T>> streamCodec(StreamCodec<? super B, T> resourceCodec) {
        return IResourceStack.streamCodec(resourceCodec, MutableResourceStack::new);
    }

    private final T resource;
    private int amount;

    public static <TResource extends IResource> NonNullList<MutableResourceStack<TResource>> nonNullListOfSize(int count, MutableResourceStack<TResource> resourceStack) {
        return NonNullList.withSize(count, resourceStack);
    }

    public static <TResource extends IResource> NonNullList<MutableResourceStack<TResource>> nonNullListOfSize(int count, TResource emptyResource) {
        return NonNullList.withSize(count, MutableResourceStack.of(emptyResource, 0));
    }

    public NonNullList<MutableResourceStack<T>> nonNullListOfSize(int count) {
        return nonNullListOfSize(count, this);
    }

    public MutableResourceStack(T resource, @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int amount) {
        Objects.requireNonNull(resource, "resource");
        this.resource = resource;
        this.amount = amount;
    }

    public static <T extends IResource> MutableResourceStack<T> of(ResourceStack<T> stack) {
        return of(stack.resource(), stack.amount());
    }

    public static <T extends IResource> MutableResourceStack<T> of(T resource, @Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int amount) {
        return new MutableResourceStack<>(resource, amount);
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
    public MutableResourceStack<T> withAmount(@Range(from = 0, to = ResourceHandlerUtil.PRETTY_MAX_INT) int newAmount) {
        amount = isEmpty() ? 0 : newAmount;
        return this;
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> shrink(int amount) {
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @return this instance with an updated amount.
     */
    @Override
    public MutableResourceStack<T> grow(int amount) {
        return withAmount(this.amount + amount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    @Override
    public MutableResourceStack<T> with(UnaryOperator<T> operator) {
        return new MutableResourceStack<>(operator.apply(resource), amount);
    }

    @Override
    public MutableResourceStack<T> mutable() {
        return this; // Already mutable, so we can return this
    }

    /**
     * @return a new immutable copy of this resource stack
     */
    @Override
    public ResourceStack<T> immutable() {
        return new ResourceStack<>(resource, amount); // Creates a new immutable resource stack with the backing data
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof IResourceStack<?> that)) return false;
        return Objects.equals(resource, that.resource()) && amount == that.amount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(resource, amount);
    }

    @Override
    public String toString() {
        return "%s(%d)".formatted(resource, amount);
    }
}
