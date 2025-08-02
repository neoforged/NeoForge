/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.TransferPreconditions;

/**
 * Represents an immutable {@link IResource} and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 */
public final class ResourceStack<T extends IResource> {
    /**
     * Creates a resource stack from a given resource and amount. Should the amount be 0 or resource be empty, the empty
     * resource stack instance provided by {@link IResource#getEmptyInfo() the resource} will be returned.
     * This means that you cannot have a resource in a resource stack that is non-empty and an amount of 0.
     *
     * @param resource The resource to wrap the stack around.
     * @param amount   The amount of the resource the stack is holding. Must be non-negative.
     * @param <T>      The type of resource.
     * @return A new resource stack (or the empty instance if had been empty).
     * @see ItemResource#withAmount(int)
     * @see FluidResource#withAmount(int)
     * @throws IllegalArgumentException When the amount is negative.
     * @throws ClassCastException       When the info resource type does not match the resource class type. This indicates a problem with the resource implementation.
     */
    public static <T extends IResource> ResourceStack<T> of(T resource, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (ResourceHandlerUtil.isEmpty(resource, amount)) {
            ResourceStack<?> emptyStack = resource.getEmptyInfo().emptyResourceStack();
            //noinspection unchecked This is expected to crash if the types don't line up as documented above.
            return (ResourceStack<T>) emptyStack;
        }
        return new ResourceStack<>(resource, amount);
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
     * @param <R>           the resource type
     * @return a codec for a resource stack
     */
    public static <R extends IResource> Codec<ResourceStack<R>> codec(Codec<R> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1).forGetter(ResourceStack::amount))
                .apply(instance, ResourceStack::of));
    }

    /**
     * Creates a standard stream codec for a ResourceStack of the specified resource type.
     *
     * @param resourceCodec The codec for the resource type.
     * @param stackFactory  The method used to create a new resource stack given a resource and an amount.
     *                      This is expected to handle returning the EMPTY instance when the stack would be empty.
     */
    public static <B extends FriendlyByteBuf, R extends IResource> StreamCodec<B, ResourceStack<R>> streamCodec(StreamCodec<? super B, R> resourceCodec, IStackFactory<R, ResourceStack<R>> stackFactory) {
        return StreamCodec.composite(
                resourceCodec, ResourceStack::resource,
                ByteBufCodecs.VAR_INT, ResourceStack::amount,
                stackFactory::create);
    }

    /**
     * Creates a hashcode derived from a resource stack list. This is similar to how vanilla handles ItemStack lists.
     */
    public static <T extends IResource> int hashTypes(Iterable<ResourceStack<T>> stacks) {
        int i = 0;
        //Like vanilla, the count is omitted in the hash comparison
        for (ResourceStack<T> resourceStack : stacks) {
            i = i * 31 + resourceStack.resource().hashCode();
        }
        return i;
    }

    private final T resource;
    private final int amount;

    ResourceStack(T resource, int amount) {
        this.resource = resource;
        this.amount = amount;
    }

    public T resource() {
        return resource;
    }

    public int amount() {
        return amount;
    }

    /**
     * @param amount Amount the new resource stack should be. Must be non-negative.
     *
     * @return A new immutable instance of this resource stack with an updated amount.
     *         If the amount is 0 or the resource is empty, then the EMPTY instance for the resource will be returned.
     *
     * @throws IllegalArgumentException when {@code amount} is negative.
     */
    public ResourceStack<T> withAmount(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == this.amount) return this;
        return ResourceStack.of(resource, amount);
    }

    /**
     * @param amount Amount to shrink by. Must be non-negative.
     * @return A new immutable instance of this resource stack with an updated amount decreased by the specified {@code amount}.
     *         If the newAmount is 0 or the resource is empty, then the EMPTY instance for the resource will be returned.
     *
     * @throws IllegalArgumentException when {@code amount} is negative.
     *
     * @see #withAmount(int)
     */
    public ResourceStack<T> shrink(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @param amount Amount to grow by. Must be non-negative.
     *
     * @return A new immutable instance of this resource stack with an amount increased by the specified {@code amount}.
     *         If the resource was already empty, then the EMPTY instance will be returned instead
     *
     * @throws IllegalArgumentException when {@code amount} is negative.
     */
    public ResourceStack<T> grow(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        int newAmount = this.amount + amount;
        if (newAmount < 0) newAmount = Integer.MAX_VALUE;
        return withAmount(newAmount);
    }

    /**
     * @return A new this instance with an updated resource should it have changed, otherwise it returns itself.
     * @throws IllegalArgumentException when {@code amount} is negative.
     */
    public ResourceStack<T> with(UnaryOperator<T> operator) {
        return with(operator, amount);
    }

    /**
     * @param amount the amount the new stack should be.
     * @return A new this instance with an updated resource should it have changed, otherwise it returns itself.
     * @throws IllegalArgumentException when {@code amount} is negative.
     */
    public ResourceStack<T> with(UnaryOperator<T> operator, int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == 0) return this;
        T result = operator.apply(resource);
        if (amount == amount() && result.equals(resource)) return this;
        return ResourceStack.of(result, amount);
    }

    /**
     * Checks if this is empty, meaning that the amount is zero
     * or that the resource is {@link IResource#isEmpty() empty}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return ResourceHandlerUtil.isEmpty(resource(), amount());
    }

    /**
     * A helper method to convert the resource stack to a given type such as an {@link ItemStack} or {@link FluidStack}.
     *
     * @param stackFactory Factory used to create the new instance
     * @return Stack instance of type {@code T} using the resource and amount of the resource stack.
     *
     * @param <S> Type of stack
     *
     * @see ItemResource#toStack(int)
     * @see ItemResource#withAmount(int)
     */
    public <S> S as(IStackFactory<T, S> stackFactory) {
        return stackFactory.create(resource, amount);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ResourceStack<?> that = (ResourceStack<?>) obj;
        return amount == that.amount && resource.equals(that.resource);
    }

    @Override
    public int hashCode() {
        return 31 * resource.hashCode() + Integer.hashCode(amount);
    }

    @Override
    public String toString() {
        return resource + "(" + amount + ")";
    }
}
