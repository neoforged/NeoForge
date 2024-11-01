/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.state;

import org.jetbrains.annotations.Nullable;

public interface IRenderStateExtension {
    @Nullable
    <T> T getExtension(RenderStateKey<T> key);

    default <T> T getExtensionOrThrow(RenderStateKey<T> key) {
        T data = getExtension(key);
        if (data == null) {
            throw new IllegalStateException("No value associated for key " + key);
        }
        return data;
    }

    <T> void setExtension(RenderStateKey<T> key, T obj);
}
