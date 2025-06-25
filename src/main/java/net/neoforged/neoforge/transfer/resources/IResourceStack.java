/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.transfer.IStackFactory;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;

/**
 * Represents the underlying instruction set for resource stacks.
 * This is provided as a helper and not necessary for a {@link IResourceHandler IResourceHandler} to function.
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
     * @param <R>           the resource type
     * @return a codec for a resource stack
     */
    static <R extends IResource, S extends IResourceStack<R>> Codec<S> codec(Codec<R> resourceCodec, IStackFactory<R, S> factory) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(IResourceStack<R>::resource),
                NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.NON_NEGATIVE_INT, "amount", 1)
                        .forGetter(IResourceStack<R>::amount))
                .apply(instance, factory));
    }

    /**
     * Creates a standard stream codec for a IResourceStack implementer of the specified resource type.
     */
    static <B extends FriendlyByteBuf, R extends IResource, S extends IResourceStack<R>> StreamCodec<B, S> streamCodec(StreamCodec<? super B, R> resourceCodec, IStackFactory<R, S> factory) {
        return StreamCodec.composite(
                resourceCodec, IResourceStack::resource,
                ByteBufCodecs.VAR_INT, IResourceStack::amount,
                factory);
    }

    /**
     * Ensures the resource is not null and the amount is non-negative, throws otherwise.
     */
    static void validate(IResource resource, int amount) {
        Objects.requireNonNull(resource, "Resource must not be null");
        ResourceHandlerUtil.isEmpty(resource, amount);
    }

    /**
     * @return the backing resource of the stack.
     */
    T resource();

    /**
     * @return the amount currently set in the stack. <strong>Must be non-negative</strong>.
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

    default boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return !(resource() instanceof IRegisteredResource<?> reg) || reg.isEnabled(enabledFeatures);
    }

    IResourceStack<T> withAmount(int newAmount);

    IResourceStack<T> shrink(int amount);

    IResourceStack<T> grow(int amount);

    IResourceStack<T> with(UnaryOperator<T> operator);

    /**
     * Creates a hashcode derived from a resource stack list. This is similar to how vanilla handles ItemStack lists.
     */
    static <T extends IResourceStack<?>> int hashTypes(Iterable<T> stacks) {
        int i = 0;
        //Like vanilla, the count is omitted in the hash comparison
        for (IResourceStack<?> resourceStack : stacks) {
            i = i * 31 + resourceStack.resource().hashCode();
        }
        return i;
    }
}
