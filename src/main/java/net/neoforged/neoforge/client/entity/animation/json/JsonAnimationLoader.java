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
 * A loader for entity animations written in JSON. You can also get parsed animations from this class, but the
 * recommended way to use JSON entity animations is with {@link JsonAnimator}.
 *
 * @see JsonAnimator
 */
public final class JsonAnimationLoader extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final JsonAnimationLoader INSTANCE = new JsonAnimationLoader();

    private final Map<ResourceLocation, AnimationDefinition> animations = new HashMap<>();

    private JsonAnimationLoader() {
        super(new Gson(), "animations/entity");
    }

    /**
     * Get the animation with the specified ID.
     * 
     * @param animationId The ID of the animation.
     * @return The animation, or null if not found.
     */
    @Nullable
    public AnimationDefinition getAnimation(ResourceLocation animationId) {
        return animations.get(animationId);
    }

    /**
     * Get the animation with the specified ID.
     * 
     * @param animationId The ID of the animation.
     * @return The animation
     * @throws IllegalStateException If the specified animation is not found
     */
    public AnimationDefinition getAnimationOrThrow(ResourceLocation animationId) throws IllegalStateException {
        final AnimationDefinition animation = getAnimation(animationId);
        if (animation == null) {
            throw new IllegalStateException("Missing animation " + animationId);
        }
        return animation;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> animationJsons, ResourceManager resourceManager, ProfilerFiller profiler) {
        animations.clear();
        for (final var entry : animationJsons.entrySet()) {
            try {
                animations.put(entry.getKey(), JsonAnimationParser.parseDefinition(
                        GsonHelper.convertToJsonObject(entry.getValue(), "animation")));
            } catch (Exception e) {
                LOGGER.error("Failed to load animation {}", entry.getKey(), e);
            }
        }
        LOGGER.info("Loaded {} entity animations", animations.size());
    }
}
