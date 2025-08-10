/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;

/**
 * Creates a stack of type {@code <S>} given a resource of type {@code <R>} and a non-negative amount.
 * It is expected that this handles cases where an empty resource or amount could occur.
 * In which case, ensure you return an existing empty instance as necessary instead of a new instance.
 * <p>
 * As an example should ItemStack be used, it would {return @link ItemStack#EMPTY} in the case either the resource was empty or the amount was 0.
 * <p>
 * Common factories are:
 * <li>{@code FluidResource::toStack} - Returns a {@code FluidStack}</li>
 * <li>{@code ItemResource::toStack} - Returns an {@code ItemStack}</li>
 *
 * @see FluidResource#toStack(int)
 * @see ItemResource#toStack(int)
 * @param <R> The type of resource
 * @param <S> The type of the returned quantifiable stack
 */
@FunctionalInterface
public interface IStackFactory<R extends IResource, S> {
    /**
     * Creates a stack of type {@code <S>} given a resource of type {@code <R>} and an amount.
     * It is expected that this handles cases where an empty resource or amount could occur.
     * In which case ensure you return the empty instanced value as necessary.
     * <p>
     * As an example would something like a ItemStack should {@link ItemStack#EMPTY ItemStack.EMPTY} in the case either the resource was empty or the amount was 0.
     */
    S create(R resource, int amount);
}
