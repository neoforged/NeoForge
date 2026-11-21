/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackResources;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public class ClientDatagenModLoader extends DatagenModLoader {
    public static void beginClient(
            Set<String> mods,
            Path path,
            Collection<Path> inputs,
            Collection<Path> existingPacks,
            boolean validate,
            boolean flat,
            boolean uncached,
            DataGenerator vanillaGenerator,
            @Nullable File assetsDir,
            @Nullable String assetIndex) {
        Runnable boostrapper = () -> {
            ClientBootstrap.bootstrap();
            ClientModLoader.beginClient(() -> {}, true);
            ClientHooks.initClientRegistries();
        };
        Consumer<Consumer<PackResources>> vanillaClientAssets = packConsumer -> {
            if (assetsDir != null && assetIndex != null) {
                packConsumer.accept(ClientPackSource.createVanillaPackSource(IndexedAssetSource.createIndexFs(assetsDir.toPath(), assetIndex)).fullResources());
            }
        };
        begin(boostrapper, mods, path, inputs, existingPacks, false, false, validate, flat, uncached, GatherDataEvent.Client::new, vanillaGenerator, vanillaClientAssets);
    }
}
