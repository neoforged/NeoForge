/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for creating {@linkplain ObjectLinkedOpenCustomHashSet linked set} for {@link FluidStack}
 * with specific {@linkplain Hash.Strategy hash strategy} as {@link FluidStack} does not override {@link #hashCode()} and {@link #equals(Object)}.
 * 
 * @see net.minecraft.world.item.ItemStackLinkedSet
 */
public class FluidStackLinkedSet {
    public static final Hash.Strategy<? super FluidStack> TYPE_AND_COMPONENTS = new Hash.Strategy<>() {
        public int hashCode(@Nullable FluidStack stack) {
            return FluidStack.hashFluidAndComponents(stack);
        }

        public boolean equals(@Nullable FluidStack first, @Nullable FluidStack second) {
            return first == second
                    || first != null
                            && second != null
                            && first.isEmpty() == second.isEmpty()
                            && FluidStack.isSameFluidSameComponents(first, second);
        }
    };

    public static Set<FluidStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_COMPONENTS);
    }
}
