/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for named {@link RenderType render types}.
 * <p>
 * Provides a lookup.
 */
public final class NamedRenderTypeManager {
    private static ImmutableMap<Identifier, RenderTypeGroup> RENDER_TYPES;

    /**
     * Finds the {@link RenderTypeGroup} for a given name, or the {@link RenderTypeGroup#EMPTY empty group} if not found.
     */
    public static RenderTypeGroup get(Identifier name) {
        return RENDER_TYPES.getOrDefault(name, RenderTypeGroup.EMPTY);
    }

    @ApiStatus.Internal
    public static void init() {
        var renderTypes = new HashMap<Identifier, RenderTypeGroup>();
        preRegisterVanillaRenderTypes(renderTypes);
        var event = new RegisterNamedRenderTypesEvent(renderTypes);
        ModLoader.postEventWrapContainerInModOrder(event);
        RENDER_TYPES = ImmutableMap.copyOf(renderTypes);
    }

    /**
     * Pre-registers vanilla render types.
     */
    private static void preRegisterVanillaRenderTypes(Map<Identifier, RenderTypeGroup> blockRenderTypes) {
        blockRenderTypes.put(Identifier.withDefaultNamespace("solid"), new RenderTypeGroup(ChunkSectionLayer.SOLID, NeoForgeRenderTypes::getItemLayeredSolid));
        blockRenderTypes.put(Identifier.withDefaultNamespace("cutout"), new RenderTypeGroup(ChunkSectionLayer.CUTOUT, NeoForgeRenderTypes::getItemLayeredCutout));
        blockRenderTypes.put(Identifier.withDefaultNamespace("translucent"), new RenderTypeGroup(ChunkSectionLayer.TRANSLUCENT, NeoForgeRenderTypes::getItemLayeredTranslucent));
        blockRenderTypes.put(Identifier.withDefaultNamespace("tripwire"), new RenderTypeGroup(ChunkSectionLayer.TRIPWIRE, NeoForgeRenderTypes::getItemLayeredTranslucent));
    }

    private NamedRenderTypeManager() {}
}
