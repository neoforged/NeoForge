/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Represents an immutable resource and an amount.
 * Can be seen as an immutable version of {@link ItemStack} or {@link FluidStack}.
 *
 * @param <T> the held resource type
 */
public record ResourceAmount<T extends IResource>(T resource, int amount) {
    public ResourceAmount {
        Objects.requireNonNull(resource, "resource");
    }

    /**
     * Creates a standard stream codec for a resource amount.
     */
    public static <B extends FriendlyByteBuf, T extends IResource> StreamCodec<B, ResourceAmount<T>> streamCodec(StreamCodec<? super B, T> resourceCodec) {
        return StreamCodec.composite(
                resourceCodec,
                ResourceAmount::resource,
                ByteBufCodecs.VAR_INT,
                ResourceAmount::amount,
                ResourceAmount::new);
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
    public ResourceAmount<T> withAmount(int newAmount) {
        return new ResourceAmount<>(resource, newAmount);
    }
}
