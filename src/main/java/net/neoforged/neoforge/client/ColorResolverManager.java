/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ColorResolver;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Manager for custom {@link ColorResolver} instances, collected via {@link RegisterColorHandlersEvent.ColorResolvers}.
 */
public final class ColorResolverManager {

    private static ImmutableList<ColorResolver> colorResolvers;

    @ApiStatus.Internal
    public static void init() {
        ImmutableList.Builder<ColorResolver> builder = ImmutableList.builder();
        ModLoader.get().postEvent(new RegisterColorHandlersEvent.ColorResolvers(builder));
        colorResolvers = builder.build();
    }

    /**
     * Register a {@link BlockTintCache} for every registered {@link ColorResolver} into the given target map.
     *
     * @param level  the level to use
     * @param target the map to populate
     */
    public static void registerBlockTintCaches(ClientLevel level, Map<ColorResolver, BlockTintCache> target) {
        for (var resolver : colorResolvers) {
            target.put(resolver, new BlockTintCache(pos -> level.calculateBlockTint(pos, resolver)));
        }
    }

    private ColorResolverManager() {}
}
