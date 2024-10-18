/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

public final class LazyNamedHolderSet<T> implements ICustomHolderSet<T> {
    private final List<Runnable> owners = new ArrayList<>();
    private final HolderLookup.RegistryLookup<T> registryLookup;
    private final TagKey<T> tagKey;
    @Nullable
    private HolderSet.Named<T> resolvedSet;

    public LazyNamedHolderSet(HolderLookup.RegistryLookup<T> registryLookup, TagKey<T> tagKey) {
        this.registryLookup = registryLookup;
        this.tagKey = tagKey;
    }

    public HolderLookup.RegistryLookup<T> registryLookup() {
        return registryLookup;
    }

    public TagKey<T> tagKey() {
        return tagKey;
    }

    @Override
    public HolderSetType type() {
        return NeoForgeMod.LAZY_NAMED_HOLDER_SET.value();
    }

    @Override
    public void addInvalidationListener(Runnable runnable) {
        this.owners.add(runnable);
    }

    @Override
    public Stream<Holder<T>> stream() {
        return resolve().stream();
    }

    @Override
    public int size() {
        return resolve().size();
    }

    @Override
    public boolean isBound() {
        return resolve().isBound();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return resolve().unwrap();
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        return resolve().getRandomElement(random);
    }

    @Override
    public Holder<T> get(int idx) {
        return resolve().get(idx);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return resolve().contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return resolve().canSerializeIn(owner);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return resolve().unwrapKey();
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return resolve().iterator();
    }

    @Override
    public String toString() {
        return "LazyNamedHolderSet(" + this.tagKey + ")";
    }

    private HolderSet.Named<T> resolve() {
        HolderSet.Named<T> resolved = this.resolvedSet;
        if (resolved == null) {
            this.resolvedSet = resolved = this.registryLookup.getOrThrow(this.tagKey);
            resolved.addInvalidationListener(this::invalidate);
        }
        return resolved;
    }

    private void invalidate() {
        this.resolvedSet = null;
        for (Runnable runnable : this.owners) {
            runnable.run();
        }
    }

    public static final class Type implements HolderSetType {
        @Override
        public <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
            return RecordCodecBuilder.<LazyNamedHolderSet<T>>mapCodec(
                    builder -> builder.group(
                            RegistryOps.retrieveRegistryLookup(registryKey).forGetter(LazyNamedHolderSet::registryLookup),
                            TagKey.hashedCodec(registryKey).fieldOf("tag").forGetter(LazyNamedHolderSet::tagKey))
                            .apply(builder, LazyNamedHolderSet::new));
        }

        @Override
        public <T> StreamCodec<RegistryFriendlyByteBuf, LazyNamedHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
            return new StreamCodec<>() {
                private final StreamCodec<ByteBuf, TagKey<T>> keyCodec = TagKey.streamCodec(registryKey);

                @Override
                public LazyNamedHolderSet<T> decode(RegistryFriendlyByteBuf buf) {
                    return new LazyNamedHolderSet<>(buf.registryAccess().lookupOrThrow(registryKey), keyCodec.decode(buf));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, LazyNamedHolderSet<T> set) {
                    keyCodec.encode(buf, set.tagKey);
                }
            };
        }
    }
}
