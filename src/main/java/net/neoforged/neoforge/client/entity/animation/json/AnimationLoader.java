/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.common.collect.MapMaker;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * A loader for entity animations written in JSON. You can also get parsed animations from this class.
 */
public final class AnimationLoader extends SimpleJsonResourceReloadListener<AnimationDefinition> {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final AnimationLoader INSTANCE = new AnimationLoader();

    private final Map<ResourceLocation, AnimationHolder> animations = new MapMaker().weakValues().concurrencyLevel(1).makeMap();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<AnimationHolder> strongHolderReferences = new ArrayList<>();

    private AnimationLoader() {
        super(AnimationParser.CODEC, FileToIdConverter.json("neoforge/animations/entity"));
    }

    /**
     * Gets a loaded {@link AnimationDefinition} with the specified {@code key}.
     */
    @Nullable
    public AnimationDefinition getAnimation(ResourceLocation key) {
        final var holder = animations.get(key);
        return holder != null ? holder.getOrNull() : null;
    }

    /**
     * Returns an {@link AnimationHolder} for an animation. If the specified animation has not been loaded, the holder
     * will be unbound, but may be bound in the future.
     */
    public AnimationHolder getAnimationHolder(ResourceLocation key) {
        return animations.computeIfAbsent(key, AnimationHolder::new);
    }

    @Override
    protected void apply(Map<ResourceLocation, AnimationDefinition> animationJsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        animations.values().forEach(AnimationHolder::unbind);
        strongHolderReferences.clear();
        int loaded = 0;
        for (final var entry : animationJsons.entrySet()) {
            final var holder = getAnimationHolder(entry.getKey());
            holder.bind(entry.getValue());
            strongHolderReferences.add(holder);
            loaded++;
        }
        LOGGER.info("Loaded {} entity animations", loaded);
    }
}
