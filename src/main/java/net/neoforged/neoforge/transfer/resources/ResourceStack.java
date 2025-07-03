/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.flag.FeatureFlagSet;
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
    //TODO documentation post slice
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
     * @throws NullPointerException When the empty instance is null
     * @throws ReportedException    When the amount is negative
     */
    public static <T extends IResource> ResourceStack<T> of(T resource, int amount, ResourceStack<T> emptyInstance) {
        if (emptyInstance == null) throw new NullPointerException("Empty instance must not be null");
        if (TransferPreconditions.checkNonNegative(amount) == 0 || resource.isEmpty()) {
            return emptyInstance;
        }
        return new ResourceStack<>(resource, amount, emptyInstance);
    }

    /**
     * Used only for initializing your Empty resource reference.
     * For items or fluids, don't construct your own, use {@link ItemResource#EMPTY} and {@link FluidResource#EMPTY} respectively.
     *
     * @return A new reference bound to your resource type.
     * @throws IllegalArgumentException When the resource is non-empty
     */
    public static <T extends IResource> ResourceStack<T> constructEmptyReference(T resource) {
        if (!resource.isEmpty()) throw new IllegalArgumentException("Resource must be empty");
        // noinspection unchecked
        return new ResourceStack<>(resource, 0, (ResourceStack<T>) EMPTY);
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
    public static <R extends IResource> Codec<ResourceStack<R>> codec(Codec<R> resourceCodec, IStackFactory<R, ResourceStack<R>> factory) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack<R>::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1).forGetter(ResourceStack<R>::amount))
                .apply(instance, factory));
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
                stackFactory);
    }

    /**
     * Ensures the resource is not null and the amount is non-negative, throws otherwise.
     *
     * @throws NullPointerException     When resource is null
     * @throws IllegalArgumentException When amount is negative
     */
    public static void validate(IResource resource, int amount) {
        Objects.requireNonNull(resource, "Resource must not be null");
        ResourceHandlerUtil.isEmpty(resource, amount);
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
    private final ResourceStack<T> emptyInstance;

    // A note for future debugging. Empty instance should only be null in one specific case,
    // which is the ResourceStack.EMPTY instance reference.
    private ResourceStack(T resource, int amount, ResourceStack<T> emptyInstance) {
        ResourceStack.validate(resource, amount);
        this.resource = resource;
        this.amount = amount;
        this.emptyInstance = emptyInstance;
    }

    public T resource() {
        return resource;
    }

    public int amount() {
        return amount;
    }

    /**
     * @return A new immutable instance of this resource stack with an updated amount.
     *         If the amount is 0 or the resource is empty, then the EMPTY instance for the resource will be returned.
     */
    public ResourceStack<T> withAmount(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        if (resource.isEmpty() || amount == 0) return emptyInstance;
        if (amount == this.amount) return this;
        return ResourceStack.of(resource, amount, emptyInstance);
    }

    /**
     * @return A new immutable instance of this resource stack with an updated amount decreased by the specified {@code amount}.
     *         If the newAmount is 0 or the resource is empty, then the EMPTY instance for the resource will be returned.
     * @see #withAmount(int)
     */
    public ResourceStack<T> shrink(int amount) {
        TransferPreconditions.checkNonNegative(amount);
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @return A new immutable instance of this resource stack with an amount increased by the specified {@code amount}.
     *         If the resource was already empty, then the EMPTY instance will be returned instead
     */
    public ResourceStack<T> grow(int amount) {
        return withAmount(this.amount + amount);
    }

    /**
     * @return A new this instance with an updated resource should it have changed, otherwise it returns itself.
     */
    public ResourceStack<T> with(UnaryOperator<T> operator) {
        T result = operator.apply(resource);
        if (result.equals(resource)) return this;
        return ResourceStack.of(result, amount, emptyInstance);
    }

    /**
     * Checks if this is empty, meaning that the amount is not positive
     * or that the resource is {@link IResource#isEmpty() empty}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return amount() == 0 || resource().isEmpty();
    }

    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return !(resource() instanceof IRegisteredResource<?> reg) || reg.isEnabled(enabledFeatures);
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

    //This should be the only instance that has a null instance empty parameter as it can't reference itself.
    @SuppressWarnings("DataFlowIssue")
    private static final ResourceStack<?> EMPTY = new ResourceStack<>(new IResource() {
        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public ResourceStack<? extends IResource> withAmount(int amount) {
            return EMPTY;
        }
    }, 0, null);
}
