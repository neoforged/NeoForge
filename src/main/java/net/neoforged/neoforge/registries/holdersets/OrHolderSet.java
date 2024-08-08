/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.holdersets;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * <p>Holderset that represents a union of other holdersets. Json format:</p>
 * 
 * <pre>
 * {
 *   "type": "neoforge:or",
 *   "values":
 *   [
 *      // list of sub-holdersets (strings, lists, or objects)
 *   ]
 * }
 * </pre>
 */
public class OrHolderSet<T> extends CompositeHolderSet<T> {
    public OrHolderSet(List<HolderSet<T>> values) {
        super(values);
    }

    @SafeVarargs
    public OrHolderSet(HolderSet<T>... values) {
        this(List.of(values));
    }

    @Override
    public HolderSetType type() {
        return NeoForgeMod.OR_HOLDER_SET.value();
    }

    @Override
    protected Set<Holder<T>> createSet() {
        return this.getComponents().stream().flatMap(HolderSet::stream).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return "OrSet[" + this.getComponents() + "]";
    }

    public static class Type implements HolderSetType {
        @Override
        public <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
            return HolderSetCodec.create(registryKey, holderCodec, forceList)
                    .listOf()
                    .xmap(OrHolderSet::new, CompositeHolderSet::homogenize)
                    .fieldOf("values");
        }

        @Override
        public <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
            return ByteBufCodecs.<RegistryFriendlyByteBuf, HolderSet<T>>list()
                    .apply(ByteBufCodecs.holderSet(registryKey))
                    .map(OrHolderSet::new, CompositeHolderSet::getComponents);
        }
    }
}
