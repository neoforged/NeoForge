/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Represents an immutable resource and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 *
 * @param <T> the held resource type
 */
public record ResourceStack<T extends IResource>(T resource, int amount) {
    public ResourceStack {
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Creates a standard stream codec for a resource amount.
     */
    public static <B extends FriendlyByteBuf, T extends IResource> StreamCodec<B, ResourceStack<T>> streamCodec(StreamCodec<? super B, T> resourceCodec) {
        return StreamCodec.composite(
                resourceCodec,
                ResourceStack::resource,
                ByteBufCodecs.VAR_INT,
                ResourceStack::amount,
                ResourceStack::new);
    }

    /**
     * Creates a codec with the resource being a field in the object.
     * <pre>{@code
     * {
     *     "resource": {
     *         "id": "minecraft:water",
     *         "components": { ... }
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     * @param resourceCodec a codec for the resource
     * @return a codec for a resource stack
     * @param <T> the resource type
     */
    public static <T extends IResource> Codec<ResourceStack<T>> codec(Codec<T> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack::resource),
                Codec.INT.fieldOf("amount").forGetter(ResourceStack::amount)
        ).apply(instance, ResourceStack::new));
    }

    /**
     * Creates a codec where the fields for the resource are at the same level as the amount
     * <pre>{@code
     * {
     *    "id": "minecraft:water",
     *    "components": { ... },
     *    "amount": 1000
     * }
     * }</pre>
     *
     * @param resourceCodec
     * @return
     * @param <T>
     */
    public static <T extends IResource> Codec<ResourceStack<T>> flatCodec(MapCodec<T> resourceCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.forGetter(ResourceStack::resource),
                Codec.INT.fieldOf("amount").forGetter(ResourceStack::amount)
        ).apply(instance, ResourceStack::new));
    }

    /**
     * Checks if this is empty, meaning that the amount is not positive
     * or that the resource is {@link IResource#isEmpty() blank}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return amount <= 0 || resource.isEmpty();
    }

    /**
     * @return a copy of this instance with an updated amount.
     */
    public ResourceStack<T> withAmount(int newAmount) {
        return new ResourceStack<>(resource, newAmount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    public ResourceStack<T> shrink(int amount) {
        return withAmount(Math.max(this.amount - amount, 0));
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    public ResourceStack<T> grow(int amount) {
        return withAmount(this.amount + amount);
    }

    /**
     * @return a copy of this instance with an updated resource.
     */
    public ResourceStack<T> with(UnaryOperator<T> operator) {
        return new ResourceStack<>(operator.apply(resource), amount);
    }
}