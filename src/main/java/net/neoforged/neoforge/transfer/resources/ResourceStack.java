/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
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
public record ResourceStack<T extends IResource>(T resource, int amount) {
    /**
     * Creates a resource stack from a given resource and amount.
     *
     * @param resource The resource to wrap the stack around.
     * @param amount   The amount of the resource the stack is holding. Must be non-negative. Should only be 0 if the resource isEmpty
     * @see ItemResource#withAmount(int)
     * @see FluidResource#withAmount(int)
     * @throws IllegalArgumentException When the specified amount is 0 and the resource was not empty
     * @throws IllegalArgumentException When the specified amount is negative
     */
    public ResourceStack {
        Objects.requireNonNull(resource, "resource must not be null");
        TransferPreconditions.checkNonNegative(amount);
        if (resource.isEmpty()) {
            // if our resource was empty, we assign 0 as our amount.
            amount = 0;
        } else {
            // if the resource is not empty, but was passed in 0 for amount, this is invalid. This is to minimize misuse.
            if (amount == 0) {
                //We have to throw as we don't have access to the empty instance, nor can we defer to an instanced stack with a constructor.
                //This info would need to be provided by the resource in some way which we currently don't have.
                throw new IllegalArgumentException("An empty resource must be used when amount is 0; " + resource + "(0) is not allowed");
            }
        }
    }

    /**
     * Creates a codec with the resource being a field in the object. Should the resource or amount be empty, the empty resource stack instance
     * provided by the stack factory will be used.
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
     * @param stackFactory  The stack factory handling new instance creation or deferring to the empty stack instance.
     * @param <R>           the resource type
     * @return a codec for a resource stack
     */
    public static <R extends IResource> Codec<ResourceStack<R>> codec(Codec<R> resourceCodec, IStackFactory<R, ResourceStack<R>> stackFactory) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1).forGetter(ResourceStack::amount))
                .apply(instance, stackFactory::create));
    }

    /**
     * Creates a standard stream codec for a ResourceStack of the specified resource type. Should the resource or amount be empty, the empty resource stack instance
     * provided by the resource will be used.
     *
     * @param resourceCodec The codec for the resource type.
     * @param stackFactory  The stack factory handling new instance creation or deferring to the empty stack instance.
     * @param <R>           the resource type
     */
    public static <B extends FriendlyByteBuf, R extends IResource> StreamCodec<B, ResourceStack<R>> streamCodec(StreamCodec<? super B, R> resourceCodec, IStackFactory<R, ResourceStack<R>> stackFactory) {
        return StreamCodec.composite(
                resourceCodec, ResourceStack::resource,
                ByteBufCodecs.VAR_INT, ResourceStack::amount,
                stackFactory::create);
    }

    /**
     * Checks if the resource stack is empty, meaning that the amount is zero
     * or that the resource is {@link IResource#isEmpty() empty}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return ResourceHandlerUtil.isEmpty(resource(), amount());
    }

    @Override
    public String toString() {
        return resource + "(" + amount + ")";
    }
}
