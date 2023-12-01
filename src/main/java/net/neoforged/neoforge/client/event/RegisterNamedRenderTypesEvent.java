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
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.RenderTypeGroup;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom named {@link RenderType render types}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
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
     * @param name             The name
     * @param blockRenderType  One of the values returned by {@link RenderType#chunkBufferLayers()}
     * @param entityRenderType A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY}
     * @deprecated Use {@link #register(ResourceLocation, RenderType, RenderType) the RL-explicit variant} instead; mod ID inference will be removed in a later update, alongside the move of registration events to the NeoForge main bus
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void register(String name, RenderType blockRenderType, RenderType entityRenderType) {
        register(new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), name), blockRenderType, entityRenderType, entityRenderType);
    }

    /**
     * Registers a named {@link RenderTypeGroup}.
     *
     * @param key              The ID of the group
     * @param blockRenderType  One of the values returned by {@link RenderType#chunkBufferLayers()}
     * @param entityRenderType A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY}
     */
    public void register(ResourceLocation key, RenderType blockRenderType, RenderType entityRenderType) {
        register(key, blockRenderType, entityRenderType, entityRenderType);
    }

    /**
     * Registers a named {@link RenderTypeGroup}.
     *
     * @param name                     The name
     * @param blockRenderType          One of the values returned by {@link RenderType#chunkBufferLayers()}
     * @param entityRenderType         A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY}
     * @param fabulousEntityRenderType A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY} for use when
     *                                 "fabulous" rendering is enabled
     * @deprecated Use {@link #register(ResourceLocation, RenderType, RenderType, RenderType) the RL-explicit variant} instead; mod ID inference will be removed in a later update, alongside the move of registration events to the NeoForge main bus
     */
    @Deprecated(forRemoval = true, since = "1.20.2")
    public void register(String name, RenderType blockRenderType, RenderType entityRenderType, RenderType fabulousEntityRenderType) {
        register(new ResourceLocation(ModLoadingContext.get().getActiveNamespace(), name), blockRenderType, entityRenderType, fabulousEntityRenderType);
    }

    /**
     * Registers a named {@link RenderTypeGroup}.
     *
     * @param key                      The ID of the group
     * @param blockRenderType          One of the values returned by {@link RenderType#chunkBufferLayers()}
     * @param entityRenderType         A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY}
     * @param fabulousEntityRenderType A {@link RenderType} using {@link DefaultVertexFormat#NEW_ENTITY} for use when
     *                                 "fabulous" rendering is enabled
     */
    public void register(ResourceLocation key, RenderType blockRenderType, RenderType entityRenderType, RenderType fabulousEntityRenderType) {
        Preconditions.checkArgument(!renderTypes.containsKey(key), "Render type already registered: " + key);
        Preconditions.checkArgument(blockRenderType.format() == DefaultVertexFormat.BLOCK, "The block render type must use the BLOCK vertex format.");
        Preconditions.checkArgument(blockRenderType.getChunkLayerId() >= 0, "Only chunk render types can be used for block rendering. Query RenderType#chunkBufferLayers() for a list.");
        Preconditions.checkArgument(entityRenderType.format() == DefaultVertexFormat.NEW_ENTITY, "The entity render type must use the NEW_ENTITY vertex format.");
        Preconditions.checkArgument(fabulousEntityRenderType.format() == DefaultVertexFormat.NEW_ENTITY, "The fabulous entity render type must use the NEW_ENTITY vertex format.");
        renderTypes.put(key, new RenderTypeGroup(blockRenderType, entityRenderType, fabulousEntityRenderType));
    }
}
