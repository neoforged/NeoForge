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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

final class FlagLoader extends SimplePreparableReloadListener<Set<FlagLoader.FlagData>> {
    static final String FILE = "flags.json";

    private final Gson gson = new GsonBuilder().create();

    FlagLoader() {}

    @Override
    protected Set<FlagData> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        var flags = Sets.<FlagData>newHashSet();

        for (var namespace : resourceManager.getNamespaces()) {
            profiler.push(namespace);

            var path = ResourceLocation.fromNamespaceAndPath(namespace, FILE);

            try {
                for (var resource : resourceManager.getResourceStack(path)) {
                    profiler.push(resource.sourcePackId());

                    try (var reader = resource.openAsReader()) {
                        profiler.push("parse");

                        var json = GsonHelper.fromJson(gson, reader, JsonElement.class);

                        FlagData.CODEC.parse(JsonOps.INSTANCE, json)
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
    protected void apply(Set<FlagData> flags, ResourceManager resourceManager, ProfilerFiller profiler) {
        FlagManager.INSTANCE.loadFromJson(flags);
    }

    public record FlagData(ResourceLocation name, boolean enabledByDefault) {
        static Codec<List<FlagData>> CODEC = Codec.withAlternative(
                RecordCodecBuilder.create(builder -> builder.group(
                        ResourceLocation.CODEC.fieldOf("name").forGetter(FlagData::name),
                        Codec.BOOL.optionalFieldOf("enabled", false).forGetter(FlagData::enabledByDefault)).apply(builder, FlagData::new)),
                ResourceLocation.CODEC.xmap(
                        name -> new FlagData(name, false),
                        FlagData::name))
                .listOf();
    }
}
