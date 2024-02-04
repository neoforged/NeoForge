/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Special {@link DeferredHolder} for {@link Fluid Fluids}.
 *
 * @param <T> The specific {@link Fluid} type.
 */
public class DeferredFluid<T extends Fluid> extends DeferredHolder<Fluid, T> {
    /**
     * Creates a new {@link FluidStack} with a default size of 1 from this {@link Fluid}
     */
    public FluidStack toStack() {
        return toStack(1);
    }

    /**
     * Creates a new {@link FluidStack} with the given size from this {@link Fluid}
     *
     * @param count The size of the stack to create
     */
    public FluidStack toStack(int count) {
        FluidStack stack = get().getDefaultInstance(1);
        if (stack.isEmpty()) throw new IllegalStateException("Obtained empty item stack; incorrect getDefaultInstance() call?");
        stack.setAmount(count);
        return stack;
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link Fluid} with the specified name.
     *
     * @param <T> The type of the target {@link Fluid}.
     * @param key The name of the target {@link Fluid}.
     */
    public static <T extends Fluid> DeferredFluid<T> createFluid(ResourceLocation key) {
        return createFluid(ResourceKey.create(Registries.FLUID, key));
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link Fluid}.
     *
     * @param <T> The type of the target {@link Fluid}.
     * @param key The resource key of the target {@link Fluid}.
     */
    public static <T extends Fluid> DeferredFluid<T> createFluid(ResourceKey<Fluid> key) {
        return new DeferredFluid<>(key);
    }

    protected DeferredFluid(ResourceKey<Fluid> key) {
        super(key);
    }
}
