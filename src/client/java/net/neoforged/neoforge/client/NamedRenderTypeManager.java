/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for named {@link RenderType render types}.
 * <p>
 * Provides a lookup.
 */
public final class NamedRenderTypeManager {
    private static ImmutableMap<ResourceLocation, RenderTypeGroup> RENDER_TYPES;

    /**
     * Finds the {@link RenderTypeGroup} for a given name, or the {@link RenderTypeGroup#EMPTY empty group} if not found.
     */
    public static RenderTypeGroup get(ResourceLocation name) {
        return RENDER_TYPES.getOrDefault(name, RenderTypeGroup.EMPTY);
    }

    @ApiStatus.Internal
    public static void init() {
        var renderTypes = new HashMap<ResourceLocation, RenderTypeGroup>();
        preRegisterVanillaRenderTypes(renderTypes);
        var event = new RegisterNamedRenderTypesEvent(renderTypes);
        ModLoader.postEventWrapContainerInModOrder(event);
        RENDER_TYPES = ImmutableMap.copyOf(renderTypes);
    }

    /**
     * Pre-registers vanilla render types.
     */
    private static void preRegisterVanillaRenderTypes(Map<ResourceLocation, RenderTypeGroup> blockRenderTypes) {
        blockRenderTypes.put(ResourceLocation.withDefaultNamespace("solid"), new RenderTypeGroup(ChunkSectionLayer.SOLID, NeoForgeRenderTypes.ITEM_LAYERED_SOLID.get()));
        blockRenderTypes.put(ResourceLocation.withDefaultNamespace("cutout"), new RenderTypeGroup(ChunkSectionLayer.CUTOUT, NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT.get()));
        // Generally entity/item rendering shouldn't use mipmaps, so cutout_mipped has them off by default. To enforce them, use cutout_mipped_all.
        blockRenderTypes.put(ResourceLocation.withDefaultNamespace("cutout_mipped"), new RenderTypeGroup(ChunkSectionLayer.CUTOUT_MIPPED, NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT.get()));
        blockRenderTypes.put(ResourceLocation.withDefaultNamespace("cutout_mipped_all"), new RenderTypeGroup(ChunkSectionLayer.CUTOUT_MIPPED, NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT_MIPPED.get()));
        blockRenderTypes.put(ResourceLocation.withDefaultNamespace("translucent"), new RenderTypeGroup(ChunkSectionLayer.TRANSLUCENT, NeoForgeRenderTypes.ITEM_LAYERED_TRANSLUCENT.get()));
        blockRenderTypes.put(ResourceLocation.withDefaultNamespace("tripwire"), new RenderTypeGroup(ChunkSectionLayer.TRIPWIRE, NeoForgeRenderTypes.ITEM_LAYERED_TRANSLUCENT.get()));
    }

    private NamedRenderTypeManager() {}
}
