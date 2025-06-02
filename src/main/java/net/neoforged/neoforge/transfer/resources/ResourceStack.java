/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable {@link IResource} and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 *
 * @param <T> the held resource type
 */
public record ResourceStack<T extends IResource>(T resource, int amount) implements IResourceStack<T> {
    public ResourceStack {
        Objects.requireNonNull(resource, "Resource must not be null");
    }

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
     * @param <T>           the resource type
     * @return a codec for a resource stack
     */
    public static <T extends IResource> Codec<ResourceStack<T>> codec(Codec<T> resourceCodec) {
        return IResourceStack.codec(resourceCodec, ResourceStack::new);
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
     * @param <T> The resource type
     * @return A flat codec that represents the ResourceStack
     */
    public static <T extends IResource> Codec<ResourceStack<T>> flatCodec(Codec<T> resourceCodec) {
        return IResourceStack.flatCodec(resourceCodec, ResourceStack::new);
    }

    /**
     * Creates a standard stream codec for a resource stack of the specified resource type.
     */
    public static <B extends FriendlyByteBuf, T extends IResource> StreamCodec<B, ResourceStack<T>> streamCodec(StreamCodec<? super B, T> resourceCodec) {
        return IResourceStack.streamCodec(resourceCodec, ResourceStack::new);
    }

    public static <T extends IResource> ResourceStack<T> of(IResourceStack<T> stack) {
        return of(stack.resource(), stack.amount());
    }

    public static <T extends IResource> ResourceStack<T> of(T resource, @Range(from = 0, to = ResourceHandlerUtil.MAX) int amount) {
        return new ResourceStack<>(resource, amount);
    }


    /**
     * @return a copy of this instance with an updated amount.
     */
    @Override
    public ResourceStack<T> withAmount(int newAmount) {
        return new ResourceStack<>(resource, newAmount);
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
        return new ResourceStack<>(operator.apply(resource), amount);
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

    /**
     * @return a copy of this instance as a mutable resource stack.
     */
    @Override
    public MutableResourceStack<T> mutable() {
        return MutableResourceStack.of(this);
    }

    /**
     * @return this instance.
     */
    @Override
    public ResourceStack<T> immutable() {
        return this;
    }

    @Override
    public IResourceStack<T> copy() {
        return of(resource, amount);
    }
    @Override
    public String toString() {
        return "%s(%d)".formatted(resource, amount);
    }
}
