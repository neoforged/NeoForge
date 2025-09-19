package net.neoforged.neoforge.server.jsonrpc;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforgespi.language.IModInfo;

public interface NeoForgeService {
    MinecraftApiServiceKey<NeoForgeService> SERVICE_KEY = new MinecraftApiServiceKey<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "neoforge"));

    List<IModInfo> getModList();

    Stream<ResourceKey<? extends Registry<?>>> listRegistries();

    <T> Stream<ResourceKey<T>> listRegistryKeys(ResourceKey<? extends Registry<T>> registryKey);
}
