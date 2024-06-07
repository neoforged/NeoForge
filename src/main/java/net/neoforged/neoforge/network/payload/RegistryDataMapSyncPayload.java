/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.datamaps.DataMap;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({ "unchecked"})
public record RegistryDataMapSyncPayload<T>(ResourceKey<? extends Registry<T>> registryKey,
        Map<ResourceLocation, DataMap<T, ?>> dataMaps) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RegistryDataMapSyncPayload<?>> TYPE = new Type<>(new ResourceLocation("neoforge:registry_data_map_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryDataMapSyncPayload<?>> STREAM_CODEC = StreamCodec.ofMember(
            RegistryDataMapSyncPayload::write, RegistryDataMapSyncPayload::decode);

    public static <R> RegistryDataMapSyncPayload<R> decode(RegistryFriendlyByteBuf buf) {
        //noinspection RedundantCast javac complains about this cast
        final ResourceKey<Registry<R>> registryKey = (ResourceKey<Registry<R>>) (Object) buf.readRegistryKey();
        final Map<ResourceLocation, DataMap<R, ?>> attach = buf.readMap(
                FriendlyByteBuf::readResourceLocation,
                (b, k) -> DataMap.decode(registryKey, k, (RegistryFriendlyByteBuf) b)
        );
        return new RegistryDataMapSyncPayload<>(registryKey, attach);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(registryKey);
        buf.writeMap(dataMaps, FriendlyByteBuf::writeResourceLocation, (b, dataMap) -> dataMap.write(b));
    }

    @Override
    public Type<RegistryDataMapSyncPayload<?>> type() {
        return TYPE;
    }
}
