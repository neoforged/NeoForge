/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

@SuppressWarnings({ "unchecked", "rawtypes" })
public record RegistryDataMapSyncPayload<T>(ResourceKey<? extends Registry<T>> registryKey,
        Map<ResourceLocation, Map<ResourceKey<T>, ?>> dataMaps) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:registry_data_map_sync");

    public static <T> RegistryDataMapSyncPayload<T> decode(FriendlyByteBuf buf) {
        //noinspection RedundantCast javac complains about this cast
        final ResourceKey<Registry<T>> registryKey = (ResourceKey<Registry<T>>) (Object) buf.readRegistryKey();
        final Map<ResourceLocation, Map<ResourceKey<T>, ?>> attach = buf.readMap(FriendlyByteBuf::readResourceLocation, (b1, key) -> {
            final DataMapType<T, ?> dataMap = RegistryManager.getDataMap(registryKey, key);
            return b1.readMap(bf -> bf.readResourceKey(registryKey), bf -> bf.readJsonWithCodec(dataMap.networkCodec()));
        });
        return new RegistryDataMapSyncPayload<>(registryKey, attach);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceKey(registryKey);
        buf.writeMap(dataMaps, FriendlyByteBuf::writeResourceLocation, (b1, key, attach) -> {
            final DataMapType<T, ?> dataMap = RegistryManager.getDataMap(registryKey, key);
            b1.writeMap(attach, FriendlyByteBuf::writeResourceKey, (bf, value) -> bf.writeJsonWithCodec((Codec) dataMap.networkCodec(), value));
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
