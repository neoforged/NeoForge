/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class AnimationHolder {
    private final ResourceLocation key;
    @Nullable
    private AnimationDefinition value;

    AnimationHolder(ResourceLocation key) {
        this.key = key;
    }

    void unbind() {
        this.value = null;
    }

    void bind(AnimationDefinition value) {
        this.value = value;
    }

    public ResourceLocation key() {
        return key;
    }

    public AnimationDefinition get() {
        final var result = value;
        if (result == null) {
            throw new IllegalStateException("Unknown entity animation " + key);
        }
        return result;
    }

    @Nullable
    public AnimationDefinition getOrNull() {
        return value;
    }
}
