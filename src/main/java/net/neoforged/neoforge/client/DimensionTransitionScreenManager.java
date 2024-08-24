/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class DimensionTransitionScreenManager {
    private static final Map<ResourceKey<Level>, DimensionTransitionScreen> toDimensionTransitions = new HashMap<>();
    private static final Map<ResourceKey<Level>, DimensionTransitionScreen> fromDimensionTransitions = new HashMap<>();

    @ApiStatus.Internal
    static void init() {
        ModLoader.postEventWrapContainerInModOrder(new RegisterDimensionTransitionScreenEvent(toDimensionTransitions, fromDimensionTransitions));
    }

    @Nullable
    public static DimensionTransitionScreen get(@Nullable ResourceKey<Level> toDimension, @Nullable ResourceKey<Level> fromDimension) {
        return toDimensionTransitions.getOrDefault(toDimension, fromDimensionTransitions.getOrDefault(fromDimension, null));
    }
}
