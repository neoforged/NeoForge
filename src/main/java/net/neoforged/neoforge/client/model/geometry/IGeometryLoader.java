/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.geometry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

/**
 * A loader for custom {@linkplain IUnbakedGeometry model geometries}.
 * <p>
 * If you do any caching, you should implement {@link ResourceManagerReloadListener} and register it with
 * {@link RegisterClientReloadListenersEvent}.
 *
 * @see ModelEvent.RegisterGeometryLoaders
 * @see RegisterClientReloadListenersEvent
 */
public interface IGeometryLoader<T extends IUnbakedGeometry<T>> {
    T read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException;
}
