/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Represents the underlying instruction set for mutable and immutable resource stacks.
 * This is provided as a helper and not necessary for a {@link net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler IResourceHandler} to function.
 *
 * @param <T> resource type
 */
public interface IResourceStack<T extends IResource> {
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
     * @param <R>   the resource type
     * @return a codec for a resource stack
     */
    static <R extends IResource, S extends IResourceStack<R>> Codec<S> codec(Codec<R> resourceCodec, BiFunction<R, Integer, S> factory) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(IResourceStack<R>::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1)
                        .forGetter(IResourceStack<R>::amount))
                .apply(instance, factory));
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
     * @param resourceCodec Backing resource codec
     * @param factory       Constructor of IResourceStack implementation
     * @param <R>   The resource type
     * @return Codec for the specified IResourceStack implementer
     */
    static <R extends IResource, S extends IResourceStack<R>> Codec<S> flatCodec(Codec<R> resourceCodec, BiFunction<R, Integer, S> factory) {
        return RecordCodecBuilder.create(instance -> instance.group(
                MapCodec.assumeMapUnsafe(resourceCodec).forGetter(IResourceStack<R>::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1)
                        .forGetter(IResourceStack<R>::amount))
                .apply(instance, factory));
    }

    /**
     * Creates a standard stream codec for a IResourceStack implementer of the specified resource type.
     */
    static <B extends FriendlyByteBuf, R extends IResource, S extends IResourceStack<R>> StreamCodec<B, S> streamCodec(StreamCodec<? super B, R> resourceCodec, BiFunction<R, Integer, S> factory) {
        return StreamCodec.composite(
                resourceCodec, IResourceStack::resource,
                ByteBufCodecs.VAR_INT, IResourceStack::amount,
                factory);
    }

    /**
     * @return the backing resource of the stack.
     */
    T resource();

    /**
     * @return the amount currently set in the stack
     */
    int amount();

    /**
     * Checks if this is empty, meaning that the amount is not positive
     * or that the resource is {@link IResource#isEmpty() empty}.
     *
     * @return {@code true} if empty
     */
    default boolean isEmpty() {
        return amount() <= 0 || resource().isEmpty();
    }

    IResourceStack<T> withAmount(int newAmount);

    IResourceStack<T> shrink(int amount);

    IResourceStack<T> grow(int amount);

    IResourceStack<T> with(UnaryOperator<T> operator);

    // These methods allow a simple helper to reduce unnecessary allocation if they are already an instance of the correct type, otherwise, a new one is created.
    /**
     * @return a mutable resource stack that allows the amount to be changeable without the underlying resource data being set.
     */
    MutableResourceStack<T> mutable();

    /**
     * @return an immutable resource stack
     */
    ResourceStack<T> immutable();

    //Useful when dealing with snapshots
    IResourceStack<T> copy();

    /**
     * Creates a hashcode derived from a resource stack list. This is similar to how vanilla handles ItemStack lists.
     */
    static <T extends IResourceStack<?>> int hashCode(Iterable<T> stacks) {
        int i = 0;
        //Like vanilla, the count is omitted in the hash comparison
        for (IResourceStack<?> resourceStack : stacks) {
            i = i * 31 + resourceStack.resource().hashCode();
        }
        return i;
    }
}
