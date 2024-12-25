/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Simple {@link ResourceManager} implementation for looking up resources at runtime during data generation.
 * <p>
 * This resource manager can be used to lookup any asset/data file at runtime, assuming that the respective mod is loaded.
 * <p>
 * Do take note that under server data-gen runs, most of Minecraft's assets will be missing due to {@link ClientPackSource} being client only.
 *
 * @see ResourceManager
 */
public final class DataResourceManager {
    private final FallbackResourceManager client = new FallbackResourceManager(PackType.CLIENT_RESOURCES, "minecraft");
    private final FallbackResourceManager server = new FallbackResourceManager(PackType.SERVER_DATA, "minecraft");

    @ApiStatus.Internal
    public DataResourceManager(@Nullable String assetIndex, @Nullable File assetsDir) {
        if (FMLEnvironment.dist.isClient() && assetIndex != null && assetsDir != null)
            client.push(ClientPackSource.createVanillaPackSource(IndexedAssetSource.createIndexFs(assetsDir.toPath(), assetIndex)));

        server.push(ServerPacksSource.createVanillaPackSource());

        ModList.get().forEachModInOrder(mod -> {
            var packInfo = new PackLocationInfo("mod/" + mod.getModId(), Component.empty(), PackSource.BUILT_IN, Optional.empty());
            var modPack = ResourcePackLoader.createPackForMod(mod.getModInfo().getOwningFile()).openPrimary(packInfo);

            client.push(modPack);
            server.push(modPack);
        });
    }

    private ResourceManager manager(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> client;
            case SERVER_DATA -> server;
        };
    }

    public Optional<Resource> getResource(PackType packType, ResourceLocation path) {
        return manager(packType).getResource(path);
    }

    public boolean exists(PackType packType, ResourceLocation path) {
        return getResource(packType, path).isPresent();
    }

    public Resource getResourceOrThrow(PackType packType, ResourceLocation path) throws FileNotFoundException {
        return getResource(packType, path).orElseThrow(() -> new FileNotFoundException("Missing " + name(packType) + " resource " + path));
    }

    public InputStream open(PackType packType, ResourceLocation path) throws IOException {
        return getResourceOrThrow(packType, path).open();
    }

    public BufferedReader openAsReader(PackType packType, ResourceLocation path) throws IOException {
        return getResourceOrThrow(packType, path).openAsReader();
    }

    public List<Resource> getResourceStack(PackType packType, ResourceLocation path) {
        return manager(packType).getResourceStack(path);
    }

    public Map<ResourceLocation, Resource> listResources(PackType packType, String path, Predicate<ResourceLocation> filter) {
        return manager(packType).listResources(path, filter);
    }

    public Map<ResourceLocation, List<Resource>> listResourceStacks(PackType packType, String path, Predicate<ResourceLocation> filter) {
        return manager(packType).listResourceStacks(path, filter);
    }

    private static String name(PackType packType) {
        return switch (packType) {
            case CLIENT_RESOURCES -> "client";
            case SERVER_DATA -> "server";
        };
    }
}
