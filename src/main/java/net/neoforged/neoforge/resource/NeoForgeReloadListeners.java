/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Keys for Neo-added resource listeners, for use in dependency ordering in the relevant events.
 * 
 * @see {@link VanillaClientListeners} for vanilla client listener names.
 * @see {@link VanillaServerListeners} for vanilla server listener names.
 */
public class NeoForgeReloadListeners {

    // Server Listeners
    public static final Identifier LOOT_MODIFIERS = key("loot_modifiers");

    public static final Identifier RECIPE_PRIORITIES = key("recipe_priorities");

    public static final Identifier DATA_MAPS = key("data_maps");

    public static final Identifier CREATIVE_TABS = key("creative_tabs");

    // Client Listeners
    public static final Identifier BRANDING = key("branding");

    public static final Identifier OBJ_LOADER = key("obj_loader");

    public static final Identifier ENTITY_ANIMATIONS = key("entity_animations");

    private static Identifier key(String path) {
        return Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, path);
    }
}
