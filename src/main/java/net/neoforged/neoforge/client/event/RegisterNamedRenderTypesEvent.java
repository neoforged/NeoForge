/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.RenderTypeGroup;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom named {@link RenderType render types}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterNamedRenderTypesEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, RenderTypeGroup> renderTypes;

    @ApiStatus.Internal
    public RegisterNamedRenderTypesEvent(Map<ResourceLocation, RenderTypeGroup> renderTypes) {
        this.renderTypes = renderTypes;
    }

    /**
     * Registers a named {@link RenderTypeGroup}.
     *
     * @param key              The ID of the group
     * @param blockRenderType  One of the values returned by {@link RenderType#chunkBufferLayers()}
     * @param entityRenderType A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY}
     */
    public void register(ResourceLocation key, RenderType blockRenderType, RenderType entityRenderType) {
        Preconditions.checkArgument(!renderTypes.containsKey(key), "Render type already registered: " + key);
        Preconditions.checkArgument(blockRenderType.format() == DefaultVertexFormat.BLOCK, "The block render type must use the BLOCK vertex format.");
        Preconditions.checkArgument(blockRenderType.getChunkLayerId() >= 0, "Only chunk render types can be used for block rendering. Query RenderType#chunkBufferLayers() for a list.");
        Preconditions.checkArgument(entityRenderType.format() == DefaultVertexFormat.NEW_ENTITY, "The entity render type must use the NEW_ENTITY vertex format.");
        renderTypes.put(key, new RenderTypeGroup(blockRenderType, entityRenderType));
    }
}
