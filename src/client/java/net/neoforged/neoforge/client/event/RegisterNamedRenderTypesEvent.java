/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.RenderTypeGroup;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom named {@link RenderType render types}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterNamedRenderTypesEvent extends Event implements IModBusEvent {
    private final Map<Identifier, RenderTypeGroup> renderTypes;

    @ApiStatus.Internal
    public RegisterNamedRenderTypesEvent(Map<Identifier, RenderTypeGroup> renderTypes) {
        this.renderTypes = renderTypes;
    }

    /**
     * Registers a named {@link RenderTypeGroup}.
     *
     * @param key              The ID of the group
     * @param chunkLayer       The {@link ChunkSectionLayer} to render blocks with as part of the terrain
     * @param entityRenderType A factory for a {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY} with the atlas passed to the function
     */
    public void register(Identifier key, ChunkSectionLayer chunkLayer, Function<Identifier, RenderType> entityRenderType) {
        Preconditions.checkArgument(!renderTypes.containsKey(key), "Render type already registered: " + key);
        renderTypes.put(key, new RenderTypeGroup(chunkLayer, entityRenderType));
    }
}
