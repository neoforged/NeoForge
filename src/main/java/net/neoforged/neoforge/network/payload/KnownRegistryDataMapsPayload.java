/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record KnownRegistryDataMapsPayload(Map<ResourceKey<? extends Registry<?>>, List<KnownDataMap>> dataMaps) implements CustomPacketPayload {
    public static final Type<KnownRegistryDataMapsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "known_registry_data_maps"));
    public static final StreamCodec<FriendlyByteBuf, KnownRegistryDataMapsPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    Maps::newHashMapWithExpectedSize,
                    NeoForgeStreamCodecs.registryKey(),
                    KnownDataMap.STREAM_CODEC.apply(ByteBufCodecs.list())),
            KnownRegistryDataMapsPayload::dataMaps,
            KnownRegistryDataMapsPayload::new);

    @Override
    public Type<KnownRegistryDataMapsPayload> type() {
        return TYPE;
    }

    public record KnownDataMap(ResourceLocation id, boolean mandatory) {
        public static final StreamCodec<FriendlyByteBuf, KnownDataMap> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                KnownDataMap::id,
                ByteBufCodecs.BOOL,
                KnownDataMap::mandatory,
                KnownDataMap::new);
    }
}
