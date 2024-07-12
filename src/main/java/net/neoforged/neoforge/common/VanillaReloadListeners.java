/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The IDs of the vanilla reload listeners.
 */
public class VanillaReloadListeners {
    private VanillaReloadListeners() {
    }

    /**
     * The IDs of the server reload listeners.
     */
    @ApiStatus.Internal
    public static final Map<Class<? extends PreparableReloadListener>, ResourceLocation> SERVER_IDS = new IdentityHashMap<>();

    /**
     * @see RecipeManager
     */
    public static final ResourceLocation RECIPES = server(RecipeManager.class, "recipes");
    /**
     * @see TagManager
     */
    public static final ResourceLocation TAGS = server(TagManager.class, "tags");
    /**
     * @see ServerAdvancementManager
     */
    public static final ResourceLocation ADVANCEMENTS = server(ServerAdvancementManager.class, "advancements");

    private static ResourceLocation server(Class<? extends PreparableReloadListener> type, String path) {
        var id = ResourceLocation.withDefaultNamespace(path);
        SERVER_IDS.put(type, id);
        return id;
    }
}
