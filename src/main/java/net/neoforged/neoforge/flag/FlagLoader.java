/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

final class FlagLoader extends SimplePreparableReloadListener<Set<ResourceLocation>> {
    static final String FILE = "flags.json";
    static final Codec<List<ResourceLocation>> CODEC = ResourceLocation.CODEC.listOf();

    private final Gson gson = new GsonBuilder().create();

    FlagLoader() {}

    @Override
    protected Set<ResourceLocation> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        var flags = Sets.<ResourceLocation>newHashSet();

        for (var namespace : resourceManager.getNamespaces()) {
            profiler.push(namespace);

            var path = ResourceLocation.fromNamespaceAndPath(namespace, FILE);

            try {
                for (var resource : resourceManager.getResourceStack(path)) {
                    profiler.push(resource.sourcePackId());

                    try (var reader = resource.openAsReader()) {
                        profiler.push("parse");

                        var json = GsonHelper.fromJson(gson, reader, JsonElement.class);

                        CODEC.parse(JsonOps.INSTANCE, json)
                                .ifSuccess(flags::addAll);

                        profiler.pop();
                    } catch (RuntimeException e) {

                    }

                    profiler.pop();
                }
            } catch (IOException e) {

            }

            profiler.pop();
        }

        profiler.endTick();
        return flags;
    }

    @Override
    protected void apply(Set<ResourceLocation> flags, ResourceManager resourceManager, ProfilerFiller profiler) {
        var flagMap = new Object2BooleanOpenHashMap<ResourceLocation>(flags.size());
        flags.forEach(flag -> flagMap.put(flag, true));

        if (FlagManager.INSTANCE.setEnabled(flagMap, false))
            FlagManager.INSTANCE.markDirty(true, true);
    }
}
