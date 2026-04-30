/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.resources.ResourceLocation;

/**
 * A group identifies a set of post-shader which is mutually exclusive.
 * The priority field controls in which order the various groups are applied, starting from low priority to high priority
 */
public record PostChainGroup(ResourceLocation resourceLocation, float priority) {
    /**
     * Default post-effect group. Unused
     */
    public static final PostChainGroup DEFAULT = new PostChainGroup(ResourceLocation.withDefaultNamespace("default"), 0);
    /**
     * Group used by spectator entity effects
     */
    public static final PostChainGroup ENTITY_SHADERS = new PostChainGroup(ResourceLocation.withDefaultNamespace("entity_shaders"), 1);
}
