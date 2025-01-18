/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * A loader for custom {@linkplain UnbakedModel unbaked models}.
 * <p>
 * If you do any caching, you should implement {@link ResourceManagerReloadListener} and register it with
 * {@link RegisterClientReloadListenersEvent}.
 *
 * @see ModelEvent.RegisterLoaders
 * @see RegisterClientReloadListenersEvent
 */
public interface UnbakedModelLoader<T extends UnbakedModel> {
    /**
     * Reads an unbaked model from the passed JSON object.
     *
     * <p>The {@link JsonDeserializationContext} argument can be used to deserialize types that the system already understands.
     * For example, {@code deserializationContext.deserialize(<sub object>, Transformation.class)} to parse a transformation,
     * or {@code deserializationContext.deserialize(<sub object>, UnbakedModel.class)} to parse a nested model.
     * The set of supported types can be found in the declaration of {@link BlockModel#GSON}.
     */
    T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
