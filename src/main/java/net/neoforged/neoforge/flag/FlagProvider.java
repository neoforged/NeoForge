/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public abstract class FlagProvider implements DataProvider {
    private final Map<ResourceLocation, Boolean> flags = Maps.newHashMap();
    private final PackOutput pack;
    private final String modId;
    private final CompletableFuture<HolderLookup.Provider> holderProvider;

    protected FlagProvider(PackOutput pack, String modId, CompletableFuture<HolderLookup.Provider> holderProvider) {
        this.pack = pack;
        this.modId = modId;
        this.holderProvider = holderProvider;
    }

    public FlagProvider flag(ResourceLocation flag, boolean enabledByDefault) {
        flags.put(flag, enabledByDefault);
        return this;
    }

    public FlagProvider flag(ResourceLocation flag) {
        return flag(flag, false);
    }

    public FlagProvider flag(ResourceLocation flag, ResourceLocation... flags) {
        for (var flg : flags) {
            flag(flg);
        }

        return flag(flag);
    }

    protected abstract void generate();

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        generate();

        return holderProvider.thenCompose(provider -> DataProvider.saveStable(
                cache,
                provider,
                FlagLoader.FlagData.CODEC,
                flags.entrySet()
                        .stream()
                        .map(entry -> new FlagLoader.FlagData(entry.getKey(), entry.getValue()))
                        .toList(),
                pack.getOutputFolder(PackOutput.Target.DATA_PACK)
                        .resolve(modId)
                        .resolve(FlagLoader.FILE)));
    }

    @Override
    public String getName() {
        return "flags";
    }
}
