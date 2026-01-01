/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.util.VanillaClassToKey;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Keys for vanilla {@link PreparableReloadListener reload listeners}, used to specify dependency ordering in the {@link AddServerReloadListenersEvent}.
 * <p>
 * Due to the volume of vanilla listeners, these keys are automatically generated based on the class name.
 * 
 * @see {@link VanillaClientListeners} for vanilla client listener names.
 * @see {@link NeoForgeReloadListeners} for Neo-added listener names.
 */
public class VanillaServerListeners {
    private static final Map<Class<?>, Identifier> KNOWN_CLASSES = new LinkedHashMap<>();

    public static final Identifier RECIPES = key(RecipeManager.class);

    public static final Identifier FUNCTIONS = key(ServerFunctionLibrary.class);

    public static final Identifier ADVANCEMENTS = key(ServerAdvancementManager.class);

    /**
     * Sentinel field that will always reference the first reload listener in the vanilla order.
     */
    public static final Identifier FIRST = RECIPES;

    /**
     * Sentinel field that will always reference the last reload listener in the vanilla order.
     */
    public static final Identifier LAST = ADVANCEMENTS;

    private static Identifier key(Class<? extends PreparableReloadListener> cls) {
        if (KNOWN_CLASSES.containsKey(cls)) {
            // Prevent duplicate registration, in case we accidentally use the same class in two different fields.
            throw new UnsupportedOperationException("Attempted to create two keys for the same class");
        }

        Identifier key = VanillaClassToKey.convert(cls);
        KNOWN_CLASSES.put(cls, key);
        return key;
    }

    @Nullable
    @ApiStatus.Internal
    public static Identifier getNameForClass(Class<? extends PreparableReloadListener> cls) {
        return KNOWN_CLASSES.get(cls);
    }
}
