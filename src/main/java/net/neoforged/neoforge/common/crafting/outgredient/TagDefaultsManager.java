/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForgeEventHandler;
import net.neoforged.neoforge.common.config.NeoForgeServerConfig;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is the central manager class for {@link TagDefaults}. Access an instance via {@link NeoForgeEventHandler#getTagDefaultsManager()}.
 */
public class TagDefaultsManager extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final String FOLDER = "tag_defaults";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<ResourceKey<? extends Registry<?>>, TagDefaults<?>> entries = new HashMap<>();

    public TagDefaultsManager() {
        super(ExtraCodecs.JSON, FileToIdConverter.json(FOLDER));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        entries.clear();
        resourceList.forEach(this::put);
    }

    /**
     * Resolves a {@link TagDefaults} into an optional outgredient.
     *
     * @param registryKey The key of the associated {@link Registry}.
     * @param tagKey      The {@link TagKey} to resolve.
     * @return An {@link Optional} of a {@link TagDefaults}'s resolved value, or an empty {@link Optional} if the {@link TagDefaults} could not be resolved.
     * @param <T> The type of the {@link Registry} and the outgredient.
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> resolve(ResourceKey<? extends Registry<T>> registryKey, TagKey<T> tagKey) {
        // Try to find an entry in the tag defaults themselves.
        TagDefaults<T> defaults = (TagDefaults<T>) entries.get(registryKey);
        if (defaults != null) {
            T result = defaults.resolve(tagKey);
            if (result != null) return Optional.of(result);
        }

        // Try to find an entry by walking the config mod id list.
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(registryKey.location());
        if (registry != null) {
            List<String> modIds = NeoForgeServerConfig.INSTANCE.defaultedTagModIds.get();
            // Walk the config mod id list.
            for (String modId : modIds) {
                // Filter the tag contents by mod id.
                List<Holder<T>> tagContents = registry
                        .getOrThrow(tagKey)
                        .stream()
                        .filter(holder -> holder.unwrapKey().map(key -> key.location().getNamespace().equals(modId)).orElse(false))
                        .toList();
                // If we have exactly one candidate, we found our result.
                if (tagContents.size() == 1) return Optional.of(tagContents.getFirst().value());
            }
        }

        // We haven't found anything.
        return Optional.empty();
    }

    /**
     * Called for every element in {@link TagDefaultsManager#apply(Map, ResourceManager, ProfilerFiller)}.
     * Moved into a separate method due to generics (unlike above, we can force the registry and the value to have the same generic type here).
     *
     * @param key   The registry's {@link ResourceLocation}.
     * @param value The {@link JsonElement} to parse.
     * @param <T> The type of the registry and the value.
     */
    @SuppressWarnings("unchecked")
    private <T> void put(ResourceLocation key, JsonElement value) {
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.getValue(key);
        if (registry == null) {
            LOGGER.warn("Skipping loading data file '{}' as no corresponding registry was found", key);
            return;
        }
        Codec.unboundedMap(TagKey.codec(registry.key()), registry.byNameCodec()).parse(JsonOps.INSTANCE, value)
                .ifError(error -> LOGGER.error("Couldn't parse data file '{}': {}", key, error))
                .ifSuccess(map -> entries.put(registry.key(), new TagDefaults<>(map)));
    }
}
