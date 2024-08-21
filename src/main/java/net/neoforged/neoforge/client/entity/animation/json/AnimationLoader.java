/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A loader for entity animations written in JSON. You can also get parsed animations from this class.
 */
public final class AnimationLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Map<ResourceLocation, AnimationHolder> animations = new HashMap<>();

    private AnimationLoader() {
        super(new Gson(), "animations/entity");
    }

    @Nullable
    public AnimationDefinition getAnimation(ResourceLocation key) {
        final var holder = animations.get(key);
        return holder != null ? holder.getOrNull() : null;
    }

    public AnimationHolder getAnimationHolder(ResourceLocation key) {
        return animations.computeIfAbsent(key, AnimationHolder::new);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> animationJsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        AnimationTypeManager.init();
        animations.values().forEach(AnimationHolder::unbind);
        int loaded = 0;
        for (final var entry : animationJsons.entrySet()) {
            try {
                final var animation = AnimationParser.parseDefinition(
                        GsonHelper.convertToJsonObject(entry.getValue(), "animation"));
                getAnimationHolder(entry.getKey()).bind(animation);
                loaded++;
            } catch (Exception e) {
                LOGGER.error("Failed to load animation {}", entry.getKey(), e);
            }
        }
        LOGGER.info("Loaded {} entity animations", loaded);
    }
}
