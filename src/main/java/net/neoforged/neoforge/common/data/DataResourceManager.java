/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.apache.commons.lang3.function.Consumers;
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
public final class DataResourceManager implements ResourceManager {
    private final ResourceManager delegate;
    private final PackType packType;

    private DataResourceManager(PackType packType, Consumer<Consumer<PackResources>> consumer) {
        this.packType = packType;

        var packs = Lists.<PackResources>newArrayList();
        consumer.accept(packs::add);
        packs.add(ServerPacksSource.createVanillaPackSource());

        ModList.get().forEachModInOrder(mod -> {
            var packInfo = new PackLocationInfo("mod/" + mod.getModId(), Component.empty(), PackSource.BUILT_IN, Optional.empty());
            var modPack = ResourcePackLoader.createPackForMod(mod.getModInfo().getOwningFile()).openPrimary(packInfo);
            packs.add(modPack);
        });

        delegate = new MultiPackResourceManager(packType, packs);
    }

    @ApiStatus.Internal
    public DataResourceManager(PackType packType) {
        this(packType, Consumers.nop());
    }

    public PackType packType() {
        return packType;
    }

    public boolean exists(ResourceLocation path) {
        return getResource(path).isPresent();
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation path) {
        return delegate.getResource(path);
    }

    @Override
    public Resource getResourceOrThrow(ResourceLocation path) throws FileNotFoundException {
        return delegate.getResourceOrThrow(path);
    }

    @Override
    public InputStream open(ResourceLocation path) throws IOException {
        return delegate.open(path);
    }

    @Override
    public BufferedReader openAsReader(ResourceLocation path) throws IOException {
        return delegate.openAsReader(path);
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation path) {
        return delegate.getResourceStack(path);
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String path, Predicate<ResourceLocation> filter) {
        return delegate.listResources(path, filter);
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String path, Predicate<ResourceLocation> filter) {
        return delegate.listResourceStacks(path, filter);
    }

    @Override
    public Set<String> getNamespaces() {
        return delegate.getNamespaces();
    }

    @Override
    public Stream<PackResources> listPacks() {
        return delegate.listPacks();
    }

    public static DataResourceManager forClient(@Nullable String assetIndex, @Nullable File assetsDir) {
        return new DataResourceManager(PackType.CLIENT_RESOURCES, consumer -> {
            if (FMLEnvironment.dist.isClient() && assetIndex != null && assetsDir != null)
                consumer.accept(ClientPackSource.createVanillaPackSource(IndexedAssetSource.createIndexFs(assetsDir.toPath(), assetIndex)));
        });
    }
}
