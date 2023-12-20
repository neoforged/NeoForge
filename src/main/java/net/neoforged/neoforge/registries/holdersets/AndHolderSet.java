/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.holdersets;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * <p>Holderset that represents an intersection of other holdersets. Json format:</p>
 * 
 * <pre>
 * {
 *   "type": "neoforge:and",
 *   "values":
 *   [
 *      // list of sub-holdersets (strings, lists, or objects)
 *   ]
 * }
 * </pre>
 */
public class AndHolderSet<T> extends CompositeHolderSet<T> {
    public static <T> Codec<? extends ICustomHolderSet<T>> codec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
        return HolderSetCodec.create(registryKey, holderCodec, forceList)
                .listOf()
                .xmap(AndHolderSet::new, CompositeHolderSet::homogenize)
                .fieldOf("values")
                .codec();
    }

    public AndHolderSet(List<HolderSet<T>> values) {
        super(values);
    }

    @Override
    public HolderSetType type() {
        return NeoForgeMod.AND_HOLDER_SET.value();
    }

    @Override
    protected Set<Holder<T>> createSet() {
        List<HolderSet<T>> components = this.getComponents();
        if (components.size() < 1) {
            return Set.of();
        }
        if (components.size() == 1) {
            return components.get(0).stream().collect(Collectors.toSet());
        }

        List<HolderSet<T>> remainingComponents = components.subList(1, components.size());
        return components.get(0)
                .stream()
                .filter(holder -> remainingComponents.stream().allMatch(holderset -> holderset.contains(holder)))
                .collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "AndSet[" + this.getComponents() + "]";
    }
}
