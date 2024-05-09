/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.storage;

import java.util.Objects;
import java.util.function.UnaryOperator;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an immutable resource and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 *
 * @param <T> the held resource type
 */
public record ResourceStack<T extends IResource>(T resource, int amount) implements DataComponentHolder {
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
     * Checks if this is empty, meaning that the amount is not positive
     * or that the resource is {@link IResource#isBlank() blank}.
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return amount <= 0 || resource.isBlank();
    }

    /**
     * Returns a copy of this instance with an updated amount.
     */
    public ResourceStack<T> withAmount(int newAmount) {
        return new ResourceStack<>(resource, newAmount);
    }

    public ResourceStack<T> shrink(int amount) {
        return withAmount(this.amount - amount);
    }

    public ResourceStack<T> grow(int amount) {
        return withAmount(this.amount + amount);
    }

    public ResourceStack<T> with(UnaryOperator<T> operator) {
        return new ResourceStack<>(operator.apply(resource), amount);
    }

    @Override
    public @NotNull DataComponentMap getComponents() {
        return resource.getComponents();
    }
}