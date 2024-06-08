/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

public final class DataMap<R, T> {
    private final DataMapType<R, T> type;
    private final Registry<R> registry;
    private final @Nullable T defaultValue;
    private final Map<ResourceKey<R>, T> specifiedValues;
    private final Map<ResourceKey<R>, T> values;

    public DataMap(RegistryAccess registryAccess, DataMapType<R, T> type, @Nullable T defaultValue, Map<ResourceKey<R>, T> specifiedValues) {
        this.type = type;
        this.registry = registryAccess.registryOrThrow(type.registryKey());
        this.defaultValue = defaultValue;
        this.specifiedValues = specifiedValues;
        this.values = Collections.unmodifiableMap(defaultValue == null
                ? specifiedValues
                : Maps.asMap(registry.registryKeySet(), key -> specifiedValues.getOrDefault(key, defaultValue)));
    }

    public static <R> ResourceKey<R> defaultKey(ResourceKey<? extends Registry<R>> registryKey) {
        return ResourceKey.create(registryKey, new ResourceLocation("neoforge", "default"));
    }

    public static <R, T> DataMap<R, T> decode(ResourceKey<Registry<R>> registryKey, ResourceLocation typeId, RegistryFriendlyByteBuf buf) {
        final DataMapType<R, T> type = RegistryManager.getDataMap(registryKey, typeId);
        final StreamDecoder<FriendlyByteBuf, T> decoder = b -> b.readJsonWithCodec(type.networkCodec());
        final T defaultValue = buf.readOptional(decoder).orElse(null);
        final Map<ResourceKey<R>, T> values = buf.readMap(b -> b.readResourceKey(registryKey), decoder);
        return new DataMap<>(buf.registryAccess(), type, defaultValue, values);
    }

    public void write(FriendlyByteBuf buf) {
        final StreamEncoder<FriendlyByteBuf, T> encoder = (b, t) -> b.writeJsonWithCodec(type.networkCodec(), t);
        buf.writeOptional(Optional.ofNullable(defaultValue), encoder);
        buf.writeMap(specifiedValues, ResourceKey.streamCodec(registry.key()), encoder);
    }

    @UnmodifiableView
    public Map<ResourceKey<R>, T> values() {
        return values;
    }
}
