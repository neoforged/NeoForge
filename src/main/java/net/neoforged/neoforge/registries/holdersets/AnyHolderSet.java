/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * <p>Holderset that represents all elements of a registry. Json format:</p>
 * 
 * <pre>
 * {
 *   "type": "neoforge:any"
 * }
 * </pre>
 */
public record AnyHolderSet<T>(HolderLookup.RegistryLookup<T> registryLookup) implements ICustomHolderSet<T> {

    @Override
    public HolderSetType type() {
        return NeoForgeMod.ANY_HOLDER_SET.value();
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return this.stream().iterator();
    }

    @Override
    public Stream<Holder<T>> stream() {
        return this.registryLookup.listElements().map(Function.identity());
    }

    @Override
    public int size() {
        return (int) this.stream().count();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(this.stream().toList());
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return Util.getRandomSafe(this.stream().toList(), random);
    }

    @Override
    public Holder<T> get(int i) {
        List<Holder<T>> holders = this.stream().toList();
        Holder<T> holder = i >= holders.size() ? null : holders.get(i);
        if (holder == null)
            throw new NoSuchElementException("No element " + i + " in registry " + this.registryLookup.key());

        return holder;
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return holder.unwrapKey().map(key -> this.registryLookup.listElementIds().anyMatch(key::equals)).orElse(false);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> holderOwner) {
        return this.registryLookup.canSerializeIn(holderOwner);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "AnySet(" + this.registryLookup.key() + ")";
    }
    public static class Type implements HolderSetType {
        @Override
        public <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
            return RegistryOps.retrieveRegistryLookup(registryKey)
                    .xmap(AnyHolderSet::new, AnyHolderSet::registryLookup);
        }

        @Override
        public <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
            return new StreamCodec<RegistryFriendlyByteBuf, AnyHolderSet<T>>() {
                @Override
                public AnyHolderSet<T> decode(RegistryFriendlyByteBuf buf) {
                    return new AnyHolderSet<>(buf.registryAccess().lookupOrThrow(registryKey));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, AnyHolderSet<T> holderSet) {
                    final var registryKeyIn = holderSet.registryLookup.key();
                    if (!registryKey.equals(registryKeyIn)) {
                        throw new IllegalStateException("Can not encode " + holderSet
                                + ", expected registry: "
                                + registryKey.registry() + "/" + registryKey.location());
                    }
                }
            };
        }
    }
}
