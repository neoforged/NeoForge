/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import org.jetbrains.annotations.Nullable;

/**
 * Extension class for render state objects. Implemented by {@link BaseRenderState} for
 * simple class extension.
 */
public interface IRenderStateExtension {
    /**
     * Gets the object associated with the given key.
     * 
     * @param key Static key reference object
     * @return The object associated with the key or null if the key is not present.
     * @param <T> Type of render data
     */
    @Nullable
    <T> T getRenderData(ContextKey<T> key);

    /**
     * Sets the object associated with the given key. Key should be stored statically for later retrieval of the object.
     * 
     * @param key  Static key reference object
     * @param data Object to store for custom rendering
     * @param <T>  Type of render data
     */
    <T> void setRenderData(ContextKey<T> key, @Nullable T data);

    /**
     * Gets the value or throws an exception. Should be used in cases where the data must be present.
     * 
     * @param key Static key reference object
     * @return The data associate with the key
     * @param <T> Type of render data
     */
    default <T> T getRenderDataOrThrow(ContextKey<T> key) {
        T data = getRenderData(key);
        if (data == null) {
            throw new IllegalStateException("No value associated for key " + key);
        }
        return data;
    }

    /**
     * Gets the value or returns the default object if an object is not present
     * 
     * @param key        Static key reference object
     * @param defaultVal Default value if an object is not present
     * @return Value from the render data or the given default value if value is not present
     * @param <T> Type of render data
     */
    default <T> T getRenderDataOrDefault(ContextKey<T> key, T defaultVal) {
        T data = getRenderData(key);
        if (data == null) {
            return defaultVal;
        }
        return data;
    }
}
