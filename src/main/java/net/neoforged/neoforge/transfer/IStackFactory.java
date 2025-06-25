/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import java.util.function.BiFunction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resources.IResource;

/**
 * Creates a stack of type {@code <S>} given a resource of type {@code <R>} and an amount.
 * It is expected that this handles cases where an empty resource or amount could occur.
 * In which case ensure you return the empty instanced value as necessary.
 * <p>
 * As an example would something like a ItemStack be used, it should {@link ItemStack#EMPTY} in the case either the resource was empty or the amount was 0.
 * <p>
 * Common factories are:
 * <li>{@code ItemResource::withAmount} - Returns an {@code ResourceStack<ItemResource>}</li>
 * <li>{@code ItemResource::toStack} - Returns an {@code ItemStack}</li>
 *
 * @param <R> The type of resource
 * @param <S> The type of the returned quantifiable stack
 */
public interface IStackFactory<R extends IResource, S> extends BiFunction<R, Integer, S> {
    @Override
    default S apply(R r, Integer integer) {
        return create(r, integer);
    }

    /**
     * Creates a stack of type {@code <S>} given a resource of type {@code <R>} and an amount.
     * It is expected that this handles cases where an empty resource or amount could occur.
     * In which case ensure you return the empty instanced value as necessary.
     * <p>
     * As an example would something like a ItemStack should {@link ItemStack#EMPTY ItemStack.EMPTY} in the case either the resource was empty or the amount was 0.
     */
    S create(R resource, int amount);
}
