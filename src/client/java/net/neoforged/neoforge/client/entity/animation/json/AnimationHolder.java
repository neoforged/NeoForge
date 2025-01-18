/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.mojang.logging.LogUtils;
import java.util.Map;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Holds a single {@link AnimationDefinition} loaded from resource packs. Objects of this class will be automatically updated with new
 * {@link AnimationDefinition}s on reload.
 */
public final class AnimationHolder {
    public static final AnimationDefinition EMPTY_ANIMATION = new AnimationDefinition(0f, false, Map.of());

    private static final Logger LOGGER = LogUtils.getLogger();

    private final ResourceLocation key;
    @Nullable
    private AnimationDefinition value;
    private boolean absentWarned;

    AnimationHolder(ResourceLocation key) {
        this.key = key;
    }

    void unbind() {
        value = null;
        absentWarned = false;
    }

    void bind(AnimationDefinition value) {
        this.value = value;
    }

    /**
     * Gets the key associated with this animation.
     */
    public ResourceLocation key() {
        return key;
    }

    /**
     * Gets the currently loaded animation. If the animation has not been loaded, returns {@link #EMPTY_ANIMATION}.
     */
    public AnimationDefinition get() {
        final var result = value;
        if (result == null) {
            if (!absentWarned) {
                absentWarned = true;
                LOGGER.warn("Missing entity animation {}", key);
            }
            return EMPTY_ANIMATION;
        }
        return result;
    }

    /**
     * Gets the currently loaded animation or null if it has not been loaded.
     */
    @Nullable
    public AnimationDefinition getOrNull() {
        return value;
    }

    /**
     * Returns whether the animation has been loaded.
     */
    public boolean isBound() {
        return value != null;
    }
}
