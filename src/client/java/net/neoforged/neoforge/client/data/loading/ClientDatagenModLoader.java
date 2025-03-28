/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.loading;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackResources;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class ClientDatagenModLoader extends DatagenModLoader {
    public static void begin(
            final Set<String> mods,
            final Path path,
            final Collection<Path> inputs,
            Collection<Path> existingPacks,
            final boolean devToolGenerators,
            final boolean reportsGenerator,
            final boolean structureValidator,
            final boolean flat,
            @Nullable final String assetIndex,
            @Nullable final File assetsDir,
            Runnable setup,
            GatherDataEvent.GatherDataEventGenerator eventGenerator,
            DataGenerator vanillaGenerator) {
        Consumer<Consumer<PackResources>> vanillaClientAssets = consumer -> {
            if (assetsDir != null && assetIndex != null)
                consumer.accept(ClientPackSource.createVanillaPackSource(IndexedAssetSource.createIndexFs(assetsDir.toPath(), assetIndex)));
        };
        begin(mods, path, inputs, existingPacks, devToolGenerators, reportsGenerator, structureValidator, flat, setup, eventGenerator, vanillaGenerator, vanillaClientAssets);
    }
}
