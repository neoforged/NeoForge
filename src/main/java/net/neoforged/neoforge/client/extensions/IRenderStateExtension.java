/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.util.context.ContextKey;
import org.jetbrains.annotations.Nullable;

public interface IRenderStateExtension {
    @Nullable
    <T> T getRenderData(ContextKey<T> key);

    <T> void setRenderData(ContextKey<T> key, @Nullable T data);

    default <T> T getRenderDataOrThrow(ContextKey<T> key) {
        T data = getRenderData(key);
        if (data == null) {
            throw new IllegalStateException("No value associated for key " + key);
        }
        return data;
    }

    default <T> T getRenderDataOrDefault(ContextKey<T> key, T defaultVal) {
        T data = getRenderData(key);
        if (data == null) {
            return defaultVal;
        }
        return data;
    }
}
