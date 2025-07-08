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
public final class ResourceStack<T extends IResource<T>> {
    /**
     * Creates a resource stack from a given resource and amount.
     * <p>
     * For custom resources, it is required to have an accessible EMPTY stack instance to use. Note, this is true even if
     * your resource type can never be empty. In that scenario, it would be more accurate to call it a default instance
     * rather than empty.
     * See {@link ItemResource#EMPTY} for an example:
     *
     * <pre>{@code
     * public static final ResourceStack<ItemResource> EMPTY_STACK = ResourceStack.constructEmptyReference(ItemResource.EMPTY);
     * }</pre>
     *
     * @param resource The resource to wrap the stack around.
     * @param amount   The amount of the resource the stack is holding.
     * @param <T>      The type of resource.
     * @return A new resource stack (or the empty instance if had been empty).
     * @see ItemResource#withAmount(int)
     * @see FluidResource#withAmount(int)
     * @throws IllegalArgumentException When the amount is negative
     */
    public static <T extends IResource<T>> ResourceStack<T> of(T resource, int amount) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) {
            var existingEmpty = resource.getEmptyResourceStackInstance();
            //noinspection ReplaceNullCheck, ConstantValue This allows assigning the empty instances instead of having a different method for it.
            if (existingEmpty != null) return existingEmpty;
            return new ResourceStack<>(resource, 0);
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
    public static <R extends IResource<R>> Codec<ResourceStack<R>> codec(Codec<R> resourceCodec, IStackFactory<R, ResourceStack<R>> stackFactory) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack<R>::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1).forGetter(ResourceStack<R>::amount))
                .apply(instance, stackFactory::create));
    }

    /**
     * Creates a standard stream codec for a ResourceStack of the specified resource type.
     *
     * @param resourceCodec The codec for the resource type.
     * @param stackFactory  The method used to create a new resource stack given a resource and an amount.
     *                      This is expected to handle returning the EMPTY instance when the stack would be empty.
     */
    public static <B extends FriendlyByteBuf, R extends IResource<R>> StreamCodec<B, ResourceStack<R>> streamCodec(StreamCodec<? super B, R> resourceCodec, IStackFactory<R, ResourceStack<R>> stackFactory) {
        return StreamCodec.composite(
                resourceCodec, ResourceStack::resource,
                ByteBufCodecs.VAR_INT, ResourceStack::amount,
                stackFactory::create);
    }

    /**
     * Creates a hashcode derived from a resource stack list. This is similar to how vanilla handles ItemStack lists.
     */
    public static <T extends ResourceStack<?>> int hashTypes(Iterable<T> stacks) {
        int i = 0;
        //Like vanilla, the count is omitted in the hash comparison
        for (ResourceStack<?> resourceStack : stacks) {
            i = i * 31 + resourceStack.resource().hashCode();
        }
        return i;
    }

    private final T resource;
    private final int amount;

    private ResourceStack(T resource, int amount) {
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
     */
    public ResourceStack<T> withAmount(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (amount == this.amount) return this;
        if (amount == 0 || resource.isEmpty()) return resource().getEmptyResourceStackInstance();
        return ResourceStack.of(resource, amount);
    }

    /**
     * @param amount Amount to shrink by. Must be non-negative.
     * @return A new immutable instance of this resource stack with an updated amount decreased by the specified {@code amount}.
     *         If the newAmount is 0 or the resource is empty, then the EMPTY instance for the resource will be returned.
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
     */
    public ResourceStack<T> grow(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        return withAmount(this.amount + amount);
    }

    /**
     * @return A new this instance with an updated resource should it have changed, otherwise it returns itself.
     */
    public ResourceStack<T> with(UnaryOperator<T> operator) {
        return with(operator, amount);
    }

    /**
     * @param amount the amount the new stack should be.
     * @return A new this instance with an updated resource should it have changed, otherwise it returns itself.
     */
    public ResourceStack<T> with(UnaryOperator<T> operator, int amount) {
        T result = operator.apply(resource);
        if (amount == 0) return result.getEmptyResourceStackInstance();

        if (result.equals(resource) && amount == amount()) return this;
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
        return "%s(%d)".formatted(resource, amount);
    }
}
