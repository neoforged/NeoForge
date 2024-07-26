/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@SuppressWarnings({ "unchecked", "rawtypes" })
public record RegistryDataMapSyncPayload<T>(ResourceKey<? extends Registry<T>> registryKey,
        Map<ResourceLocation, Map<ResourceKey<T>, ?>> dataMaps) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RegistryDataMapSyncPayload<?>> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "registry_data_map_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RegistryDataMapSyncPayload<?>> STREAM_CODEC = StreamCodec.ofMember(
            RegistryDataMapSyncPayload::write, RegistryDataMapSyncPayload::decode);

    public static <T> RegistryDataMapSyncPayload<T> decode(RegistryFriendlyByteBuf buf) {
        //noinspection RedundantCast javac complains about this cast
        final ResourceKey<Registry<T>> registryKey = (ResourceKey<Registry<T>>) (Object) buf.readRegistryKey();
        final Map<ResourceLocation, Map<ResourceKey<T>, ?>> attach = buf.readRegistryMap(FriendlyByteBuf::readResourceLocation, (b1, key) -> {
            final DataMapType<T, ?> dataMap = RegistryManager.getDataMap(registryKey, key);
            return b1.readRegistryMap(bf -> bf.readResourceKey(registryKey), bf -> bf.readJsonWithRegistryCodec(dataMap.networkCodec()));
        });
        return new RegistryDataMapSyncPayload<>(registryKey, attach);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(registryKey);
        buf.writeRegistryMap(dataMaps, FriendlyByteBuf::writeResourceLocation, (b1, key, attach) -> {
            final DataMapType<T, ?> dataMap = RegistryManager.getDataMap(registryKey, key);
            b1.writeRegistryMap(attach, FriendlyByteBuf::writeResourceKey, (bf, value) -> bf.writeJsonWithRegistryCodec((Codec) dataMap.networkCodec(), value));
        });
    }

    @Override
    public Type<RegistryDataMapSyncPayload<?>> type() {
        return TYPE;
    }
}
