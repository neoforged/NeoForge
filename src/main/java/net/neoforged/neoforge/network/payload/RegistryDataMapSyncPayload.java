package net.neoforged.neoforge.network.payload;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.attachment.DataMapType;

import java.util.Map;

public record RegistryDataMapSyncPayload<T>(ResourceKey<? extends Registry<T>> registryKey,
                                            Map<ResourceLocation, Map<ResourceKey<T>, ?>> dataMaps) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:registry_data_map_sync");

    public static <T> RegistryDataMapSyncPayload<T> decode(FriendlyByteBuf buf) {
        final ResourceKey<Registry<T>> registryKey = (ResourceKey<Registry<T>>) (Object) buf.readRegistryKey();
        final Map attach = buf.readMap(FriendlyByteBuf::readResourceLocation, (b1, key) -> {
            final DataMapType dataMap = RegistryManager.getDataMap(registryKey, key);
            return b1.readMap(bf -> bf.readResourceKey(registryKey), bf -> bf.readJsonWithCodec(dataMap.networkCodec()));
        });
        return new RegistryDataMapSyncPayload<T>(registryKey, attach);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceKey(registryKey);
        buf.writeMap(dataMaps, FriendlyByteBuf::writeResourceLocation, (b1, key, attach) -> {
            final DataMapType dataMap = RegistryManager.getDataMap(registryKey, key);
            b1.writeMap(attach, FriendlyByteBuf::writeResourceKey, (bf, value) -> bf.writeJsonWithCodec(dataMap.networkCodec(), value));
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
